package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/*
 * M"""""`'"""`YM                   oo                   M""""""'YMM            dP            
 * M  mm.  mm.  M                                        M  mmmm. `M            88            
 * M  MMM  MMM  M .d8888b. dP   .dP dP 88d888b. .d8888b. M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMM  MMM  M 88'  `88 88   d8' 88 88'  `88 88'  `88 M  MMMMM  M 88'  `88   88   88'  `88 
 * M  MMM  MMM  M 88.  .88 88 .88'  88 88    88 88.  .88 M  MMMM' .M 88.  .88   88   88.  .88 
 * M  MMM  MMM  M `88888P' 8888P'   dP dP    dP `8888P88 M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMMMMM                                    .88 MMMMMMMMMMM                          
 *                                               d8888P                                       
 */
/**
 * Player specific data for the moving checks.
 */
public class MovingData extends ACheckData {
	
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
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new MovingData());
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
		return playersMap.remove(playerName);
	}
    
    public static void clear(){
    	playersMap.clear();
    }
    
    /**
     * Assume the player has to move on ground or so to lift off. TODO: Test, might be better ground.
     */
    private static final MediumLiftOff defaultMediumLiftOff = MediumLiftOff.LIMIT_JUMP;
    
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
    
    // Velocity handling.
    // TODO: consider resetting these with clearFlyData and onSetBack.
    public int            verticalVelocityCounter;
    public double         verticalFreedom;
    public double         verticalVelocity;
    public int 		      verticalVelocityUsed = 0;
    /** Active velocity entries (horizontal distance). */
    public final List<Velocity> hVelActive = new LinkedList<Velocity>();
    /** Queued velocity entries (horizontal distance). */
    public final List<Velocity> hVelQueued = new LinkedList<Velocity>(); 
//    public int            horizontalVelocityCounter;
//    public double         horizontalFreedom;
//    public int 			  horizontalVelocityUsed = 0;
    
    // Coordinates.
    /** Last from coordinates. */
    public double         fromX = Double.MAX_VALUE, fromY, fromZ;
    /** Last to coordinates. */
    public double 		  toX = Double.MAX_VALUE, toY, toZ;
    
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
    public int            morePacketsBuffer        = 50;
    public long           morePacketsLastTime;
    public int            morePacketsPackets;
    private Location      morePacketsSetback = null;

    // Data of the more packets vehicle check.
    public int            morePacketsVehicleBuffer = 50;
    public long           morePacketsVehicleLastTime;
    public int            morePacketsVehiclePackets;
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
	public double 		sfHorizontalBuffer = 0;
	/** Times to add extra horizontal buffer if necessary. */
	public int			sfHBufExtra = 0;
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
	/** Counting while the player is not on ground and not moving. A value <0 means not hovering at all. */
	public int 			sfHoverTicks = -1;
	/** First count these down before incrementing sfHoverTicks. Set on join, if configured so. */
	public int 			sfHoverLoginTicks = 0;
	public int			sfFlyOnIce = 0;
	public long			sfCobwebTime = 0;
	public double		sfCobwebVL = 0;
	public long			sfVLTime = 0;
    
    // Accounting info.
	public final ActionAccumulator vDistAcc = new ActionAccumulator(3, 3);
	
	
	// HOT FIX
	/** Inconsistency-flag. Set on moving inside of vehicles, reset on exiting properly. Workaround for VehicleLeaveEvent missing. */ 
	public boolean wasInVehicle = false;
	
	
