/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.checks.access.IRemoveSubCheckData;
import fr.neatmonster.nocheatplus.checks.moving.location.setback.DefaultSetBackStorage;
import fr.neatmonster.nocheatplus.checks.moving.location.tracking.LocationTrace;
import fr.neatmonster.nocheatplus.checks.moving.location.tracking.LocationTrace.TraceEntryPool;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveConsistency;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveTrace;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;
import fr.neatmonster.nocheatplus.checks.moving.velocity.AccountEntry;
import fr.neatmonster.nocheatplus.checks.moving.velocity.FrictionAxisVelocity;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleAxisVelocity;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleEntry;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeReference;
import fr.neatmonster.nocheatplus.components.data.ICanHandleTimeRunningBackwards;
import fr.neatmonster.nocheatplus.components.entity.IEntityAccessDimensions;
import fr.neatmonster.nocheatplus.components.location.IGetPosition;
import fr.neatmonster.nocheatplus.components.location.IPositionWithLook;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.location.RichEntityLocation;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import fr.neatmonster.nocheatplus.workaround.IWorkaroundRegistry.WorkaroundSet;

/**
 * Player specific data for the moving checks.
 */
public class MovingData extends ACheckData implements IRemoveSubCheckData {

    public static final class MovingDataFactory implements CheckDataFactory, ICanHandleTimeRunningBackwards {
        @Override
        public final ICheckData getData(final Player player) {
            return MovingData.getData(player);
        }

        @Override
        public ICheckData getDataIfPresent(UUID playerId, String playerName) {
            return MovingData.playersMap.get(playerName);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return MovingData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            MovingData.clear();
        }

        @Override
        public void handleTimeRanBackwards() {
            for (final MovingData data : playersMap.values()) {
                data.handleTimeRanBackwards();
            }
        }
    }

    /** The factory creating data. */
    public static final CheckDataFactory factory = new MovingDataFactory();

    private static Map<String, MovingData> playersMap = new HashMap<String, MovingData>();
    /** The map containing the data per players. */

    /**
     * Gets the data of a specified player.
     * final CheckDataFactory factory = new CheckDataFactory(
     * @param player
     *            the player
     * @return the data
     */
    public static MovingData getData(final Player player) {
        // Note that the trace might be null after just calling this.
        MovingData data = playersMap.get(player.getName());
        if (data == null) {
            data = new MovingData(MovingConfig.getConfig(player));
            playersMap.put(player.getName(), data);
        }
        return data;
    }

    public static ICheckData removeData(final String playerName) {
        return playersMap.remove(playerName);
    }

    public static void clear() {
        playersMap.clear();
    }

    /**
     * Clear data related to the given world.
     * @param world The world that gets unloaded.
     */
    public static void onWorldUnload(final World world) {
        final String worldName = world.getName();
        for (final MovingData data : playersMap.values()) {
            data.onWorldUnload(worldName);
        }
    }

    public static void onReload() {
        final MovingConfig globalCc = MovingConfig.getConfig((String) null);
        final int tick = TickTask.getTick();
        for (final MovingData data : playersMap.values()) {
            data.adjustOnReload(globalCc, tick);
        }
    }

    // Check specific.

    /**
     * Default lift-off envelope, used after resetting. <br>
     * TODO: Test, might be better ground.
     */
    private static final LiftOffEnvelope defaultLiftOffEnvelope = LiftOffEnvelope.UNKNOWN;

    public static final int              vehicleMorePacketsBufferDefault = 50;

    /** Tolerance value for using vertical velocity (the client sends different values than received with fight damage). */
    private static final double TOL_VVEL = 0.0625;

    //  private static final long IGNORE_SETBACK_Y = BlockProperties.F_SOLID | BlockProperties.F_GROUND | BlockProperties.F_CLIMBABLE | BlockProperties.F_LIQUID;

    /////////////////
    // Not static.
    /////////////////

    // Violation levels -----
    public double         creativeFlyVL            = 0.0;
    public double         morePacketsVL            = 0.0;
    public double         noFallVL                 = 0.0;
    public double         survivalFlyVL            = 0.0;
    public double         vehicleMorePacketsVL     = 0.0;
    public double         vehicleEnvelopeVL        = 0.0;

    // Data shared between the fly checks -----
    public int            bunnyhopDelay;
    public double         jumpAmplifier = 0;
    /** Last time the player was actually sprinting. */
    public long           timeSprinting = 0;
    public double         multSprinting = 1.30000002; // Multiplier at the last time sprinting.
    /** Compatibility entry for bouncing of slime blocks and the like. */
    public SimpleEntry verticalBounce = null;
    /** Last used block change id (BlockChangeTracker). */
    public final BlockChangeReference blockChangeRef = new BlockChangeReference();

    /** Tick at which walk/fly speeds got changed last time. */
    public int speedTick = 0;
    public float walkSpeed = 0.0f;
    public float flySpeed = 0.0f;

    /** Count set back (re-) setting. */
    private int setBackResetCount = 0;
    /**
     * setBackResetCount (incremented) at the time of (re-) setting the ordinary
     * set back.
     */
    private int setBackResetTime = 0;
    /**
     * setBackResetCount (incremented) at the time of (re-) setting the
     * morepackets set back.
     */
    private int morePacketsSetBackResetTime = 0;

