package fr.neatmonster.nocheatplus.checks.moving;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/*
 * MP""""""`MM                            oo                   dP MM""""""""`M dP          
 * M  mmmmm..M                                                 88 MM  mmmmmmmM 88          
 * M.      `YM dP    dP 88d888b. dP   .dP dP dP   .dP .d8888b. 88 M'      MMMM 88 dP    dP 
 * MMMMMMM.  M 88    88 88'  `88 88   d8' 88 88   d8' 88'  `88 88 MM  MMMMMMMM 88 88    88 
 * M. .MMM'  M 88.  .88 88       88 .88'  88 88 .88'  88.  .88 88 MM  MMMMMMMM 88 88.  .88 
 * Mb.     .dM `88888P' dP       8888P'   dP 8888P'   `88888P8 dP MM  MMMMMMMM dP `8888P88 
 * MMMMMMMMMMM                                                    MMMMMMMMMMMM         .88 
 *                                                                                 d8888P  
 */
/**
 * The counterpart to the CreativeFly check. People that are not allowed to fly get checked by this. It will try to
 * identify when they are jumping, check if they aren't jumping too high or far, check if they aren't moving too fast on
 * normal ground, while sprinting, sneaking, swimming, etc.
 */
public class SurvivalFly extends Check {
	
	// Mostly horizontal speeds
	public static final double sneakingSpeed 	= 0.13D;
	public static final double walkingSpeed 	= 0.22D;
	public static final double sprintingSpeed 	= 0.35D;
	
	public static final double blockingSpeed 	= 0.16D;
	public static final double swimmingSpeed    = 0.115D;
	public static final double webSpeed         = 0.105D; // TODO: walkingSpeed * 0.15D; <- does not work
	
	public static final double climbSpeed      = sprintingSpeed; // TODO.
	
	public static final double modIce			= 2.5D;
	
	/** Faster moving down stream (water mainly). */
	public static final double modDownStream      = 0.19 / swimmingSpeed;
	/** Bunny-hop delay. */
	private static final int   bunnyHopMax = 9;

	/** To join some tags with moving check violations. */
	private final ArrayList<String> tags = new ArrayList<String>(15);
	
	
	private final Set<String> reallySneaking = new HashSet<String>(30);
	