//	public final Stats stats = new Stats(); // Test.
    
	/**
	 * Clear the data of the fly checks (not more-packets).
	 */
	public void clearFlyData() {
		bunnyhopDelay = 0;
		sfJumpPhase = 0;
		jumpAmplifier = 0;
		setBack = null;
		sfLastYDist = Double.MAX_VALUE;
		fromX = toX = Double.MAX_VALUE;
		clearAccounting();
		clearNoFallData();
		removeAllVelocity();
		sfHorizontalBuffer = 0;
		sfHBufExtra = 0;
		toWasReset = fromWasReset = false; // TODO: true maybe
		sfHoverTicks = sfHoverLoginTicks = -1;
		sfDirty = false;
		sfLowJump = false;
		mediumLiftOff = defaultMediumLiftOff;
	}

	/**
	 * Mildly reset the flying data without losing any important information.
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
		sfHorizontalBuffer = Math.min(0, sfHorizontalBuffer);
		sfHBufExtra = 0;
		toWasReset = fromWasReset = false; // TODO: true maybe
		sfHoverTicks = -1;
		sfDirty = false;
		sfLowJump = false;
		mediumLiftOff = defaultMediumLiftOff;
		removeAllVelocity();
	}
	
    /**
     * Just reset the "last locations" references.
     * @param loc
     */
    public void resetPositions(final Location loc){
        if (loc == null) resetPositions(Double.MAX_VALUE, 0, 0);
        else resetPositions(loc.getX(), loc.getY(), loc.getZ());
    }
    
    /**
     * Just reset the "last locations" references.
     * @param loc
     */
	public void resetPositions(PlayerLocation loc) {
		if (loc == null) resetPositions(Double.MAX_VALUE, 0, 0);
        else resetPositions(loc.getX(), loc.getY(), loc.getZ());
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
        sfLastYDist = Double.MAX_VALUE;
        sfDirty = false;
        sfLowJump = false;
        mediumLiftOff = defaultMediumLiftOff;
        // TODO: other buffers ?
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
    public void setSetBack(final PlayerLocation loc){
    	if (setBack == null){
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
    public void setSetBack(final Location loc){
    	if (setBack == null){
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
    public Location getSetBack(final Location ref){
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
		if (setBack == null) return true;
		else return setBack.getWorld().equals(loc.getWorld());
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
	
	public final Location getTeleported(){
		// TODO: here a reference might do.
		return teleported == null ? teleported : LocUtil.clone(teleported);
	}
	
	public final void setTeleported(final Location loc) {
		teleported = LocUtil.clone(loc); // Always overwrite.
	}

	public boolean hasMorePacketsSetBack() {
		return morePacketsSetback != null;
	}

	public final void setMorePacketsSetBack(final PlayerLocation loc) {
		if (morePacketsSetback == null) morePacketsSetback = loc.getLocation();
		else LocUtil.set(morePacketsSetback, loc);
	}
	
	public final void setMorePacketsSetBack(final Location loc) {
		if (morePacketsSetback == null) morePacketsSetback = LocUtil.clone(loc);
		else LocUtil.set(morePacketsSetback, loc);
	}

	public Location getMorePacketsSetBack() {
		return LocUtil.clone(morePacketsSetback);
	}
	
	public boolean hasMorePacketsVehicleSetBack() {
		return morePacketsVehicleSetback != null;
	}

	public final void setMorePacketsVehicleSetBack(final PlayerLocation loc) {
		if (morePacketsVehicleSetback == null) morePacketsVehicleSetback = loc.getLocation();
		else LocUtil.set(morePacketsVehicleSetback, loc);
	}
	
	public final void setMorePacketsVehicleSetBack(final Location loc) {
		if (morePacketsVehicleSetback == null) morePacketsVehicleSetback = LocUtil.clone(loc);
		else LocUtil.set(morePacketsVehicleSetback, loc);
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
		// TODO: Might merge entries !
		hVelQueued.add(vel);
	}
	
	/**
	 * Currently only applies to horizontal velocity.
	 */
	public void removeAllVelocity(){
		hVelActive.clear();
		hVelQueued.clear();
	}

	/**
	 * Remove all velocity entries that are invalid. Checks both active and queued.
	 * <br>(This does not catch invalidation by speed / direction changing.)
	 * @param tick All velocity added before this tick gets removed.
	 */
	public void removeInvalidVelocity(final int tick) {
		// TODO: Also merge entries here, or just on adding?
		Iterator<Velocity> it;
		// Active.
		it = hVelActive.iterator();
		while (it.hasNext()){
			final Velocity vel = it.next();
			// TODO: 0.001 can be stretched somewhere else, most likely...
			// TODO: Somehow use tick here too (actCount, valCount)?
			if (vel.valCount <= 0 || vel.value <= 0.001){
//				System.out.prsintln("Invalidate active: " + vel);
				it.remove();
			}
		}
		// Queued.
		it = hVelQueued.iterator();
		while (it.hasNext()){
			final Velocity vel = it.next();
			if (vel.actCount <= 0 || vel.tick < tick){
//				System.out.println("Invalidate queued: " + vel);
				it.remove();
			}
		}
	}
	
	/**
	 * Called for moving events, increase age of velocity.
	 */
	public void velocityTick(){
		// Decrease counts for active.
		// TODO: Consider removing already invalidated here.
		for (final Velocity vel : hVelActive){
			vel.valCount --;
			vel.sum += vel.value;
			vel.value *= 0.93; // TODO: Actual friction.
			// (Altered entries should be kept, since they get used right away.)
		}
		// Decrease counts for queued.
		final Iterator<Velocity> it = hVelQueued.iterator();
		while (it.hasNext()){
			it.next().actCount --;
		}
	}

	/**
	 * Get effective amount of all used velocity.
	 * @return
	 */
	public double getHorizontalFreedom() {
		// TODO: model/calculate it as accurate as possible...
		double f = 0;
		for (final Velocity vel : hVelActive){
			f += vel.value;
		}
		return f;
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
		final Iterator<Velocity> it = hVelQueued.iterator();
		double used = 0;
		while (it.hasNext()){
			final Velocity vel = it.next();
			used += vel.value;
			hVelActive.add(vel);
			it.remove();
			if (used >= amount){
				break;
			}
		}
		// TODO: Add to sum.
		return used;
	}

	/**
	 * Test if the location is the same, ignoring pitch and yaw.
	 * @param loc
	 * @return
	 */
	public boolean isSetBack(final Location loc) {
		if (loc == null || setBack == null){
			return false;
		}
		if (!loc.getWorld().getName().equals(setBack.getWorld().getName())){
			return false;
		}
		return loc.getX() == setBack.getX() && loc.getY() == setBack.getY() && loc.getZ() == setBack.getZ();
	}
	
}