    /**
     * Position teleported from into another world. Only used for certain
     * contexts for workarounds.
     */
    public IPositionWithLook crossWorldFrom = null;

    /** Keep track of currently processed (if) and past moves for player moving. */
    public final MoveTrace <PlayerMoveData> playerMoves = new MoveTrace<PlayerMoveData>(new Callable<PlayerMoveData>() {
        @Override
        public PlayerMoveData call() throws Exception {
            return new PlayerMoveData();
        }
    }, 2);

    /** Keep track of currently processed (if) and past moves for vehicle moving. */
    // TODO: There may be need to store such data with vehicles, or detect tandem abuse in a different way.
    public final MoveTrace <VehicleMoveData> vehicleMoves = new MoveTrace<VehicleMoveData>(new Callable<VehicleMoveData>() {
        @Override
        public VehicleMoveData call() throws Exception {
            return new VehicleMoveData();
        }
    }, 2);

    // Velocity handling.
    /** Vertical velocity modeled as an axis (positive and negative possible) */
    private final SimpleAxisVelocity verVel = new SimpleAxisVelocity();

    /** Horizontal velocity modeled as an axis (always positive) */
    private final FrictionAxisVelocity horVel = new FrictionAxisVelocity();

    /** Duration of the boost effect in ticks. */
    public int fireworksBoostDuration = 0;
    /**
     * Expire at this tick.
     */
    public int fireworksBoostTickExpire = 0;

    // Coordinates.
    /** Moving trace (to-positions, use tick as time). This is initialized on "playerJoins, i.e. MONITOR, and set to null on playerLeaves." */
    private final LocationTrace trace;

    // sf rather
    /** Basic envelope constraints for switching into air. */
    public LiftOffEnvelope liftOffEnvelope = defaultLiftOffEnvelope;
    /** Count how many moves have been made inside a medium (other than air). */
    public int insideMediumCount = 0;
    // TODO: Does combinedMedium stuff need resetting on join/teleport/special?
    /** Number of moves for horizontal moving within air + certain medium. */
    public int combinedMediumHCount = 0;
    /** Sum of actual speed / base speed for horizontal moving within air + certain medium. */
    public double combinedMediumHValue = 0.0;

    // Locations shared between all checks.
    private Location    setBack = null;
    private Location    teleported = null;

    // Check specific data -----

    // Data of the more packets check.
    /** Packet frequency count. */
    public final ActionFrequency    morePacketsFreq;
    /** Burst count. */
    public final ActionFrequency    morePacketsBurstFreq;
    private Location                morePacketsSetback = null;

    // Data of the no fall check.
    public float            noFallFallDistance = 0;
    /** Last y coordinate from when the player was on ground. */
    public double           noFallMaxY = 0;
    /** Indicate that NoFall is not to use next damage event for checking on-ground properties. */ 
    public boolean          noFallSkipAirCheck = false;
    // Passable check.
    public double           passableVL;

    // Data of the survival fly check.
    public double       sfHorizontalBuffer = 0.0; // ineffective: SurvivalFly.hBufMax / 2.0;
    /** Event-counter to cover up for sprinting resetting server side only. Set in the FighListener. */
    public int          lostSprintCount = 0;
    public int          sfJumpPhase = 0;
    /**
     * Count how many times in a row v-dist has been zero, only for in-air
     * moves, updated on not cancelled moves (aimed at in-air workarounds).
     */
    public int          sfZeroVdistRepeat = 0;
    /** Only used during processing, to keep track of sub-checks using velocity. Reset in velocityTick, before checks run. */

    /** "Dirty" flag, for receiving velocity and similar while in air. */
    private boolean     sfDirty = false;

    /** Indicate low jumping descending phase (likely cheating). */
    public boolean sfLowJump = false;
    public boolean sfNoLowJump = false; // Hacks.

    /**
     * Counting while the player is not on ground and not moving. A value <0
     * means not hovering at all.
     */
    public int          sfHoverTicks = -1;
    /**
     * First count these down before incrementing sfHoverTicks. Set on join, if
     * configured so.
     */
    public int          sfHoverLoginTicks = 0;
    public int          sfOnIce = 0; // TODO: Replace by allowed speed + friction.
    public long         sfCobwebTime = 0;
    public double       sfCobwebVL = 0;
    public long         sfVLTime = 0;


    // Accounting info.
    public final ActionAccumulator vDistAcc = new ActionAccumulator(3, 3);
    /**
     * Rough friction factor estimate, 0.0 is the reset value (maximum with
     * lift-off/burst speed is used).
     */
    public double lastFrictionHorizontal = 0.0;
    /**
     * Rough friction factor estimate, 0.0 is the reset value (maximum with
     * lift-off/burst speed is used).
     */
    public double lastFrictionVertical = 0.0;
    /** Used during processing, no resetting necessary.*/
    public double nextFrictionHorizontal = 0.0;
    /** Used during processing, no resetting necessary.*/
    public double nextFrictionVertical= 0.0;
    /** Workarounds */
    public final WorkaroundSet ws;