    /**
     * Instantiates a new survival fly check.
     */
    public SurvivalFly() {
        super(CheckType.MOVING_SURVIVALFLY);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     * @return the location
     */
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {
        final long now = System.currentTimeMillis();
        tags.clear();
        // A player is considered sprinting if the flag is set and if he has enough food level.
        final boolean sprinting = player.isSprinting() && player.getFoodLevel() > 5;
        
        // Set some flags:
        final boolean fromOnGround = from.isOnGround();
        final boolean toOnGround = to.isOnGround();
        
        // Calculate some distances.
        final double xDistance = to.getX() - from.getX();
        final double yDistance = to.getY() - from.getY();
        final double zDistance = to.getZ() - from.getZ();
        
        // TODO: Later switch to squared distances.
        final double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
        
        // If we don't have any setBack, choose the location the player comes from.
        if (!data.hasSetBack())
            data.setSetBack(from);

		final boolean resetFrom;
		
//		data.stats.addStats(data.stats.getId("sfCheck", true), 1);

		// "Lost ground" workaround.
		if (fromOnGround || from.isResetCond()) resetFrom = true;
		// TODO: Extra workarounds for toOnGround ?
		else{
			// TODO: Consider if (!resetTo) ?
			// Check lost-ground workarounds.
			boolean lostGround = false; // Just used for better overview.
			
			// TODO: Move more conditions here (!)
			// TODO: Confine by max y distance and max/min xz-distance?
			if (yDistance >= -0.5 && yDistance <= 0.52 + data.jumpAmplifier * 0.2){
				// "Mild" Ascending / descending.
				// Stairs.
				// TODO: More safety guards.
				if (from.isAboveStairs()) {
					applyLostGround(player, from, true, data, "stairs");
					lostGround = true;
				}
				// Descending.
				if (!lostGround && yDistance <= 0){
					if (lostGroundDescend(player, from, to, hDistance, yDistance, sprinting, data, cc)){
						lostGround = true;	
					}
				}
				//Ascending
				if (!lostGround && yDistance >= 0){
					if (lostGroundAscend(player, from, to, hDistance, yDistance, sprinting, data, cc)){
						lostGround = true;
					}
				}
			}
			else if (yDistance < -0.5){
				// Clearly descending.
				if (hDistance <= 0.5){
					if (lostGroundFastDescend(player, from, to, hDistance, yDistance, sprinting, data, cc)){
						lostGround = true;
					}
				}
			}
//			data.stats.addStats(data.stats.getId("sfLostGround", true), lostGround ? 1 : 0);
			resetFrom = lostGround;
			// Note: if not setting resetFrom, other places have to check assumeGround...
		}
		
		// Set flag for swimming with the flowing direction of liquid.
		final boolean downStream = hDistance > swimmingSpeed && from.isInLiquid() && from.isDownStream(xDistance, zDistance);

		// TODO: Account for lift-off medium / if in air [i.e. account for medium + friction]?
		// (Might set some margin for buffering if cutting down hAllowedDistance.)
		double hAllowedDistance = getAllowedhDist(player, from, to, sprinting, downStream, hDistance, data, cc, false);
		
        // Judge if horizontal speed is above limit.
//        double hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom;
        double hDistanceAboveLimit = hDistance - hAllowedDistance;
        double hFreedom = 0; // Horizontal velocity used (!).
		if (hDistanceAboveLimit > 0){
			// Check extra buffer (!).
			final double extraUsed;
			if (data.sfHBufExtra > 0){
				extraUsed = 0.11;
				hDistanceAboveLimit = Math.max(0.0, hDistanceAboveLimit - extraUsed);
				data.sfHBufExtra --;
				tags.add("hbufextra");
				if (data.sfHBufExtra < 3 && to.isOnGround() || to.isResetCond()){
					data.sfHBufExtra = 0;
				}
			}
			else{
				extraUsed = 0.0;
			}
			// Check velocity.
			if (hDistanceAboveLimit > 0){
				hFreedom = data.getHorizontalFreedom();
				if (hFreedom < hDistanceAboveLimit){
					// Use queued velocity if possible.
					hFreedom += data.useHorizontalVelocity(hDistanceAboveLimit - hFreedom);
				}
				if (hFreedom > 0.0){
					hDistanceAboveLimit = Math.max(0.0, hDistanceAboveLimit - hFreedom);
				}
			}
			else{
//				System.out.println("*** Invalidate velocity on not used (extra)");
				data.hVelActive.clear(); // TODO: test/check !
				hFreedom = 0;
			}
			// TODO: Use velocity already here ?
			// After failure permission checks ( + speed modifier + sneaking + blocking + speeding) and velocity (!).
			if (hDistanceAboveLimit > 0){
				hAllowedDistance = getAllowedhDist(player, from, to, sprinting, downStream, hDistance, data, cc, true);
//				hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom - extraUsed;
				if (hFreedom > 0){
					hDistanceAboveLimit = hDistance - hAllowedDistance - extraUsed - hFreedom;
				}
				else{
					hDistanceAboveLimit = hDistance - hAllowedDistance - extraUsed;
				}
				if (hAllowedDistance > 0){ // TODO: Fix !
					// (Horizontal buffer might still get used.)
					tags.add("hspeed");
				}
			}
		}
		else{
//			System.out.println("*** Invalidate velocity on not used (normal)");
			data.hVelActive.clear(); // TODO: test/check !
			data.sfHBufExtra = 0;
		}
		///////
		// Note: here the normal speed checks must be  finished.
		//////

		// Prevent players from walking on a liquid in a too simple way.
		// TODO: yDistance == 0D <- should there not be a tolerance +- or 0...x ?
		if (hDistanceAboveLimit <= 0D && hDistance > 0.1D && yDistance == 0D && BlockProperties.isLiquid(to.getTypeId()) && !toOnGround && to.getY() % 1D < 0.8D) {
			hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hDistance);
			tags.add("waterwalk");
		}

        // Prevent players from sprinting if they're moving backwards.
//        if (hDistanceAboveLimit <= 0D && sprinting && data.horizontalFreedom <= 0.001D) {
        if (hDistanceAboveLimit <= 0D && sprinting && data.hVelActive.isEmpty()) {
            final float yaw = from.getYaw();
            if (xDistance < 0D && zDistance > 0D && yaw > 180F && yaw < 270F || xDistance < 0D && zDistance < 0D
                    && yaw > 270F && yaw < 360F || xDistance > 0D && zDistance < 0D && yaw > 0F && yaw < 90F
                    || xDistance > 0D && zDistance > 0D && yaw > 90F && yaw < 180F){
            	// Assumes permission check to be the heaviest (might be mistaken).
            	if (!player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPRINTING)){
            		hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hDistance);
            		tags.add("sprintback"); // Might add it anyway.
            	}
            }
        }

		data.bunnyhopDelay--;
		// "Bunny-hop".
		if (hDistanceAboveLimit > 0 && sprinting){
			// Try to treat it as a the "bunny-hop" problem.
			// TODO: sharpen the pre-conditions (fromWasReset or distance to last on-ground position).
			if (data.bunnyhopDelay <= 0 && hDistanceAboveLimit > 0.05D && hDistanceAboveLimit < 0.28D) {
				data.bunnyhopDelay = bunnyHopMax;
				hDistanceAboveLimit = 0D; // TODO: maybe relate buffer use to this + sprinting ?
				tags.add("bunny"); // TODO: Which here...
			}
		}
		
		final boolean resetTo = toOnGround || to.isResetCond();

		// Horizontal buffer.
		if (hDistanceAboveLimit > 0D && data.sfHorizontalBuffer != 0D) {
			if (data.sfHorizontalBuffer > 0D) tags.add("hbufuse");
			else tags.add("hbufpen");
			// Try to consume the "buffer".
			hDistanceAboveLimit -= data.sfHorizontalBuffer;
			data.sfHorizontalBuffer = 0D;

			// Put back the "over-consumed" buffer.
			if (hDistanceAboveLimit < 0D) {
				data.sfHorizontalBuffer = -hDistanceAboveLimit;
			}
		} else if (hDistance != 0D){
			data.sfHorizontalBuffer = Math.min(1D, data.sfHorizontalBuffer - hDistanceAboveLimit);
		}
		
		// Account for "dirty"-flag (allow less for normal jumping).
		if (data.sfDirty){
			if (resetFrom || resetTo){
				// Not resetting for data.noFallAssumeOnGround, currently.
				data.sfDirty = false;
			}
			else{
				tags.add("dirty");
			}
		}

        // Calculate the vertical speed limit based on the current jump phase.
        double vAllowedDistance = 0, vDistanceAboveLimit = 0;
        if (from.isInWeb()){
        	// Very simple: force players to descend or stay.
         	vAllowedDistance = from.isOnGround() ? 0.1D : 0;
         	data.jumpAmplifier = 0; // TODO: later maybe fetch.
        	vDistanceAboveLimit = yDistance - vAllowedDistance;
        	if (cc.survivalFlyCobwebHack && vDistanceAboveLimit > 0 && hDistanceAboveLimit <= 0){
        		// TODO: Seemed fixed at first by CB/MC, but still does occur due to jumping. 
        		final Location silentSetBack = hackCobweb(player, data, to, now, vDistanceAboveLimit);
        		if (silentSetBack != null){
        			if (cc.debug) {
        				tags.add("silentsbcobweb");
        				outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
        			}
        			return silentSetBack;
        		}
        	}
        	if (vDistanceAboveLimit > 0) tags.add("vweb");
        }
        else if (data.verticalFreedom <= 0.001 && from.isOnClimbable()){
        	// Ladder types.
        	// TODO: bring in in-medium accounting.
//        	// TODO: make these extra checks to the jumpphase thing ?
//        	if (fromOnGround) vAllowedDistance = climbSpeed + 0.3;
//        	else vAllowedDistance = climbSpeed;
//        	vDistanceAboveLimit = Math.abs(yDistance) - vAllowedDistance;
//        	if (vDistanceAboveLimit > 0) tags.add("vclimb");
        	final double jumpHeight = 1.35 + (data.jumpAmplifier > 0 ? (0.6 + data.jumpAmplifier - 1.0) : 0.0);
        	// TODO: ladders are ground !
        	if (yDistance > climbSpeed && !from.isOnGround(jumpHeight, 0D, 0D, BlockProperties.F_CLIMBABLE)){
        		// Ignore ladders. TODO: Check for false positives...
        		tags.add("climbspeed");
        		vDistanceAboveLimit = Math.max(vDistanceAboveLimit, yDistance - climbSpeed);
        	}
        	if (yDistance > 0){
            	if (!fromOnGround && !toOnGround && !data.noFallAssumeGround){
            		// Check if player may climb up.
            		// (This does exclude ladders.)
            		if (!from.canClimbUp(jumpHeight)){
            			tags.add("climbup");
            			vDistanceAboveLimit = Math.max(vDistanceAboveLimit, yDistance);
            		}
            	}
        	}

        }
        else if (data.verticalFreedom <= 0.001 && from.isInLiquid() && (Math.abs(yDistance) > 0.2 || to.isInLiquid())){
        	// Swimming...
        	if (yDistance >= 0){
        		// This is more simple to test.
        		// TODO: Friction in water...
        		vAllowedDistance = swimmingSpeed + 0.02;
        		vDistanceAboveLimit = yDistance - vAllowedDistance;
        		if (vDistanceAboveLimit > 0){
        			// Check workarounds.
        			if (yDistance <= 0.5){
        				// TODO: mediumLiftOff: refine conditions (general) , to should be near water level.
        				if (data.mediumLiftOff == MediumLiftOff.GROUND && !BlockProperties.isLiquid(from.getTypeIdAbove()) || !to.isInLiquid() ||  (toOnGround || data.sfLastYDist - yDistance >= 0.010 || to.isAboveStairs())){
                    		vAllowedDistance = swimmingSpeed + 0.5;
                    		vDistanceAboveLimit = yDistance - vAllowedDistance;
        				}
            		}
        			
        			if (vDistanceAboveLimit > 0){
        				tags.add("swimup");
        			}
        		}
        	}
        	// TODO: This is more complex, depends on speed of diving into it.
//        	else{
//        		// TODO
//        		if (-yDistance > swimmingSpeed * modDownStream){
//        			if (data.sfLastYDist != Double.MAX_VALUE && yDistance - swimmingSpeed <= data.sfLastYDist){
//        				// Expect to get less as much as swimming speed (heuristic).
//        				vAllowedDistance = Math.abs(data.sfLastYDist) - swimmingSpeed; // Just for reference.
//        				vDistanceAboveLimit = -yDistance - swimmingSpeed; // TODO
//        				if (vDistanceAboveLimit > 0) tags.add("swimdown");
//        			}
//        			else{
//        				// Ignore.
//        				vDistanceAboveLimit = 0;
//        				vAllowedDistance = Math.abs(yDistance);
//        			}
//        		} else{
//        			// Ignore.
//    				vDistanceAboveLimit = 0;
//    				vAllowedDistance = Math.abs(yDistance);
//        		}
//        	}
        }
        else{
        	// Check traveled y-distance, orientation is air + jumping + velocity (as far as it gets).
        	// TODO: Can it be easily transformed to a more accurate max. absolute height? 
        	vAllowedDistance = 1.35D + data.verticalFreedom;
        	int maxJumpPhase;
            if (data.mediumLiftOff == MediumLiftOff.LIMIT_JUMP){
            	// TODO: In normal water this is 0. Could set higher for special cases only (needs efficient data + flags collection?).
            	maxJumpPhase = 3;
            	if (data.sfJumpPhase > 0) tags.add("limitjump");
            }
            else if (data.jumpAmplifier > 0){
                vAllowedDistance += 0.6 + data.jumpAmplifier - 1.0;
                maxJumpPhase = (int) (9 + (data.jumpAmplifier - 1.0) * 6);
            }
            else maxJumpPhase = 6;
            // TODO: consider tags for jumping as well (!).
            if (data.sfJumpPhase > maxJumpPhase && data.verticalVelocityCounter <= 0){
            	// Could use dirty flag here !
            	if (data.sfDirty || yDistance < 0 || resetFrom){
            		if (data.getSetBackY() > to.getY()){
                		if (data.sfJumpPhase > 2 * maxJumpPhase){
                			// Ignore it for falling.
                		}
                		else{
                			vAllowedDistance -= Math.max(0, (data.sfJumpPhase - maxJumpPhase) * 0.15D);
                		}
                	}
            		else{
                		// TODO: (This allows the one tick longer jump (resetTo)).
                		vAllowedDistance -= Math.max(0, (data.sfJumpPhase - maxJumpPhase) * 0.15D);
            		}
            	}
            	else if (!data.sfDirty){
            		// Violation (Too high jumping or step).
            		tags.add("maxphase");
            		vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.max(yDistance, 0.15));
            	}
            }
            
