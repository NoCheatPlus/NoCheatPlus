package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.checks.moving.locations.LocUtil;
import fr.neatmonster.nocheatplus.checks.moving.locations.LocationTrace;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveConsistency;
import fr.neatmonster.nocheatplus.checks.moving.velocity.AccountEntry;
import fr.neatmonster.nocheatplus.checks.moving.velocity.FrictionAxisVelocity;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleAxisVelocity;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleEntry;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Player specific data for the moving checks.
 */
public class MovingData extends ACheckData {

    /** The factory creating data. */
    public static final CheckDataFactory factory = new CheckDataFactory() {
        @Override
        public final ICheckData getData(final Player player) {
            return MovingData.getData(player);
        }

        @Override
        public ICheckData removeData(final String playerName) {
            return MovingData.removeData(playerName);
        }

        @Override
        public void removeAllData() {
            clear();
        }
    };

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
        for (final MovingData data : playersMap.values()) {
            data.deleteTrace(); // Safe side.
        }
    }

    // Check specific.

    /**
     * Default lift-off envelope, used after resetting. <br>
     * TODO: Test, might be better ground.
     */
    private static final LiftOffEnvelope defaultLiftOffEnvelope = LiftOffEnvelope.UNKNOWN;

    /** Tolerance value for using vertical velocity (the client sends different values than received with fight damage). */
    private static final double TOL_VVEL = 0.0625;

    //  private static final long IGNORE_SETBACK_Y = BlockProperties.F_SOLID | BlockProperties.F_GROUND | BlockProperties.F_CLIMBABLE | BlockProperties.F_LIQUID;

    /////////////////
    // Not static.
    /////////////////

    // Violation levels -----
    public double         creativeFlyVL            = 0D;
    public double         morePacketsVL            = 0D;
    public double         morePacketsVehicleVL     = 0D;
    public double         noFallVL                 = 0D;
    public double         survivalFlyVL            = 0D;

    // Data shared between the fly checks -----
    public int            bunnyhopDelay;
    public double         jumpAmplifier = 0;
    /** Last time the player was actually sprinting. */
    public long           timeSprinting = 0;
    public double         multSprinting = 1.30000002; // Multiplier at the last time sprinting.
    /**
     * Last valid y distance covered by a move. Integer.MAX_VALUE indicates "not set".
     */
    public double       lastYDist = Double.MAX_VALUE;
    /**
     * Last valid horizontal distance covered by a move. Integer.MAX_VALUE indicates "not set".
     */
    public double       lastHDist = Double.MAX_VALUE;
    /** Just used velocity, during processing of moving checks. */
    public SimpleEntry  verVelUsed = null;
    /** Compatibility entry for bouncing of slime blocks and the like. */
    public SimpleEntry verticalBounce = null;

    /** Tick at which walk/fly speeds got changed last time. */
    public int speedTick = 0;
    public float walkSpeed = 0.0f;
    public float flySpeed = 0.0f;

    // Velocity handling.
    /** Vertical velocity modeled as an axis (positive and negative possible) */
    private final SimpleAxisVelocity verVel = new SimpleAxisVelocity();

    /** Horizontal velocity modeled as an axis (always positive) */
    private final FrictionAxisVelocity horVel = new FrictionAxisVelocity();

    // Coordinates.
    /** Last from coordinates. X is at Double.MAX_VALUE, if not set. */
    public double         fromX = Double.MAX_VALUE, fromY, fromZ;
    /** Last to coordinates. X is at Double.MAX_VALUE, if not set. */
    public double         toX = Double.MAX_VALUE, toY, toZ;
    /** Last to looking direction. Yaw is at Float.MAX_VALUE if not set. */
    public float          toYaw = Float.MAX_VALUE, toPitch ;
    /** Moving trace (to-positions, use tick as time). This is initialized on "playerJoins, i.e. MONITOR, and set to null on playerLeaves." */
    private LocationTrace trace = null; 

    // sf rather
    /** To/from was ground or web or assumed to be etc. */
    public boolean		  toWasReset, fromWasReset;
    /** Basic envelope constraints for switching into air. */
    public LiftOffEnvelope liftOffEnvelope = defaultLiftOffEnvelope;
    /** Count how many moves have been made inside a medium (other than air). */
    public int insideMediumCount = 0;

    // Locations shared between all checks.
    private Location    setBack = null;
    private Location    teleported = null;

    // Check specific data -----

    // Data of the creative check.
    public boolean        creativeFlyPreviousRefused;

    // Data of the more packets check.
    /** Packet frequency count. */
    public final ActionFrequency morePacketsFreq;
    /** Burst count. */
    public final ActionFrequency morePacketsBurstFreq;
    private Location      morePacketsSetback = null;

    // Data of the more packets vehicle check.
    public int            morePacketsVehicleBuffer = 50;
    public long           morePacketsVehicleLastTime;
    private Location      morePacketsVehicleSetback = null;
    /** Task id of the morepackets set-back task. */ 
    public int			  morePacketsVehicleTaskId = -1;


    // Data of the no fall check.
    public float          noFallFallDistance = 0;
    /** Last y coordinate from when the player was on ground. */
    public double         noFallMaxY = 0;
    /** Indicate that NoFall should assume the player to be on ground. */
    public boolean noFallAssumeGround = false;
    /** Indicate that NoFall is not to use next damage event for checking on-ground properties. */ 
    public boolean noFallSkipAirCheck = false;
    // Passable check.
    public double 	      passableVL;

    // Data of the survival fly check.
    public double       sfHorizontalBuffer = 0.0; // ineffective: SurvivalFly.hBufMax / 2.0;
    /** Event-counter to cover up for sprinting resetting server side only. Set in the FighListener. */
    public int          lostSprintCount = 0;
    public int          sfJumpPhase = 0;
    /** Count how many times in a row v-dist has been zero, at very low h-dist (aimed at in-air checks). */
    public int          sfZeroVdist = 0;
    /** Only used during processing, to keep track of sub-checks using velocity. Reset in velocityTick, before checks run. */

    /** "Dirty" flag, for receiving velocity and similar while in air. */
    private boolean     sfDirty = false;

    /** Indicate low jumping descending phase (likely cheating). */
    public boolean sfLowJump = false;
    public boolean sfNoLowJump = false; // Hacks.

    /** Counting while the player is not on ground and not moving. A value <0 means not hovering at all. */
    public int 			sfHoverTicks = -1;
    /** First count these down before incrementing sfHoverTicks. Set on join, if configured so. */
    public int 			sfHoverLoginTicks = 0;
    public int			sfOnIce = 0;
    public long			sfCobwebTime = 0;
    public double		sfCobwebVL = 0;
    public long			sfVLTime = 0;

    // Accounting info.
    public final ActionAccumulator vDistAcc = new ActionAccumulator(3, 3);
    /** Rough friction factor estimate, 0.0 is the reset value (maximum with lift-off/burst speed is used). */
    public double lastFrictionHorizontal = 0.0;
    /** Rough friction factor estimate, 0.0 is the reset value (maximum with lift-off/burst speed is used). */
    public double lastFrictionVertical = 0.0;
    /** Used during processing, no resetting necessary.*/
    public double nextFrictionHorizontal = 0.0;
    /** Used during processing, no resetting necessary.*/
    public double nextFrictionVertical= 0.0;


    // HOT FIX
    /** Inconsistency-flag. Set on moving inside of vehicles, reset on exiting properly. Workaround for VehicleLeaveEvent missing. */ 
    public boolean wasInVehicle = false;
    public MoveConsistency vehicleConsistency = MoveConsistency.INCONSISTENT;
    /** Set to true after login/respawn, only if the set-back is reset there. Reset in MovingListener after handling PlayerMoveEvent */
    public boolean joinOrRespawn = false;

    public MovingData(final MovingConfig config) {
        super(config);
        morePacketsFreq = new ActionFrequency(config.morePacketsEPSBuckets, 500);
        morePacketsBurstFreq = new ActionFrequency(12, 5000);
    }

    /**
     * Clear the data of the fly checks (not more-packets).
     */
    public void clearFlyData() {
        bunnyhopDelay = 0;
        sfJumpPhase = 0;
        jumpAmplifier = 0;
        setBack = null;
        lastYDist = lastHDist = Double.MAX_VALUE;
        sfZeroVdist = 0;
        fromX = toX = Double.MAX_VALUE;
        toYaw = Float.MAX_VALUE;
        clearAccounting();
        clearNoFallData();
        removeAllVelocity();
        sfHorizontalBuffer = 0.0;
        lostSprintCount = 0;
        toWasReset = fromWasReset = false; // TODO: true maybe
        sfHoverTicks = sfHoverLoginTicks = -1;
        sfDirty = false;
        sfLowJump = false;
        liftOffEnvelope = defaultLiftOffEnvelope;
        insideMediumCount = 0;
        vehicleConsistency = MoveConsistency.INCONSISTENT;
        lastFrictionHorizontal = lastFrictionVertical = 0.0;
        verVelUsed = null;
        verticalBounce = null;
    }

    /**
     * Teleport event: Mildly reset the flying data without losing any important information.
     * 
     * @param setBack
     */
    public void onSetBack(final Location setBack) {
        // Reset positions
        resetPositions(teleported);
        // NOTE: Do mind that the reference is used directly for set-backs, should stay consistent, though.

        setSetBack(teleported);
        this.morePacketsSetback = this.morePacketsVehicleSetback = null; // TODO: or set.

        clearAccounting(); // Might be more safe to do this.
        // Keep no-fall data.
        // Fly data: problem is we don't remember the settings for the set back location.
        // Assume the player to start falling from there rather, or be on ground.
        // TODO: Check if to adjust some counters to state before setback? 
        // Keep jump amplifier
        // Keep bunny-hop delay (?)
        // keep jump phase.
        sfHorizontalBuffer = 0.0;
        lostSprintCount = 0;
        toWasReset = fromWasReset = false; // TODO: true maybe
        sfHoverTicks = -1; // 0 ?
        sfDirty = false;
        sfLowJump = false;
        liftOffEnvelope = defaultLiftOffEnvelope;
        insideMediumCount = 0;
        removeAllVelocity();
        vehicleConsistency = MoveConsistency.INCONSISTENT; // Not entirely sure here.
        lastFrictionHorizontal = lastFrictionVertical = 0.0;
        verticalBounce = null;
    }

    /**
     * Move event: Mildly reset some data, prepare setting a new to-Location.
     */
    public void prepareSetBack(final Location loc) {
        clearAccounting();
        sfJumpPhase = 0;
        lastYDist = lastHDist = Double.MAX_VALUE;
        sfZeroVdist = 0;
        toWasReset = false;
        fromWasReset = false;
        verticalBounce = null;
        // Remember where we send the player to.
        setTeleported(loc);
        // TODO: sfHoverTicks ?
    }

    /**
     * Adjust properties that relate to the mediu, called on set back and
     * similar. <br>
     * Currently: liftOffEnvelope, nextFriction.
     * 
     * @param loc
     */
    public void adjustMediumProperties(final PlayerLocation loc) {
        // Simplified.
        if (loc.isInWeb()) {
            liftOffEnvelope = LiftOffEnvelope.NO_JUMP;
            nextFrictionHorizontal = nextFrictionVertical = 0.0;
        }
        else if (loc.isInLiquid()) {
            // TODO: Distinguish strong limit.
            liftOffEnvelope = LiftOffEnvelope.LIMIT_LIQUID;
            if (loc.isInLava()) {
                nextFrictionHorizontal = nextFrictionVertical = SurvivalFly.FRICTION_MEDIUM_LAVA;
            } else {
                nextFrictionHorizontal = nextFrictionVertical = SurvivalFly.FRICTION_MEDIUM_WATER;
            }
        }
        else if (loc.isOnGround()) {
            liftOffEnvelope = LiftOffEnvelope.NORMAL;
            nextFrictionHorizontal = nextFrictionVertical = SurvivalFly.FRICTION_MEDIUM_AIR;
        }
        else {
            liftOffEnvelope = LiftOffEnvelope.UNKNOWN;
            nextFrictionHorizontal = nextFrictionVertical = SurvivalFly.FRICTION_MEDIUM_AIR;
        }
        insideMediumCount = 0;
    }

    /**
     * Called when a player leaves the server.
     */
    public void onPlayerLeave() {
        removeAllVelocity();
        deleteTrace();
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
        if (morePacketsSetback != null && worldName.equalsIgnoreCase(morePacketsSetback.getWorld().getName()) || morePacketsVehicleSetback != null && worldName.equalsIgnoreCase(morePacketsVehicleSetback.getWorld().getName())) {
            clearMorePacketsData();
            clearNoFallData(); // just in case.
        }
    }

    /**
     * Just reset the "last locations" references.
     * @param x
     * @param y
     * @param z
     */
    public void resetPositions(final double x, final double y, final double z, final float yaw, final float pitch) {
        fromX = toX = x;
        fromY = toY = y;
        fromZ = toZ = z;
        toYaw = yaw;
        toPitch = pitch;
        lastYDist = lastHDist = Double.MAX_VALUE;
        sfZeroVdist = 0;
        sfDirty = false;
        sfLowJump = false;
        liftOffEnvelope = defaultLiftOffEnvelope;
        insideMediumCount = 0;
        lastFrictionHorizontal = lastFrictionVertical = 0.0;
        verticalBounce = null;
        // TODO: other buffers ?
        // No reset of vehicleConsistency.
    }

    /**
     * Just reset the "last locations" references.
     * @param loc
     */
    public void resetPositions(PlayerLocation loc) {
        if (loc == null) {
            resetPositions();
        }
        else {
            resetPositions(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        }
    }

    /**
     * Just reset the "last locations" references.
     * @param loc
     */
    public void resetPositions(final Location loc) {
        if (loc == null) {
            resetPositions();
        }
        else {
            resetPositions(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        }
    }

    /**
     * Reset the "last locations" to "not set".
     */
    public void resetPositions() {
        resetPositions(Double.MAX_VALUE, 0.0, 0.0, Float.MAX_VALUE, 0f);
    }

    public void resetLastDistances() {
        lastHDist = lastYDist = Double.MAX_VALUE;
    }

    /**
     * Set positions according to a move (just to and from).
     * @param from
     * @param to
     */
    public void setPositions(final Location from, final Location to) {
        fromX = from.getX();
        fromY = from.getY();
        fromZ = from.getZ();
        toX = to.getX();
        toY = to.getY();
        toZ = to.getZ();
        toYaw = to.getYaw();
        toPitch = to.getPitch();
    }

    /**
     * Clear accounting data.
     */
    public void clearAccounting() {
        vDistAcc.clear();
    }

    /**
     * Clear the data of the more packets checks.
     */
    public void clearMorePacketsData() {
        morePacketsSetback = null;
        morePacketsVehicleSetback = null;
        // TODO: Also reset other data ?
    }

    /**
     * Clear the data of the new fall check.
     */
    public void clearNoFallData() {
        noFallFallDistance = 0;
        noFallMaxY = 0D;
        noFallSkipAirCheck = false;
    }

    /**
     * Set the set-back location, this will also adjust the y-coordinate for some block types (at least air).
     * @param loc
     */
    public void setSetBack(final PlayerLocation loc) {
        if (setBack == null) {
            setBack = loc.getLocation();
        }
        else{
            LocUtil.set(setBack, loc);
        }
        // TODO: Consider adjusting the set-back-y here. Problem: Need to take into account for bounding box (collect max-ground-height needed).
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
    }

    /**
     * Get the set-back location with yaw and pitch set form ref.
     * @param ref
     * @return
     */
    public Location getSetBack(final Location ref) {
        return LocUtil.clone(setBack, ref);
    }

    /**
     * Get the set-back location with yaw and pitch set from ref.
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
     * Set teleport-to location to recognize NCP set-backs. This copies the coordinates and world.
     * @param loc
     */
    public final void setTeleported(final Location loc) {
        teleported = LocUtil.clone(loc); // Always overwrite.
    }

    public boolean hasMorePacketsSetBack() {
        return morePacketsSetback != null;
    }

    public final void setMorePacketsSetBack(final PlayerLocation loc) {
        if (morePacketsSetback == null) {
            morePacketsSetback = loc.getLocation();
        }
        else {
            LocUtil.set(morePacketsSetback, loc);
        }
    }

    public final void setMorePacketsSetBack(final Location loc) {
        if (morePacketsSetback == null) {
            morePacketsSetback = LocUtil.clone(loc);
        }
        else {
            LocUtil.set(morePacketsSetback, loc);
        }
    }

    public Location getMorePacketsSetBack() {
        return LocUtil.clone(morePacketsSetback);
    }

    public boolean hasMorePacketsVehicleSetBack() {
        return morePacketsVehicleSetback != null;
    }

    public final void setMorePacketsVehicleSetBack(final PlayerLocation loc) {
        if (morePacketsVehicleSetback == null) {
            morePacketsVehicleSetback = loc.getLocation();
        }
        else {
            LocUtil.set(morePacketsVehicleSetback, loc);
        }
    }

    public final void setMorePacketsVehicleSetBack(final Location loc) {
        if (morePacketsVehicleSetback == null) {
            morePacketsVehicleSetback = LocUtil.clone(loc);
        }
        else {
            LocUtil.set(morePacketsVehicleSetback, loc);
        }
    }

    public final Location getMorePacketsVehicleSetBack() {
        return LocUtil.clone(morePacketsVehicleSetback);
    }

    public final void resetTeleported() {
        teleported = null;
    }

    /**
     * Set set-back location to null.
     */
    public final void resetSetBack() {
        setBack = null;
    }

    /**
     * Just set the last "to-coordinates", no world check.
     * @param to
     */
    public final void setTo(final Location to) {
        toX = to.getX();
        toY = to.getY();
        toZ = to.getZ();
        toYaw = to.getYaw();
        toPitch = to.getPitch();
    }

    /**
     * Add velocity to internal book-keeping.
     * @param player
     * @param data
     * @param cc
     * @param vx
     * @param vy
     * @param vz
     */
    public void addVelocity(final Player player, final MovingConfig cc, final double vx, final double vy, final double vz) {

        final int tick = TickTask.getTick();
        // TODO: Slightly odd to call this each time, might switch to a counter-strategy (move - remove). 
        removeInvalidVelocity(tick  - cc.velocityActivationTicks);

        if (debug) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " new velocity: " + vx + ", " + vy + ", " + vz);
        }

        // Always add vertical velocity.
        verVel.add(new SimpleEntry(tick, vy, cc.velocityActivationCounter));

        // TODO: Should also switch to adding always.
        if (vx != 0.0 || vz != 0.0) {
            final double newVal = Math.sqrt(vx * vx + vz * vz);
            horVel.add(new AccountEntry(tick, newVal, cc.velocityActivationCounter, Math.max(20,  1 + (int) Math.round(newVal * 10.0))));
        }

        // Set dirty flag here.
        sfDirty = true; // TODO: Set on using the velocity, due to latency !
        sfNoLowJump = true; // TODO: Set on using the velocity, due to latency !

    }

    public void prependVerticalVelocity(final SimpleEntry entry) {
        verVel.addToFront(entry);
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
     * Remove all vertical and horizontal velocity.
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
     * Active or queued.
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

        // Reset the "just used" velocity.
        verVelUsed = null;
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
            verVelUsed = available;
            sfDirty = true;
            // TODO: Consider sfNoLowJump = true;
        }
        return available;
    }

    /**
     * Check the verVelUsed field and return that if appropriate. Otherwise
     * call useVerticalVelocity(amount). 
     * 
     * @param amount
     * @return
     */
    public SimpleEntry getOrUseVerticalVelocity(final double amount) {
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
        if (trace == null) {
            final MovingConfig cc = MovingConfig.getConfig(player);
            trace = new LocationTrace(cc.traceSize, cc.traceMergeDist);
        }
        return trace;
    }

    /**
     * Convenience
     * @param player
     * @param loc
     */
    public void resetTrace(final Player player, final Location loc, final long time) {
        final MovingConfig cc = MovingConfig.getConfig(player);
        resetTrace(loc, time, cc.traceSize, cc.traceMergeDist);
    }

    /**
     * Convenience method to add a location to the trace, creates the trace if necessary.
     * @param player
     * @param loc
     * @param time
     * @return Updated LocationTrace instance, for convenient use, without sticking too much to MovingData.
     */
    public LocationTrace updateTrace(final Player player, final Location loc, final long time) {
        final LocationTrace trace = getTrace(player);
        trace.addEntry(time, loc.getX(), loc.getY(), loc.getZ());
        return trace;
    }

    /**
     * Convenience: Create or just reset the trace, add the current location.
     * @param loc 
     * @param size
     * @param mergeDist
     * @param traceMergeDist 
     */
    public void resetTrace(final Location loc, final long time, final int size, double mergeDist) {
        if (trace == null || trace.getMaxSize() != size || trace.getMergeDist() != mergeDist) {
            trace = new LocationTrace(size, mergeDist);
        } else {
            trace.reset();
        }
        trace.addEntry(time, loc.getX(), loc.getY(), loc.getZ());
    }

    public void deleteTrace() {
        trace = null;
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

}