    // HOT FIX / WORKAROUND
    /**
     * Set to true after login/respawn, only if the set back is reset there.
     * Reset in MovingListener after handling PlayerMoveEvent
     */
    public boolean joinOrRespawn = false;
    /**
     * Number of (player/vehicle) move events since set.back. Update after
     * running standard checks on that EventPriority level (not MONITOR).
     */
    public int timeSinceSetBack = 0;
    /**
     * Location hash value of the last (player/vehicle) set back, for checking
     * independently of which set back location had been used.
     */
    public int lastSetBackHash = 0;

    // Vehicles.
    /**
     * Inconsistency-flag. Set on moving inside of vehicles, reset on exiting
     * properly. Workaround for VehicleLeaveEvent missing.
     */
    public boolean wasInVehicle = false; // Workaround
    /**
     * Set to indicate that events happen during a vehicle set back. Allows
     * skipping some resetting.
     */
    public boolean isVehicleSetBack = false; // Workaround
    public MoveConsistency vehicleConsistency = MoveConsistency.INCONSISTENT; // Workaround
    public final DefaultSetBackStorage vehicleSetBacks = new DefaultSetBackStorage();
    // Data of the more packets vehicle check.
    public int              vehicleMorePacketsBuffer = vehicleMorePacketsBufferDefault;
    public long             vehicleMorePacketsLastTime;
    /** Task id of the vehicle set back task. */ 
    public int              vehicleSetBackTaskId = -1;

    public MovingData(final MovingConfig config) {
        super(config);
        morePacketsFreq = new ActionFrequency(config.morePacketsEPSBuckets, 500);
        morePacketsBurstFreq = new ActionFrequency(12, 5000);

        // Location trace.
        trace = new LocationTrace(config.traceMaxAge, config.traceMaxSize, NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(TraceEntryPool.class));

        // A new set of workaround conters.
        ws = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(WRPT.class).getWorkaroundSet(WRPT.WS_MOVING);

    }

    @Override
    public boolean removeSubCheckData(final CheckType checkType) {
        // TODO: LocationTrace stays (leniency for other players!).
        // TODO: Likely more fields left to change.
        switch (checkType) {
            /*
             * TODO: case MOVING: // Remove all in-place (future: data might
             * stay as long as the player is online).
             */
            case MOVING_SURVIVALFLY:
                survivalFlyVL = 0;
                clearFlyData();
                resetSetBack(); // TODO: Not sure this is really best for compatibility.
                // TODO: other?
                return true;
            case MOVING_CREATIVEFLY:
                creativeFlyVL = 0;
                clearFlyData();
                resetSetBack(); // TODO: Not sure this is really best for compatibility.
                // TODO: other?
                return true;
            case MOVING_NOFALL:
                noFallVL = 0;
                clearNoFallData();
                return true;
            case MOVING_MOREPACKETS:
                morePacketsVL = 0;
                clearPlayerMorePacketsData();
                morePacketsSetback = null;
                morePacketsSetBackResetTime = 0;
                return true;
            case MOVING_PASSABLE:
                passableVL = 0;
                return true;
            case MOVING_VEHICLE:
                vehicleEnvelopeVL = 0;
                vehicleMorePacketsVL = 0;
                clearVehicleData();
                return true;
            case MOVING_VEHICLE_ENVELOPE:
                vehicleEnvelopeVL = 0;
                vehicleMoves.invalidate();
                vehicleSetBacks.invalidateAll(); // Also invalidates morepackets set back.
                return true;
            case MOVING_VEHICLE_MOREPACKETS:
                vehicleMorePacketsVL = 0;
                clearVehicleMorePacketsData();
                return true;
            default:
                return false;
        }
    }

    /**
     * Clear fly and more packets check data for both vehicles and players.
     */
    public void clearMostMovingCheckData() {
        clearFlyData();
        clearVehicleData();
        clearAllMorePacketsData();
    }

    /**
     * Clear vehicle related data, except more packets.
     */
    public void clearVehicleData() {
        // TODO: Not entirely sure what to do here.
        vehicleMoves.invalidate();
        vehicleSetBacks.invalidateAll();
    }

    /**
     * Clear the data of the fly checks (not more-packets).
     */
    public void clearFlyData() {
        playerMoves.invalidate();
        bunnyhopDelay = 0;
        sfJumpPhase = 0;
        jumpAmplifier = 0;
        setBack = null;
        sfZeroVdistRepeat = 0;
        clearAccounting();
        clearNoFallData();
        removeAllPlayerSpeedModifiers();
        lostSprintCount = 0;
        sfHoverTicks = sfHoverLoginTicks = -1;
        sfDirty = false;
        sfLowJump = false;
        liftOffEnvelope = defaultLiftOffEnvelope;
        insideMediumCount = 0;
        vehicleConsistency = MoveConsistency.INCONSISTENT;
        lastFrictionHorizontal = lastFrictionVertical = 0.0;
        verticalBounce = null;
        blockChangeRef.valid = false;
    }