//            // Check maximal absolute distance (jumping).
//            if (!data.sfDirty && yDistance > 0.57 + data.jumpAmplifier * 0.2 && !toOnGround && from.isPassable()){
//            	// TODO: Side conditions... from.isPassable is checked because of pistons.
//            	// TODO: Pistons don't work.
//            	vDistanceAboveLimit = Math.max(vDistanceAboveLimit, yDistance - 0.53 + data.jumpAmplifier * 0.2);
//            	tags.add("fastascend");
//            }
            
            // TODO: Velocity handling here [concept: set vdistAbove.. almost always]?

            // TODO: This might need max(0, for ydiff)
			vDistanceAboveLimit = Math.max(vDistanceAboveLimit, to.getY() - data.getSetBackY() - vAllowedDistance);
			
			if (vDistanceAboveLimit > 0) tags.add("vdist");

			// Simple-step blocker.
			// TODO: Complex step blocker: distance to set-back + low jump + accounting info
			if ((fromOnGround || data.noFallAssumeGround) && toOnGround && Math.abs(yDistance - 1D) <= cc.yStep && vDistanceAboveLimit <= 0D && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_STEP)) {
				vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
				tags.add("step");
			}
		}

		if (data.noFallAssumeGround || fromOnGround || toOnGround) {
			// Some reset condition.
			data.jumpAmplifier = MovingListener.getJumpAmplifier(player);
		}
		
		// TODO: on ground -> on ground improvements
		
		if (!resetFrom && !resetTo){
			// "On-air" checks (vertical)
			vDistanceAboveLimit = Math.max(vDistanceAboveLimit, verticalAccounting(now, from, to, hDistance, yDistance, data, cc));
		}

        final double result = (Math.max(hDistanceAboveLimit, 0D) + Math.max(vDistanceAboveLimit, 0D)) * 100D;

		if (cc.debug) {
			// Put in a method for shorter code.
			outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
		}
		
		data.sfJumpPhase++;

		// Handle violations.
		if (result > 0D) {
			final Location vLoc = handleViolation(now, result, player, from, to, data, cc);
			if (vLoc != null) return vLoc;
		}
        else{
            // Slowly reduce the level with each event, if violations have not recently happened.
            if (now - data.sfVLTime > cc.survivalFlyVLFreeze) data.survivalFlyVL *= 0.95D;
        }
		
		//  Set data for normal move or violation without cancel (cancel would have returned above)
        
		// Check lift-off medium.
		// TODO: Web might be NO_JUMP !
		// TODO: isNextToGround(0.15, 0.4) allows a little much (yMargin), but reduces false positives.
		// TODO: nextToGround: Shortcut with block-flags ?
		
		if (to.isInLiquid()){
			if (fromOnGround && !toOnGround && data.mediumLiftOff == MediumLiftOff.GROUND && data.sfJumpPhase <= 1  && !from.isInLiquid()){
				data.mediumLiftOff = MediumLiftOff.GROUND;
			}
			else if (to.isNextToGround(0.15, 0.4)){
				// Consent with ground.
				data.mediumLiftOff = MediumLiftOff.GROUND;
			} 
			else{
				data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
			}
		}
		else if (to.isInWeb()){
			data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
		}
		else if (resetTo){
			// TODO: This might allow jumping on vines etc., but should do for the moment.
			data.mediumLiftOff = MediumLiftOff.GROUND;
		}
		else if (from.isInLiquid()){
			if (!resetTo && data.mediumLiftOff == MediumLiftOff.GROUND && data.sfJumpPhase <= 1){
				data.mediumLiftOff = MediumLiftOff.GROUND;
			}
			else if (to.isNextToGround(0.15, 0.4)){
				data.mediumLiftOff = MediumLiftOff.GROUND;
			}
			else{
				data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
			}
		}
		else if (from.isInWeb()){
			data.mediumLiftOff = MediumLiftOff.LIMIT_JUMP;
		}
		else if (resetFrom || data.noFallAssumeGround){
			// TODO: Where exactly to put noFallAssumeGround ?
			data.mediumLiftOff = MediumLiftOff.GROUND;
		}
		else{
			// Keep medium.
			// TODO: Is above stairs ?
		}
		
        // Apply reset conditions.
        data.toWasReset = resetTo || data.noFallAssumeGround;
        data.fromWasReset = resetFrom || data.noFallAssumeGround;
        if (data.verticalVelocityUsed > cc.velocityGraceTicks && yDistance <= 0 && data.sfLastYDist > 0){
//        	data.verticalFreedom = 0;
        	data.verticalVelocityCounter = 0;
        	data.verticalVelocity = 0;
        }
        if (resetTo){
            // The player has moved onto ground.
            data.setSetBack(to);
            data.sfJumpPhase = 0;
            data.clearAccounting();
            // TODO: Experimental: reset velocity.
            if (data.verticalVelocityUsed > cc.velocityGraceTicks && toOnGround && yDistance < 0){
                data.verticalVelocityCounter = 0;
                data.verticalFreedom = 0;
                data.verticalVelocity = 0;
                data.verticalVelocityUsed = 0;
            }
        }
        else if (resetFrom){
            // The player moved from ground.
            data.setSetBack(from);
            data.sfJumpPhase = 1; // TODO: ?
            data.clearAccounting();
        }
        
        // Check removal of active horizontal velocity.
        if (hDistance <= hAllowedDistance){ // TODO: Check conditions etc.
        	// Invalidate used horizontal velocity.
//        	System.out.println("*** INVALIDATE ON SPEED");
        	data.hVelActive.clear();
//          if (data.horizontalVelocityUsed > cc.velocityGraceTicks){
//        	data.horizontalFreedom = 0;
//        	data.horizontalVelocityCounter = 0;
//        	data.horizontalVelocityUsed = 0;
//        }
        }
        
        data.sfLastYDist = yDistance;
        return null;
    }

    /**
     * Syso debug output.
     * @param player
     * @param data
     * @param cc
     * @param hDistance
     * @param hAllowedDistance
     * @param yDistance
     * @param vAllowedDistance
     * @param fromOnGround
     * @param resetFrom
     * @param toOnGround
     * @param resetTo
     */
	private void outputDebug(final Player player, final PlayerLocation to, final MovingData data, final MovingConfig cc, 
			final double hDistance, final double hAllowedDistance, final double hFreedom, final double yDistance, final double vAllowedDistance,
			final boolean fromOnGround, final boolean resetFrom, final boolean toOnGround, final boolean resetTo) {
		// TODO: Show player name once (!)
		final StringBuilder builder = new StringBuilder(500);
		final String hBuf = (data.sfHorizontalBuffer < 1.0 ? ((" hbuf=" + StringUtil.fdec3.format(data.sfHorizontalBuffer))) : "");
		final String hBufExtra = (data.sfHBufExtra > 0 ? (" hbufextra=" + data.sfHBufExtra) : "");
		final String hVelUsed = hFreedom > 0 ? " hVelUsed=" + StringUtil.fdec3.format(hFreedom) : "";
		builder.append(player.getName() + " SurvivalFly\nground: " + (data.noFallAssumeGround ? "(assumeonground) " : "") + (fromOnGround ? "onground -> " : (resetFrom ? "resetcond -> " : "--- -> ")) + (toOnGround ? "onground" : (resetTo ? "resetcond" : "---")) + ", jumpphase: " + data.sfJumpPhase);
		builder.append("\n" + " hDist: " + StringUtil.fdec3.format(hDistance) + " / " +  StringUtil.fdec3.format(hAllowedDistance) + hBuf + hBufExtra + hVelUsed + " , vDist: " +  StringUtil.fdec3.format(yDistance) + " (" + StringUtil.fdec3.format(to.getY() - data.getSetBackY()) + " / " +  StringUtil.fdec3.format(vAllowedDistance) + "), sby=" + (data.hasSetBack() ? data.getSetBackY() : "?"));
		if (data.verticalVelocityCounter > 0 || data.verticalFreedom >= 0.001){
			builder.append("\n" + " vertical freedom: " +  StringUtil.fdec3.format(data.verticalFreedom) + " (vel=" +  StringUtil.fdec3.format(data.verticalVelocity) + "/counter=" + data.verticalVelocityCounter +"/used="+data.verticalVelocityUsed);
		}
//		if (data.horizontalVelocityCounter > 0 || data.horizontalFreedom >= 0.001){
//			builder.append("\n" + player.getName() + " horizontal freedom: " +  StringUtil.fdec3.format(data.horizontalFreedom) + " (counter=" + data.horizontalVelocityCounter +"/used="+data.horizontalVelocityUsed);
//		}
		if (!data.hVelActive.isEmpty()){
			builder.append("\n" + " horizontal velocity (active):");
			addVeloctiy(builder, data.hVelActive);
		}
		if (!data.hVelQueued.isEmpty()){
			builder.append("\n" + " horizontal velocity (queued):");
			addVeloctiy(builder, data.hVelQueued);
		}
		if (!resetFrom && !resetTo) {
			if (cc.survivalFlyAccountingV && data.vDistAcc.count() > data.vDistAcc.bucketCapacity()) builder.append("\n" + " vacc=" + data.vDistAcc.toInformalString());
		}
		if (player.isSleeping()) tags.add("sleeping");
		if (!tags.isEmpty()) builder.append("\n" + " tags: " + StringUtil.join(tags, "+"));
		builder.append("\n");
//		builder.append(data.stats.getStatsStr(false));
		System.out.print(builder.toString());
	}

	private void addVeloctiy(final StringBuilder builder, final List<Velocity> entries) {
		for (final Velocity vel: entries){
			 builder.append(" ");
			 builder.append(vel);
		}
	}
	
	/**
	 * Check if a ground-touch has been lost due to event-sending-frequency or other reasons.<br>
	 * This is for ascending only (yDistance >= 0).
	 * @param player
	 * @param from
	 * @param to
	 * @param hDistance
	 * @param yDistance
	 * @param sprinting
	 * @param data
	 * @param cc
	 * @return
	 */
	private boolean lostGroundAscend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
		// TODO: re-organize for faster exclusions (hDistance, yDistance).
		// TODO: more strict conditions ?
		
		final double setBackYDistance = to.getY() - data.getSetBackY();
		
		// Half block step up.
		if (yDistance <= 0.5 && hDistance < 0.5 && setBackYDistance <= 1.3 + 0.2 * data.jumpAmplifier && to.isOnGround()){
			if (data.sfLastYDist < 0 || from.isOnGround(0.5 - Math.abs(yDistance))){
				return applyLostGround(player, from, true, data, "step");
			}
//			else data.stats.addStats(data.stats.getId("sfLostGround_" + "step", true), 0);
		}
		
		// Interpolation check.
		// (Still needed, unless a faster workaround is found.)
		// TODO: Check if the set-back distance still has relevance.
		// TODO: Check use of jump-amplifier.
		// TODO: Might check fall distance.
		//  && data.sfJumpPhase > 3 <- Seems to be a problem with cake on a block + jump over both mini edges (...).
		if (data.fromX != Double.MAX_VALUE && yDistance > 0 && data.sfLastYDist < 0 && !to.isOnGround()) {
			// TODO: Check if last-y-dist or sprinting should be considered.
			if (setBackYDistance > 0D && setBackYDistance <= 1.5D + 0.2 * data.jumpAmplifier || setBackYDistance < 0 && Math.abs(setBackYDistance) < 3.0) {
				// Interpolate from last to-coordinates to the from
				// coordinates (with some safe-guard).
				final double dX = from.getX() - data.fromX;
				final double dY = from.getY() - data.fromY;
				final double dZ = from.getZ() - data.fromZ;
				if (dX * dX + dY * dY + dZ * dZ < 0.5) { 
					// TODO: adjust limit according to ... speed etc ?
					// Check full bounding box since last from.
					final double minY = Math.min(data.toY, Math.min(data.fromY, from.getY()));
					final double iY = minY; // TODO ...
					final double r = from.getWidth() / 2.0; // TODO: check + 0.35;
					double yMargin = cc.yOnGround;
					// TODO: Might set margin higher depending on distance to 0 of block and last y distance etc.
					// TODO: check with iY + 0.25 removed.
					if (BlockProperties.isOnGround(from.getBlockCache(), Math.min(data.fromX, from.getX()) - r, iY - yMargin, Math.min(data.fromZ, from.getZ()) - r, Math.max(data.fromX, from.getX()) + r, iY + 0.25, Math.max(data.fromZ, from.getZ()) + r, 0L)) {
						return applyLostGround(player, from, true, data, "interpolate");
					}
//					else data.stats.addStats(data.stats.getId("sfLostGround_" + "interpolate", true), 0);
				}
			}
		}
		
		// Nothing found.
		return false;
	}
	
	/**
	 * Check if a ground-touch has been lost due to event-sending-frequency or other reasons.<br>
	 * This is for descending "mildly" only (-0.5 <= yDistance <= 0).
	 * @param player
	 * @param from
	 * @param to
	 * @param hDistance
	 * @param yDistance
	 * @param sprinting
	 * @param data
	 * @param cc
	 * @return
	 */
	private boolean lostGroundDescend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
		// TODO: re-organize for faster exclusions (hDistance, yDistance).
		// TODO: more strict conditions 
		
		final double setBackYDistance = to.getY() - data.getSetBackY();
		
		if (data.sfJumpPhase <= 7){
			// Check for sprinting down blocks etc.
			if (data.sfLastYDist <= yDistance && setBackYDistance < 0 && !to.isOnGround()){
				// TODO: setbackydist: <= - 1.0 or similar
				// TODO: <= 7 might work with speed II, not sure with above.
				// TODO: account for speed/sprint
				// TODO: account for half steps !?
				if (from.isOnGround(0.6, 0.4, 0, 0L) ){
					// TODO: further narrow down bounds ?
					// Temporary "fix".
					return applyLostGround(player, from, true, data, "pyramid");
				}
//				else data.stats.addStats(data.stats.getId("sfLostGround_" + "pyramid", true), 0);
			}
			
			// Check for jumping up strange blocks like flower pots on top of other blocks.
			if (yDistance == 0 && data.sfLastYDist > 0 && data.sfLastYDist < 0.25 && data.sfJumpPhase <= 6 + data.jumpAmplifier * 3 && setBackYDistance > 1.0 && setBackYDistance < 1.5 + 0.2 * data.jumpAmplifier && !to.isOnGround()){
				// TODO: confine by block types ?
				if (from.isOnGround(0.25, 0.4, 0, 0L) ){
					// Temporary "fix".
					return applyLostGround(player, from, true, data, "ministep");
				}
//				else data.stats.addStats(data.stats.getId("sfLostGround_" + "ministep", true), 0);
			}
		}
		// Lost ground while falling onto/over edges of blocks.
		if (yDistance < 0 && hDistance <= 0.5 && data.sfLastYDist < 0 && yDistance > data.sfLastYDist && !to.isOnGround()){
			// TODO: Should this be an extra lost-ground(to) check, setting toOnGround  [for no-fall no difference]?
			// TODO: yDistance <= 0 might be better.
			// Also clear accounting data.
//			if (to.isOnGround(0.5) || from.isOnGround(0.5)){
			if (from.isOnGround(0.5, 0.2, 0) || to.isOnGround(0.5, Math.min(0.2, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance))){
				return applyLostGround(player, from, true, data, "edge");
			}
//			else data.stats.addStats(data.stats.getId("sfLostGround_" + "edge", true), 0);
		}
		
		// Nothing found.
		return false;
	}
	
	/**
	 * Check if a ground-touch has been lost due to event-sending-frequency or other reasons.<br>
	 * This is for fast descending only (yDistance < -0.5).
	 * @param player
	 * @param from
	 * @param to
	 * @param hDistance
	 * @param yDistance
	 * @param sprinting
	 * @param data
	 * @param cc
	 * @return
	 */
	private boolean lostGroundFastDescend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
		// TODO: re-organize for faster exclusions (hDistance, yDistance).
		// TODO: more strict conditions 
		// Lost ground while falling onto/over edges of blocks.
		if (yDistance > data.sfLastYDist && !to.isOnGround()){
			// TODO: Should this be an extra lost-ground(to) check, setting toOnGround  [for no-fall no difference]?
			// TODO: yDistance <= 0 might be better.
			// Also clear accounting data.
			// TODO: stairs ?
			// TODO: Can it be safe to only check to with raised margin ? [in fact should be checked from higher yMin down]
			// TODO: Interpolation method (from to)?
			if (from.isOnGround(0.5, 0.2, 0) || to.isOnGround(0.5, Math.min(0.3, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance))){
				// (Usually yDistance should be -0.078)
				return applyLostGround(player, from, true, data, "fastedge");
			}
//			else data.stats.addStats(data.stats.getId("sfLostGround_" + "fastedge", true), 0);
		}
		return false;
	}
	
	/**
	 * Apply lost-ground workaround, 
	 * @param player
	 * @param from
	 * @param setBackSafe If to use from as set-back (if set to false: currently nothing changed).
	 * @param data
	 * @param tag Tag extra to "lostground"
	 * @return Always true.
	 */
	private boolean applyLostGround(final Player player, final PlayerLocation from, final boolean setBackSafe, final MovingData data, final String tag){
		// Set the new setBack and reset the jumpPhase.
		// TODO: Some interpolated position ?
		// TODO: (Task list: sharpen when this is used, might remove isAboveStairs!)
		if (setBackSafe){
			data.setSetBack(from);
		}
		else{
			// Keep Set-back.
		}
		
		// data.ground ?
		// ? set jumpphase to height / 0.15 ?
		data.sfJumpPhase = 0;
		data.jumpAmplifier = MovingListener.getJumpAmplifier(player);
		data.clearAccounting();
		// Tell NoFall that we assume the player to have been on ground somehow.
		data.noFallAssumeGround = true;
		tags.add("lostground_" + tag);
//		data.stats.addStats(data.stats.getId("sfLostGround_" + tag, true), 1);
		return true;
	}

	/**
	 * Return hAllowedDistance, not exact, check permissions as far as
	 * necessary, if flag is set to check them.
	 * 
	 * @param player
	 * @param sprinting
	 * @param hDistance
	 * @param hAllowedDistance
	 * @param data
	 * @param cc
	 * @return
	 */
	private double getAllowedhDist(final Player player, final PlayerLocation from, final PlayerLocation to, final boolean sprinting, final boolean downStream, final double hDistance, final MovingData data, final MovingConfig cc, boolean checkPermissions)
	{
		if (checkPermissions) tags.add("permchecks");
		// TODO: re-arrange for fastest checks first (check vs. allowed distance
		// multiple times if necessary.
		double hAllowedDistance = 0D;
		// Player on ice? Give him higher max speed.
		// TODO: maybe re-model ice stuff (check what is really needed).
		if (from.isOnIce() || to.isOnIce()) data.sfFlyOnIce = 20;
		else if (data.sfFlyOnIce > 0) data.sfFlyOnIce--;
		
		final boolean sfDirty = data.sfDirty;

		if (from.isInWeb()) {
			data.sfFlyOnIce = 0;
			// TODO: if (from.isOnIce()) <- makes it even slower !
			// Does include sprinting by now (would need other accounting methods).
			hAllowedDistance = webSpeed * cc.survivalFlyWalkingSpeed / 100D;
		} else if (from.isInLiquid() && to.isInLiquid()) {
			// Check all liquids (lava might demand even slower speed though).
			// TODO: too many false positives with just checking from ?
			// TODO: Sneaking and blocking applies to when in water !
			hAllowedDistance = swimmingSpeed * cc.survivalFlySwimmingSpeed / 100D;
		} else if (!sfDirty && player.isSneaking() && reallySneaking.contains(player.getName()) && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SNEAKING))) hAllowedDistance = sneakingSpeed * cc.survivalFlySneakingSpeed / 100D;
		else if (!sfDirty && player.isBlocking() && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))) hAllowedDistance = blockingSpeed * cc.survivalFlyBlockingSpeed / 100D;
		else {
			if (!sprinting) hAllowedDistance = walkingSpeed * cc.survivalFlyWalkingSpeed / 100D;
			else hAllowedDistance = sprintingSpeed * cc.survivalFlySprintingSpeed / 100D;

			// Speeding bypass permission (can be combined with other bypasses).
			// TODO: How exactly to bring it on finally.
			if (checkPermissions && player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPEEDING)) hAllowedDistance *= cc.survivalFlySpeedingSpeed / 100D;
		}

		// If the player is on ice, give him an higher maximum speed.
		if (data.sfFlyOnIce > 0) hAllowedDistance *= modIce;
		
		// Account for flowing liquids (only if needed).
		// Assume: If in liquids this would be placed right here.
		// TODO: Consider data.mediumLiftOff != ...GROUND
		if (downStream) {
			hAllowedDistance *= modDownStream;
		}
		
		if (hDistance <= hAllowedDistance) return hAllowedDistance;
		
		// Speed amplifier.
		final double speedAmplifier = mcAccess.getFasterMovementAmplifier(player);
		if (speedAmplifier != Double.NEGATIVE_INFINITY) hAllowedDistance *= 1.0D + 0.2D * (speedAmplifier + 1);
		
		return hAllowedDistance;
	}

	/**
	 * Violation handling put here to have less code for the frequent processing of check.
	 * @param now
	 * @param result
	 * @param player
	 * @param from
	 * @param to
	 * @param data
	 * @param cc
	 * @return
	 */
	private final Location handleViolation(final long now, final double result, final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc)
	{
		// Increment violation level.
		data.survivalFlyVL += result;
		data.sfVLTime = now;
		final ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, result, cc.survivalFlyActions);
		if (vd.needsParameters()) {
			vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
			vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
			vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", to.getLocation().distance(from.getLocation())));
			vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
		}
		// Some resetting is done in MovingListener.
		if (executeActions(vd)) {
			// Set-back + view direction of to (more smooth).
			return data.getSetBack(to);
		}
		else{
			data.clearAccounting();
			data.sfJumpPhase = 0;
			// Cancelled by other plugin, or no cancel set by configuration.
			return null;
		}
	}
	
	/**
	 * Hover violations have to be handled in this check, because they are handled as SurvivalFly violations (needs executeActions).
	 * @param player
	 * @param loc
	 * @param cc
	 * @param data
	 */
	protected final void handleHoverViolation(final Player player, final Location loc, final MovingConfig cc, final MovingData data) {
		data.survivalFlyVL += cc.sfHoverViolation;
		
		// TODO: Extra options for set-back / kick, like vl?
		data.sfVLTime = System.currentTimeMillis();
		final ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, cc.sfHoverViolation, cc.survivalFlyActions);
		if (vd.needsParameters()) {
			vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ()));
			vd.setParameter(ParameterName.LOCATION_TO, "(HOVER)");
			vd.setParameter(ParameterName.DISTANCE, "0.0(HOVER)");
			vd.setParameter(ParameterName.TAGS, "hover");
		}
		if (executeActions(vd)) {
			// Set-back or kick.
			if (data.hasSetBack()){
				data.clearAccounting();
				data.sfJumpPhase = 0;
				data.sfLastYDist = Double.MAX_VALUE;
				data.toWasReset = false;
				data.fromWasReset = false;
				data.setTeleported(data.getSetBack(loc));
				player.teleport(data.getTeleported());
			}
			else{
				// Solve by extra actions ? Special case (probably never happens)?
				player.kickPlayer("Hovering?");
			}
		}
		else{
			// Ignore.
		}
	}
	
	

	/**
	 * First split-off. Not strictly accounting only, actually.<br>
	 * In-air checks.
	 * 
	 * @param now
	 * @param yDistance
	 * @param data
	 * @param cc
	 * @return
	 */
	private double verticalAccounting(final long now, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final MovingData data, final MovingConfig cc) {
		double vDistanceAboveLimit = 0;
		// y direction change detection.
		// TODO: Consider using accounting for y-change detection.
		boolean yDirChange = data.sfLastYDist != Double.MAX_VALUE && data.sfLastYDist != yDistance && (yDistance <= 0 && data.sfLastYDist >= 0 || yDistance >= 0 && data.sfLastYDist <= 0 ); 
		if (yDirChange){
			// TODO: account for velocity,
			if (yDistance > 0){
				// Increase
				if (data.toWasReset){
					tags.add("ychinc");
				}
				else {
					// Moving upwards after falling without having touched the ground.
					if (data.verticalFreedom <= 0.001 && data.bunnyhopDelay < 9 && !(data.fromWasReset && data.sfLastYDist == 0D)){
						// TODO: adjust limit for bunny-hop.
						vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
						tags.add("ychincfly");
					}
					else tags.add("ychincair");
				}
			}
			else{
				// Decrease
				tags.add("ychdec");
			}
		}
		
		// Accounting support.
		if (cc.survivalFlyAccountingV) {
			// Currently only for "air" phases.
			// Vertical.
			if (yDirChange && data.sfLastYDist > 0){
				// Change to descending phase !
				data.vDistAcc.clear();
				data.vDistAcc.add((float) yDistance);
			}
			else if (data.verticalFreedom <= 0.001D) {
				// Here yDistance can be negative and positive (!).
				if (yDistance != 0D){
					final double accAboveLimit = verticalAccounting(now, from, to, yDistance, data.vDistAcc ,tags, "vacc");
					if (accAboveLimit > vDistanceAboveLimit){
						vDistanceAboveLimit = accAboveLimit;
					}
				}
			}
			else{
				// TODO: Just to exclude source of error, might be redundant.
				data.vDistAcc.clear();
			}
		}
		return vDistanceAboveLimit;
	}
	
	/**
	 * Keep track of values, demanding that with time the values decrease.<br>
	 * The ActionFrequency objects have 3 buckets, bucket 1 is checked against
	 * bucket 2, 0 is ignored. [Vertical accounting.]
	 * 
	 * @param now
	 * @param yDistance
	 * @param sum
	 * @param count
	 * @param tags
	 * @param tag
	 * @return absolute difference on violation.;
	 */
