package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.utilities.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Player specific data for the moving checks.
 */
public class MovingData extends ACheckData {

    /**
     * Assume the player has to move on ground or so to lift off. TODO: Test, might be better ground.
     */
    private static final MediumLiftOff defaultMediumLiftOff = MediumLiftOff.LIMIT_JUMP;

    //	private static final long IGNORE_SETBACK_Y = BlockProperties.F_SOLID | BlockProperties.F_GROUND | BlockProperties.F_CLIMBABLE | BlockProperties.F_LIQUID;

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
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static MovingData getData(final Player player) {
        // Note that the trace might be null after just calling this.
        MovingData data = playersMap.get(player.getName());
        if (data == null) {
            data = new MovingData(ConfigManager.getConfigFile(player.getWorld().getName()));
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
    public double         jumpAmplifier;
    /** Last time the player was actually sprinting. */
    public long			  timeSprinting = 0;

    /** Tick at which walk/fly speeds got changed last time. */
    public int speedTick = 0;
    public float walkSpeed = 0.0f;
    public float flySpeed = 0.0f;

    // Velocity handling.
    // TODO: consider resetting these with clearFlyData and onSetBack.
    public int            verticalVelocityCounter;
    public double         verticalFreedom;
    public double         verticalVelocity;
    public int 		      verticalVelocityUsed = 0;
    /** Horizontal velocity modeled as an axis (always positive) */
    private final AxisVelocity hVel = new AxisVelocity();

    // Coordinates.
    /** Last from coordinates. */
    public double         fromX = Double.MAX_VALUE, fromY, fromZ;
    /** Last to coordinates. */
    public double 		  toX = Double.MAX_VALUE, toY, toZ;
    /** Moving trace (to positions). This is initialized on "playerJoins, i.e. MONITOR, and set to null on playerLeaves."*/
    private LocationTrace trace = null; 

    // sf rather
    /** To/from was ground or web or assumed to be etc. */
    public boolean		  toWasReset, fromWasReset;
    public MediumLiftOff  mediumLiftOff = defaultMediumLiftOff;

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
    public double 		sfHorizontalBuffer = 0.0; // ineffective: SurvivalFly.hBufMax / 2.0;
    /** Event-counter to cover up for sprinting resetting server side only. Set in the FighListener. */
    public int			lostSprintCount = 0;
    public int 			sfJumpPhase = 0;
    /** "Dirty" flag, for receiving velocity and similar while in air. */
    public boolean      sfDirty = false;

    /** Indicate low jumping descending phase (likely cheating). */
    public boolean sfLowJump = false;
    public boolean sfNoLowJump = false; // Hacks.

    /**
     * Last valid y distance covered by a move. Integer.MAX_VALUE indicates "not set".
     */
    public double		sfLastYDist = Double.MAX_VALUE;
    public double		sfLastHDist = Double.MAX_VALUE;
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


    // HOT FIX
    /** Inconsistency-flag. Set on moving inside of vehicles, reset on exiting properly. Workaround for VehicleLeaveEvent missing. */ 
    public boolean wasInVehicle = false;
    public MoveConsistency vehicleConsistency = MoveConsistency.INCONSISTENT;

    public MovingData(final ConfigFile config) {
        // TODO: Parameters from cc.
        final int nob = 2 * Math.max(1, Math.min(60, config.getInt(ConfPaths.MOVING_MOREPACKETS_SECONDS)));
        morePacketsFreq = new ActionFrequency(nob, 500);
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
        sfLastYDist = sfLastHDist = Double.MAX_VALUE;
        fromX = toX = Double.MAX_VALUE;
        clearAccounting();
        clearNoFallData();
        removeAllVelocity();
        sfHorizontalBuffer = 0.0;
        lostSprintCount = 0;
        toWasReset = fromWasReset = false; // TODO: true maybe
        sfHoverTicks = sfHoverLoginTicks = -1;
        sfDirty = false;
        sfLowJump = false;
        mediumLiftOff = defaultMediumLiftOff;
        vehicleConsistency = MoveConsistency.INCONSISTENT;
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
        mediumLiftOff = defaultMediumLiftOff;
        removeAllVelocity();
        vehicleConsistency = MoveConsistency.INCONSISTENT; // Not entirely sure here.
    }

    /**
     * Move event: Mildly reset some data, prepare setting a new to-Location.
     */
    public void prepareSetBack(final Location loc) {
        clearAccounting();
        sfJumpPhase = 0;
        sfLastYDist = sfLastHDist = Double.MAX_VALUE;
        toWasReset = false;
        fromWasReset = false;
        // Remember where we send the player to.
        setTeleported(loc);
        // TODO: sfHoverTicks ?
    }

    /**
     * Just reset the "last locations" references.
     * @param loc
     */
    public void resetPositions(final Location loc) {
        if (loc == null) {
            resetPositions(Double.MAX_VALUE, 0, 0);
        }
        else {
            resetPositions(loc.getX(), loc.getY(), loc.getZ());
        }
    }

    /**
     * Just reset the "last locations" references.
     * @param loc
     */
    public void resetPositions(PlayerLocation loc) {
        if (loc == null) {
            resetPositions(Double.MAX_VALUE, 0, 0);
        }
        else {
            resetPositions(loc.getX(), loc.getY(), loc.getZ());
        }
    }

    /**
     * Just reset the "last locations" references.
     * @param x
     * @param y
     * @param z
     */
    public void resetPositions(final double x, final double y, final double z) {
        fromX = toX = x;
        fromY = toY = y;
        fromZ = toZ = z;
        sfLastYDist = sfLastHDist = Double.MAX_VALUE;
        sfDirty = false;
        sfLowJump = false;
        mediumLiftOff = defaultMediumLiftOff;
        // TODO: other buffers ?
        // No reset of vehicleConsistency.
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
    }

    /**
     * Add horizontal velocity (distance). <br>
     * Since velocity is seldom an access method should be better. Flying players are expensive anyway, so this should not matter too much.
     * @param vel
     */
    public void addHorizontalVelocity(final Velocity vel) {
        hVel.add(vel);
    }

    /**
     * Currently only applies to horizontal velocity.
     */
    public void removeAllVelocity() {
        hVel.clear();
    }

    /**
     * Remove all velocity entries that are invalid. Checks both active and queued.
     * <br>(This does not catch invalidation by speed / direction changing.)
     * @param tick All velocity added before this tick gets removed.
     */
    public void removeInvalidVelocity(final int tick) {
        hVel.removeInvalid(tick);
    }

    /**
     * Clear only active horizontal velocity.
     */
    public void clearActiveHVel() {
        hVel.clearActive();
    }

    public boolean hasActiveHVel() {
        return hVel.hasActive();
    }

    public boolean hasQueuedHVel() {
        return hVel.hasQueued();
    }

    /**
     * Called for moving events, increase age of velocity, decrease amounts, check which entries are invalid. Both horizontal and vertical.
     */
    public void velocityTick() {
        // Horizontal velocity (intermediate concept).
        hVel.tick();

        // Vertical velocity (old concept).
        if (verticalVelocity <= 0.09D) {
            verticalVelocityUsed ++;
            verticalVelocityCounter--;
        }
        else if (verticalVelocityCounter > 0) {
            verticalVelocityUsed ++;
            verticalFreedom += verticalVelocity;
            verticalVelocity = Math.max(0.0, verticalVelocity -0.09);
            // TODO: Consider using up counter ? / better use velocity entries / even better use x,y,z entries right away .
        } else if (verticalFreedom > 0.001D) {
            if (verticalVelocityUsed == 1 && verticalVelocity > 1.0) {
                // Workarounds.
                verticalVelocityUsed = 0;
                verticalVelocity = 0;
                verticalFreedom = 0;
            }
            else{
                // Counter has run out, now reduce the vertical freedom over time.
                verticalVelocityUsed ++;
                verticalFreedom *= 0.93D;
            }
        }
    }

    /**
     * Get effective amount of all used velocity. Non-destructive.
     * @return
     */
    public double getHorizontalFreedom() {
        return hVel.getFreedom();
    }

    /**
     * Use all queued velocity until at least amount is matched.
     * Amount is the horizontal distance that is to be covered by velocity (active has already been checked).
     * <br>
     * If the modeling changes (max instead of sum or similar), then this will be affected.
     * @param amount The amount used.
     * @return
     */
    public double useHorizontalVelocity(final double amount) {
        return hVel.use(amount);
    }

    /**
     * Debugging.
     * @param builder
     */
    public void addHorizontalVelocity(final StringBuilder builder) {
        if (hVel.hasActive()) {
            builder.append("\n" + " horizontal velocity (active):");
            hVel.addActive(builder);
        }
        if (hVel.hasQueued()) {
            builder.append("\n" + " horizontal velocity (queued):");
            hVel.AddQueued(builder);
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

}