    /**
     * On confirming a set back (teleport monitor / move start point): Mildly
     * reset the flying data without losing any important information. Past move
     * is adjusted to the given setBack, internal setBack is only updated, if
     * none is set.
     * 
     * @param setBack
     */
    public void onSetBack(final PlayerLocation setBack) {
        // Reset positions (a teleport should follow, though).
        this.morePacketsSetback = null;
        clearAccounting(); // Might be more safe to do this.
        // Keep no-fall data.
        // Fly data: problem is we don't remember the settings for the set back location.
        // Assume the player to start falling from there rather, or be on ground.
        // TODO: Check if to adjust some counters to state before setback? 
        // Keep jump amplifier
        // Keep bunny-hop delay (?)
        // keep jump phase.
        lostSprintCount = 0;
        sfHoverTicks = -1; // 0 ?
        sfDirty = false;
        sfLowJump = false;
        liftOffEnvelope = defaultLiftOffEnvelope;
        insideMediumCount = 0;
        removeAllPlayerSpeedModifiers();
        vehicleConsistency = MoveConsistency.INCONSISTENT; // Not entirely sure here.
        lastFrictionHorizontal = lastFrictionVertical = 0.0;
        verticalBounce = null;
        timeSinceSetBack = 0;
        lastSetBackHash = setBack == null ? 0 : setBack.hashCode();
        // Reset to setBack.
        resetPlayerPositions(setBack);
        adjustMediumProperties(setBack);
        // Only setSetBack if no set back location is there.
        if (setBack == null) {
            setSetBack(setBack);
        }
        // vehicleSetBacks.resetAllLazily(setBack); // Not good: Overrides older set back locations.
    }

    /**
     * Move event: Mildly reset some data, prepare setting a new to-Location.
     */
    public void prepareSetBack(final Location loc) {
        playerMoves.invalidate();
        vehicleMoves.invalidate();
        clearAccounting();
        sfJumpPhase = 0;
        sfZeroVdistRepeat = 0;
        verticalBounce = null;
        // Remember where we send the player to.
        setTeleported(loc);
        // TODO: sfHoverTicks ?
    }

    /**
     * Adjust properties that relate to the medium, called on set back and
     * similar. <br>
     * Currently: liftOffEnvelope, nextFriction.
     * 
     * @param loc
     */
    public void adjustMediumProperties(final PlayerLocation loc) {
        // Ensure block flags have been collected.
        loc.collectBlockFlags();
        // Simplified.
        if (loc.isInWeb()) {
            liftOffEnvelope = LiftOffEnvelope.NO_JUMP;
            nextFrictionHorizontal = nextFrictionVertical = 0.0;
        }
        else if (loc.isInLiquid()) {
            // TODO: Distinguish strong limit.
            liftOffEnvelope = LiftOffEnvelope.LIMIT_LIQUID;
            if (loc.isInLava()) {
                nextFrictionHorizontal = nextFrictionVertical = Magic.FRICTION_MEDIUM_LAVA;
            } else {
                nextFrictionHorizontal = nextFrictionVertical = Magic.FRICTION_MEDIUM_WATER;
            }
        }
        else if (loc.isOnGround()) {
            liftOffEnvelope = LiftOffEnvelope.NORMAL;
            nextFrictionHorizontal = nextFrictionVertical = Magic.FRICTION_MEDIUM_AIR;
        }
        else {
            liftOffEnvelope = LiftOffEnvelope.UNKNOWN;
            nextFrictionHorizontal = nextFrictionVertical = Magic.FRICTION_MEDIUM_AIR;
        }
        insideMediumCount = 0;
    }


    /**
     * Called when a player leaves the server.
     */
    public void onPlayerLeave() {
        removeAllPlayerSpeedModifiers();
        trace.reset();
        playerMoves.invalidate();
        vehicleMoves.invalidate();
    }

    /**
     * Clean up data related to worlds with the given name (not case-sensitive).
     * @param worldName
     */
    public void onWorldUnload(final String worldName) {
        // TODO: Unlink world references.
        if (teleported != null && worldName.equalsIgnoreCase(teleported.getWorld().getName())) {
            resetTeleported();
        }
        if (setBack != null && worldName.equalsIgnoreCase(setBack.getWorld().getName())) {
            clearFlyData();
        }
        if (morePacketsSetback != null && worldName.equalsIgnoreCase(morePacketsSetback.getWorld().getName())) {
            clearPlayerMorePacketsData();
            clearNoFallData(); // just in case.
        }
        // (Assume vehicle data needn't really reset here.)
        vehicleSetBacks.resetByWorldName(worldName);
    }

    /**
     * Invalidate all past player moves data and set last position if not null.
     * 
     * @param loc
     */
    public void resetPlayerPositions(final PlayerLocation loc) {
        resetPlayerPositions();
        if (loc != null) {
            final PlayerMoveData lastMove = playerMoves.getFirstPastMove();
            // Always set with extra properties.
            lastMove.setWithExtraProperties(loc);
        }
    }

    /**
     * Invalidate all past moves data (player).
     */
    private void resetPlayerPositions() {
        playerMoves.invalidate();
        sfZeroVdistRepeat = 0;
        sfDirty = false;
        sfLowJump = false;
        liftOffEnvelope = defaultLiftOffEnvelope;
        insideMediumCount = 0;
        lastFrictionHorizontal = lastFrictionVertical = 0.0;
        verticalBounce = null;
        blockChangeRef.valid = false;
        // TODO: other buffers ?
        // No reset of vehicleConsistency.
    }