//	private static final double verticalAccounting(final long now, final double value, final ActionFrequency sum, final ActionFrequency count, final ArrayList<String> tags, String tag)
	private static final double verticalAccounting(final long now,  final PlayerLocation from, final PlayerLocation to, final double yDistance, final ActionAccumulator acc, final ArrayList<String> tags, final String tag)
	{
//		sum.add(now, (float) value);
//		count.add(now, 1f);
		acc.add((float) yDistance);
		// TODO: Add on-eq-return parameter
//		if (count.bucketScore(2) > 0 && count.bucketScore(1) > 0) {
		final int i1, i2;
		// TODO: distinguish near-ground moves somehow ?
//		if (acc.bucketCount(0) == acc.bucketCapacity()){
//			i1 = 0;
//			i2 = 1;
//		}
//		else{
		i1 = 1;
		i2 = 2;
//		}
		if (acc.bucketCount(i1) > 0 && acc.bucketCount(i2) > 0) {
//			final float sc1 = sum.bucketScore(1);
//			final float sc2 = sum.bucketScore(2);
			final float sc1 = acc.bucketScore(i1);
			final float sc2 = acc.bucketScore(i2);
			final double diff = sc1 - sc2;
			final double aDiff = Math.abs(diff);
			// TODO: Relate this to the fall distance !
			if (diff > 0 || yDistance > -1.1 && aDiff <= 0.07) { // TODO: sharpen later (force speed gain while falling).
				if (yDistance < -1.1 && (aDiff < Math.abs(yDistance) || sc2 < - 10)){
					// High falling speeds are somewhat ok.
					tags.add(tag + "grace");
					return 0;
				}
//				else if (value < 0 && aDiff < 0.27 && sc1 * sc2 > 0.0 && Math.abs(sc1) > 0.27 && (BlockProperties.isGround(from.getTypeIdBelow()) || BlockProperties.isGround(to.getTypeIdBelow()) || from.isOnGround(0.6, 0.4, 0))){
//					// TODO: This part is a temporary workaround for sprinting down block-stairs (around sc1*sc2).
//					// TODO: This works partly only.
//					tags.add(tag + "tempgrace");
//					return 0;
//				}
				tags.add(tag);
				if (diff < 0 ){
					return 1.3 - aDiff;
				}
				else{
					return diff;
				}
			}
		}
		// TODO: return Float.MAX_VALUE if no violation ?
		return 0;
	}

	/**
	 * Allow accumulating some vls and silently set the player back.
	 * 
	 * @param player
	 * @param data
	 * @param cc
	 * @param to
	 * @param now
	 * @param vDistanceAboveLimit
	 * @return
	 */
	private final Location hackCobweb(final Player player, final MovingData data, final PlayerLocation to, 
			final long now, final double vDistanceAboveLimit)
	{
		if (now - data.sfCobwebTime > 3000) {
			data.sfCobwebTime = now;
			data.sfCobwebVL = vDistanceAboveLimit * 100D;
		} else data.sfCobwebVL += vDistanceAboveLimit * 100D;
		if (data.sfCobwebVL < 550) { // Totally random !
			// Silently set back.
			if (!data.hasSetBack()) data.setSetBack(player.getLocation()); // ? check moment of call.
			data.sfJumpPhase = 0;
			data.sfLastYDist = Double.MAX_VALUE;
			return data.getSetBack(to);
		} else return null;
	}

	/**
	 * This is set with PlayerToggleSneak, to be able to distinguish players that are really sneaking from players that are set sneaking by a plugin. 
	 * @param player
	 * @param sneaking
	 */
	public void setReallySneaking(final Player player, final boolean sneaking) {
		if (sneaking) reallySneaking.add(player.getName());
		else reallySneaking.remove(player.getName());
	}
	
}