    /**
     * Invalidate all past vehicle moves data and set last position if not null.
     * 
     * @param loc
     */
    public void resetVehiclePositions(final RichEntityLocation loc) {
        // TODO: Other properties (convenience, e.g. set back?) ?
        vehicleMoves.invalidate();
        if (loc != null) {
            final VehicleMoveData lastMove = vehicleMoves.getFirstPastMove();
            // Always set with extra properties.
            lastMove.setWithExtraProperties(loc);
            final Entity entity = loc.getEntity();
            lastMove.vehicleId = entity.getUniqueId();
            lastMove.vehicleType = entity.getType();
        }
    }

    /**
     * Clear accounting data.
     */
    public void clearAccounting() {
        vDistAcc.clear();
    }

    /**
     * Clear the data of the more packets checks, both for players and vehicles.
     */
    public void clearAllMorePacketsData() {
        clearPlayerMorePacketsData();
        clearVehicleMorePacketsData();
    }

    public void clearPlayerMorePacketsData() {
        morePacketsSetback = null;
        final long now = System.currentTimeMillis();
        morePacketsFreq.clear(now);
        morePacketsBurstFreq.clear(now);
        // TODO: Also reset other data ?
    }

    /**
     * Reduce the morepackets frequency counters by the given amount, capped at
     * a minimum of 0.
     * 
     * @param amount
     */
    public void reducePlayerMorePacketsData(final float amount) {
        CheckUtils.reduce(System.currentTimeMillis(), amount, morePacketsFreq, morePacketsBurstFreq);
    }

    public void clearVehicleMorePacketsData() {
        vehicleMorePacketsLastTime = 0;
        vehicleMorePacketsBuffer = vehicleMorePacketsBufferDefault;
        vehicleSetBacks.getMidTermEntry().setValid(false); // TODO: Will have other resetting conditions later on.
        // TODO: Also reset other data ?
    }

    /**
     * Clear the data of the new fall check.
     */
    public void clearNoFallData() {
        noFallFallDistance = 0;
        noFallMaxY = 0.0;
        noFallSkipAirCheck = false;
    }

    /**
     * Set the set back location, this will also adjust the y-coordinate for some block types (at least air).
     * @param loc
     */
    public void setSetBack(final PlayerLocation loc) {
        if (setBack == null) {
            setBack = loc.getLocation();
        }
        else{
            LocUtil.set(setBack, loc);
        }
        // TODO: Consider adjusting the set back-y here. Problem: Need to take into account for bounding box (collect max-ground-height needed).
        setBackResetTime = ++setBackResetCount;
    }

    /**
     * Convenience method.
     * @param loc
     */
    public void setSetBack(final Location loc) {
        if (setBack == null) {
            setBack = LocUtil.clone(loc);
        }
        else{
            LocUtil.set(setBack, loc);
        }
        setBackResetTime = ++setBackResetCount;
    }

    /**
     * Get the set back location with yaw and pitch set form ref.
     * @param ref
     * @return
     */
    public Location getSetBack(final Location ref) {
        return LocUtil.clone(setBack, ref);
    }

    /**
     * Get the set back location with yaw and pitch set from ref.
     * @param ref
     * @return
     */
    public Location getSetBack(final PlayerLocation ref) {
        return LocUtil.clone(setBack, ref);
    }

    public boolean hasSetBack() {
        return setBack != null;
    }

    public boolean hasSetBackWorldChanged(final Location loc) {
        if (setBack == null) {
            return true;
        }
        else {
            return setBack.getWorld().equals(loc.getWorld());
        }
    }


    public double getSetBackX() {
        return setBack.getX();
    }

    public double getSetBackY() {
        return setBack.getY();
    }

    public double getSetBackZ() {
        return setBack.getZ();
    }

    public void setSetBackY(final double y) {
        setBack.setY(y);
        // (Skip setting/increasing the reset count.)
    }

    /**
     * Test, if the 'teleported' location is set, e.g. on a scheduled set back.
     * 
     * @return
     */
    public boolean hasTeleported() {
        return teleported != null;
    }

    /**
     * Return a copy of the teleported-to Location.
     * @return
     */
    public final Location getTeleported() {
        // TODO: here a reference might do.
        return teleported == null ? teleported : LocUtil.clone(teleported);
    }

    /**
     * Check if the given location equals to the 'teleported' (set back)
     * location.
     * 
     * @param loc
     * @return In case of either loc or teleported being null, false is
     *         returned, otherwise teleported.equals(loc).
     */
    public boolean isTeleported(final Location loc) {
        return loc != null && teleported != null && teleported.equals(loc);
    }

    /**
     * Check if the given location has the same coordinates like the
     * 'teleported' (set back) location. This is more light-weight and more
     * lenient than isTeleported, because world and yaw and pitch are all
     * ignored.
     * 
     * @param loc
     * @return In case of either loc or teleported being null, false is
     *         returned, otherwise TrigUtil.isSamePos(teleported, loc).
     */
    public boolean isTeleportedPosition(final Location loc) {
        return loc != null && teleported != null && TrigUtil.isSamePos(teleported, loc);
    }

    /**
     * Check if the given location has the same coordinates like the
     * 'teleported' (set back) location. This is more light-weight and more
     * lenient than isTeleported, because world and yaw and pitch are all
     * ignored.
     * 
     * @param loc
     * @return In case of either loc or teleported being null, false is
     *         returned, otherwise TrigUtil.isSamePos(pos, teleported).
     */
    public boolean isTeleportedPosition(final IGetPosition pos) {
        return pos != null && teleported != null && TrigUtil.isSamePos(pos, teleported);
    }

    /**
     * Set teleport-to location to recognize NCP set backs. This copies the coordinates and world.
     * @param loc
     */
    public final void setTeleported(final Location loc) {
        teleported = LocUtil.clone(loc); // Always overwrite.
    }

    public boolean hasMorePacketsSetBack() {
        return morePacketsSetback != null;
    }

    /**
     * Test if the morepackets set back is older than the ordinary set back.
     * Does not check for existence of either.
     * 
     * @return
     */
    public boolean isMorePacketsSetBackOldest() {
        return morePacketsSetBackResetTime < setBackResetTime;
    }

    public final void setMorePacketsSetBack(final PlayerLocation loc) {
        if (morePacketsSetback == null) {
            morePacketsSetback = loc.getLocation();
        }
        else {
            LocUtil.set(morePacketsSetback, loc);
        }
        morePacketsSetBackResetTime = ++setBackResetCount;
    }

    public final void setMorePacketsSetBack(final Location loc) {
        if (morePacketsSetback == null) {
            morePacketsSetback = LocUtil.clone(loc);
        }
        else {
            LocUtil.set(morePacketsSetback, loc);
        }
        morePacketsSetBackResetTime = ++setBackResetCount;
    }

    public Location getMorePacketsSetBack() {
        return LocUtil.clone(morePacketsSetback);
    }

    public final void resetTeleported() {
        teleported = null;
    }

    /**
     * Set set back location to null.
     */
    public final void resetSetBack() {
        setBack = null;
    }

    /**
     * Add velocity to internal book-keeping.
     * 
     * @param player
     * @param data
     * @param cc
     * @param vx
     * @param vy
     * @param vz
     */
    public void addVelocity(final Player player, final MovingConfig cc, 
            final double vx, final double vy, final double vz) {
        addVelocity(player, cc, vx, vy, vz, 0L);
    }

    /**
     * Add velocity to internal book-keeping.
     * 
     * @param player
     * @param data
     * @param cc
     * @param vx
     * @param vy
     * @param vz
     * @param flags
     *            Flags to use with velocity entries.
     */
    public void addVelocity(final Player player, final MovingConfig cc, 
            final double vx, final double vy, final double vz, final long flags) {

        final int tick = TickTask.getTick();
        // TODO: Slightly odd to call this each time, might switch to a counter-strategy (move - remove). 
        removeInvalidVelocity(tick  - cc.velocityActivationTicks);

        if (debug) {
            CheckUtils.debug(player, CheckType.MOVING, "New velocity: " + vx + ", " + vy + ", " + vz);
        }

        // Always add vertical velocity.
        verVel.add(new SimpleEntry(tick, vy, flags, cc.velocityActivationCounter));

        // TODO: Should also switch to adding always.
        if (vx != 0.0 || vz != 0.0) {
            final double newVal = Math.sqrt(vx * vx + vz * vz);
            horVel.add(new AccountEntry(tick, newVal, cc.velocityActivationCounter, getHorVelValCount(newVal)));
        }

        // Set dirty flag here.
        sfDirty = true; // TODO: Set on using the velocity, due to latency !
        sfNoLowJump = true; // TODO: Set on using the velocity, due to latency !

    }

    /**
     * Std. value counter for horizontal velocity, based on the vlaue.
     * 
     * @param velocity
     * @return
     */
    public static int getHorVelValCount(double velocity) {
        return Math.max(20,  1 + (int) Math.round(velocity * 10.0));
    }

    public void prependVerticalVelocity(final SimpleEntry entry) {
        verVel.addToFront(entry);
    }

    /**
     * Get the first element without using it.
     * @param amount
     * @param minActCount
     * @param maxActCount
     * @return
     */
    public SimpleEntry peekVerticalVelocity(final double amount, final int minActCount, final int maxActCount) {
        return verVel.peek(amount, minActCount, maxActCount, TOL_VVEL);
    }

    public void addVerticalVelocity(final SimpleEntry entry) {
        verVel.add(entry);
    }

    /**
     * Add horizontal velocity directly to horizontal-only bookkeeping.
     * 
     * @param vel
     *            Assumes positive values always.
     */
    public void addHorizontalVelocity(final AccountEntry vel) {
        horVel.add(vel);
    }

    /**
     * Remove/reset all speed modifier tracking, like vertical and horizontal
     * velocity, elytra boost, buffer.
     */
    private void removeAllPlayerSpeedModifiers() {
        // Velocity
        removeAllVelocity();
        // Elytra boost best fits velocity / effects.
        fireworksBoostDuration = 0; 
        fireworksBoostTickExpire = 0;
        // Horizontal buffer.
        sfHorizontalBuffer = 0.0;
    }

    /**
     * Reset velocity tracking (h+v).
     */
    public void removeAllVelocity() {
        horVel.clear();
        verVel.clear();
        sfDirty = false;
    }

    /**
     * Remove all velocity entries that are invalid. Checks both active and queued.
     * <br>(This does not catch invalidation by speed / direction changing.)
     * @param tick All velocity added before this tick gets removed.
     */
    public void removeInvalidVelocity(final int tick) {
        horVel.removeInvalid(tick);
        verVel.removeInvalid(tick);
    }

    /**
     * Clear only active horizontal velocity.
     */
    public void clearActiveHorVel() {
        horVel.clearActive();
    }

    public boolean hasActiveHorVel() {
        return horVel.hasActive();
    }

    public boolean hasQueuedHorVel() {
        return horVel.hasQueued();
    }

    /**
     * Active or queued.
     * @return
     */
    public boolean hasAnyHorVel() {
        return horVel.hasAny();
    }

    /**
     * Queued velocity only.
     * @return
     */
    public boolean hasAnyVerVel() {
        return verVel.hasQueued();
    }

    //    public boolean hasActiveVerVel() {
    //        return verVel.hasActive();
    //    }

    //    public boolean hasQueuedVerVel() {
    //        return verVel.hasQueued();
    //    }

    /**
     * Called for moving events. Remove invalid entries, increase age of velocity, decrease amounts, check which entries are invalid. Both horizontal and vertical.
     */
    public void velocityTick(final int invalidateBeforeTick) {
        // Remove invalid velocity.
        removeInvalidVelocity(invalidateBeforeTick);

        // Horizontal velocity (intermediate concept).
        horVel.tick();

        // (Vertical velocity does not tick.)

        // Renew the dirty phase.
        if (!sfDirty && (horVel.hasActive() || horVel.hasQueued())) {
            sfDirty = true;
        }
    }

    /**
     * Get effective amount of all used velocity. Non-destructive.
     * @return
     */
    public double getHorizontalFreedom() {
        return horVel.getFreedom();
    }

    /**
     * Use all queued velocity until at least amount is matched.
     * Amount is the horizontal distance that is to be covered by velocity (active has already been checked).
     * <br>
     * If the modeling changes (max instead of sum or similar), then this will be affected.
     * @param amount The amount demanded, must be positive.
     * @return
     */
    public double useHorizontalVelocity(final double amount) {
        final double available = horVel.use(amount);
        if (available >= amount) {
            sfDirty = true;
        }
        return available;
    }

    /**
     * Debugging.
     * @param builder
     */
    public void addHorizontalVelocity(final StringBuilder builder) {
        if (horVel.hasActive()) {
            builder.append("\n" + " horizontal velocity (active):");
            horVel.addActive(builder);
        }
        if (horVel.hasQueued()) {
            builder.append("\n" + " horizontal velocity (queued):");
            horVel.addQueued(builder);
        }
    }

    /**
     * Get the first matching velocity entry (invalidate others). Sets
     * verVelUsed if available.
     * 
     * @param amount
     * @return
     */
    public SimpleEntry useVerticalVelocity(final double amount) {
        final SimpleEntry available = verVel.use(amount, TOL_VVEL);
        if (available != null) {
            playerMoves.getCurrentMove().verVelUsed = available;
            sfDirty = true;
            // TODO: Consider sfNoLowJump = true;
        }
        return available;
    }

    /**
     * Use the verVelUsed field, if it matches. Otherwise call
     * useVerticalVelocity(amount).
     * 
     * @param amount
     * @return
     */
    public SimpleEntry getOrUseVerticalVelocity(final double amount) {
        final SimpleEntry verVelUsed = playerMoves.getCurrentMove().verVelUsed;
        if (verVelUsed != null) {
            if (verVel.matchesEntry(verVelUsed, amount, TOL_VVEL)) {
                return verVelUsed;
            }
        }
        return useVerticalVelocity(amount);
    }

    /**
     * Debugging.
     * @param builder
     */
    public void addVerticalVelocity(final StringBuilder builder) {
        if (verVel.hasQueued()) {
            builder.append("\n" + " vertical velocity (queued):");
            verVel.addQueued(builder);
        }
    }

    /**
     * Test if the location is the same, ignoring pitch and yaw.
     * @param loc
     * @return
     */
    public boolean isSetBack(final Location loc) {
        if (loc == null || setBack == null) {
            return false;
        }
        if (!loc.getWorld().getName().equals(setBack.getWorld().getName())) {
            return false;
        }
        return loc.getX() == setBack.getX() && loc.getY() == setBack.getY() && loc.getZ() == setBack.getZ();
    }

    public void adjustWalkSpeed(final float walkSpeed, final int tick, final int speedGrace) {
        if (walkSpeed > this.walkSpeed) {
            this.walkSpeed = walkSpeed;
            this.speedTick = tick;
        } else if (walkSpeed < this.walkSpeed) {
            if (tick - this.speedTick > speedGrace) {
                this.walkSpeed = walkSpeed;
                this.speedTick = tick;
            }
        } else {
            this.speedTick = tick;
        }
    }

    public void adjustFlySpeed(final float flySpeed, final int tick, final int speedGrace) {
        if (flySpeed > this.flySpeed) {
            this.flySpeed = flySpeed;
            this.speedTick = tick;
        } else if (flySpeed < this.flySpeed) {
            if (tick - this.speedTick > speedGrace) {
                this.flySpeed = flySpeed;
                this.speedTick = tick;
            }
        } else {
            this.speedTick = tick;
        }
    }

    /**
     * This tests for a LocationTrace instance being set at all, not for locations having been added.
     * @return
     */
    public boolean hasTrace() {
        return trace != null;
    }

    /**
     * Convenience: Access method to simplify coding, being aware of some plugins using Player implementations as NPCs, leading to traces not being present.
     * @return
     */
    public LocationTrace getTrace(final Player player) {
        return trace;
    }

    /**
     * Ensure to have a LocationTrace instance with the given parameters.
     * 
     * @param maxAge
     * @param maxSize
     * @return
     */
    private LocationTrace getTrace(final int maxAge, final int maxSize) {
        if (trace.getMaxSize() != maxSize || trace.getMaxAge() != maxAge) {
            // TODO: Might want to have tick passed as argument?
            trace.adjustSettings(maxAge, maxSize, TickTask.getTick());
        } 
        return trace;
    }

    /**
     * Convenience method to add a location to the trace, creates the trace if
     * necessary.
     * 
     * @param player
     * @param loc
     * @param time
     * @param iead
     *            If null getEyeHeight and 0.3 are used (assume fake player).
     * @return Updated LocationTrace instance, for convenient use, without
     *         sticking too much to MovingData.
     */
    public LocationTrace updateTrace(final Player player, final Location loc, final long time, 
            final IEntityAccessDimensions iead) {
        final LocationTrace trace = getTrace(player);
        if (iead == null) {
            // TODO: 0.3 from bukkit based default heights (needs extra registered classes).
            trace.addEntry(time, loc.getX(), loc.getY(), loc.getZ(), 0.3, player.getEyeHeight());
        }
        else {
            trace.addEntry(time, loc.getX(), loc.getY(), loc.getZ(), iead.getWidth(player) / 2.0, Math.max(player.getEyeHeight(), iead.getHeight(player)));
        }
        return trace;
    }

    /**
     * Convenience.
     * @param loc
     * @param time
     * @param cc
     */
    public void resetTrace(final Player player, final Location loc, final long time, 
            final IEntityAccessDimensions iead, final MovingConfig cc) {
        resetTrace(player, loc, time, cc.traceMaxAge, cc.traceMaxSize, iead);
    }

    /**
     * Convenience: Create or just reset the trace, add the current location.
     * @param loc 
     * @param size
     * @param mergeDist
     * @param traceMergeDist 
     */
    public void resetTrace(final Player player, final Location loc, final long time, 
            final int maxAge, final int maxSize, final IEntityAccessDimensions iead) {
        if (trace != null) {
            trace.reset();
        }
        getTrace(maxAge, maxSize).addEntry(time, loc.getX(), loc.getY(), loc.getZ(), 
                iead.getWidth(player) / 2.0, Math.max(player.getEyeHeight(), iead.getHeight(player)));
    }

    /**
     * Adjust to new parameters.
     */
    protected void adjustOnReload(final MovingConfig cc, final int tick) {
        trace.adjustSettings(cc.traceMaxAge, cc.traceMaxSize, tick);
    }

    /**
     * Test if velocity has affected the in-air jumping phase. Keeps set until
     * reset on-ground or otherwise. Use clearActiveVerVel to force end velocity
     * jump phase. Use hasAnyVerVel() to test if active or queued vertical
     * velocity should still be able to influence the in-air jump phase.
     * 
     * @return
     */
    public boolean isVelocityJumpPhase() {
        return sfDirty;
    }

    /**
     * Refactoring stage: Test which value sfDirty should have and set
     * accordingly. This should only be called, if the player reached ground.
     * 
     * @return If the velocity jump phase is still active (sfDirty).
     */
    public boolean resetVelocityJumpPhase() {
        if (horVel.hasActive() || horVel.hasQueued()) {
            // TODO: What with vertical ?
            sfDirty = true;
        } else {
            sfDirty = false;
        }
        return sfDirty;
    }

    /**
     * Force set the move to be affected by previous speed. Currently
     * implemented as setting velocity jump phase.
     */
    public void setFrictionJumpPhase() {
        // TODO: Better and more reliable modeling.
        sfDirty = true;
    }

    public void useVerticalBounce(final Player player) {
        // CHEATING: Ensure fall distance is reset.
        player.setFallDistance(0f);
        noFallMaxY = 0.0;
        noFallFallDistance = 0f;
        noFallSkipAirCheck = true;
        prependVerticalVelocity(verticalBounce);
        verticalBounce = null;
    }

    public void handleTimeRanBackwards() {
        final long time = System.currentTimeMillis();
        timeSprinting = Math.min(timeSprinting, time);
        vehicleMorePacketsLastTime = Math.min(vehicleMorePacketsLastTime, time);
        sfCobwebTime = Math.min(sfCobwebTime, time);
        sfVLTime = Math.min(sfVLTime, time);
        clearAccounting(); // Not sure: adding up might not be nice.
        removeAllPlayerSpeedModifiers(); // TODO: This likely leads to problems.
        // (ActionFrequency can handle this.)
    }

    /**
     * Get the y-axis velocity tracker. Rather for testing purposes.
     * 
     * @return
     */
    public SimpleAxisVelocity getVerticalVelocityTracker() {
        return verVel;
    }

}
