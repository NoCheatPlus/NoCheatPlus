package fr.neatmonster.nocheatplus.checks.moving;

import java.util.ArrayList;
import java.util.Locale;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

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
	public static final double swimmingSpeed    = 0.11D;
	public static final double webSpeed         = 0.105D; // TODO: walkingSpeed * 0.15D; <- does not work
	
	public static final double modIce			= 2.5D;
	
	/** Faster moving down stream (water mainly). */
	public static final double modDownStream      = 0.19 / swimmingSpeed;
	/** Bunny-hop delay. */
	private static final int   bunnyHopMax = 9;

	/** To join some tags with moving check violations. */
	private final ArrayList<String> tags = new ArrayList<String>(15);
	
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
	 * @return Location to teleport to if it is a violation.
	 */
	public Location checkBed(final Player player, final MovingData data) {
		Location newTo = null;
		// Check if the player had been in bed at all.
		if (!data.sfWasInBed) {
			// Violation ...
			data.survivalFlyVL += 100D;
			
			// TODO: add tag

			// And return if we need to do something or not.
			if (executeActions(player, data.survivalFlyVL, 100D, MovingConfig.getConfig(player).survivalFlyActions)){
				final Location loc = player.getLocation();
				newTo = data.setBack;
				if (newTo == null){
					// TODO: Add something to guess the best set back location (possibly data.guessSetBack(Location)).
					newTo = loc;
				}
				newTo.setPitch(loc.getPitch());
				newTo.setYaw(loc.getYaw());
			}
		} else{
			// He has, everything is alright.
			data.sfWasInBed = false;
		}
		return newTo;
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
    public Location check(final Player player, final EntityPlayer mcPlayer, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {
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
        if (data.setBack == null)
            data.setBack = from.getLocation();

		final boolean resetFrom;

		// "Lost ground" workaround.
		if (fromOnGround || from.isResetCond()) resetFrom = true;
		else if (lostGround(player, mcPlayer, from, to, yDistance, data, cc)){
			resetFrom = true;
			// TODO: Consider && !resetTo ?
			// Note: if not setting resetFrom, other places have to check assumeGround...
		}
		else resetFrom = false;

		double hAllowedDistance = getAllowedhDist(player, mcPlayer, from, to, sprinting, hDistance, data, cc, false);

		// Account for flowing liquids (only if needed).
		// Assume: If in fluids this would be placed right here.
		if (hDistance > swimmingSpeed && from.isInLiquid() && from.isDownStream(xDistance, zDistance)) {
			hAllowedDistance *= modDownStream;
		}
		
        // Judge if horizontal speed is above limit.
        double hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom;

		// Tag for simple speed violation (medium), might get overridden.
		if (hDistanceAboveLimit > 0){
			// After failure permission checks ( + speed modifier + sneaking + blocking + speeding) !
			hAllowedDistance = getAllowedhDist(player, mcPlayer, from, to, sprinting, hDistance, data, cc, true);
			hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom;
			if (hAllowedDistance > 0){
				tags.add("hspeed");
			}
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
        if (hDistanceAboveLimit <= 0D && sprinting) {
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
		
//		if (cc.survivalFlyAccountingH && !resetFrom && !resetTo) {
//			// Currently only for "air" phases.
//			// Horizontal.
//			if (data.horizontalFreedom <= 0.001D) {
//				// This only checks general speed decrease once velocity is smoked up.
//				// TODO: account for bunny-hop
//				if (hDistance != 0.0) hDistanceAboveLimit = Math.max(hDistanceAboveLimit, doAccounting(now, hDistance, data.hDistSum, data.hDistCount, tags, "hacc"));
//			} else {
//				// TODO: Just to exclude source of error, might be redundant.
//				data.hDistCount.clear(now);
//				data.hDistSum.clear(now);
//			}
//		}

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

        // Calculate the vertical speed limit based on the current jump phase.
        double vAllowedDistance, vDistanceAboveLimit;
        if (from.isInWeb()){
        	// Very simple: force players to descend or stay.
         	vAllowedDistance = from.isOnGround() ? 0.1D : 0;
         	data.jumpAmplifier = 0; // TODO: later maybe fetch.
        	vDistanceAboveLimit = yDistance;
        	if (cc.survivalFlyCobwebHack && vDistanceAboveLimit > 0 && hDistanceAboveLimit <= 0){
        		// TODO: Seemed fixed at first by CB/MC, but still does occur. 
        		final Location silentSetBack = hackCobweb(player, data, to, now, vDistanceAboveLimit);
        		if (silentSetBack != null){
        			if (cc.debug) System.out.println(player.getName()+ " (Cobweb: silent set-back)");
        			return silentSetBack;
        		}
        	}
        	if (vDistanceAboveLimit > 0) tags.add("vweb");
        }
        // else if (verticalFreedom <= 0.001 && from.isOnLadder) ....
        // else if (verticalFreedom <= 0.001 (?) & from.isInFluid
        else{
        	// Check traveled y-distance, orientation is air + jumping + velocity (as far as it gets).
        	vAllowedDistance = (!(fromOnGround || data.noFallAssumeGround) && !toOnGround ? 1.45D : 1.35D) + data.verticalFreedom;
        	final int maxJumpPhase;
            if (data.jumpAmplifier > 0){
                vAllowedDistance += 0.5 + data.jumpAmplifier - 1.0;
                maxJumpPhase = (int) (9 + (data.jumpAmplifier - 1.0) * 6);
            }
            else maxJumpPhase = 6;
            // TODO: consider tags for jumping as well (!).
            if (data.sfJumpPhase > maxJumpPhase && data.verticalVelocityCounter <= 0){
            	vAllowedDistance -= Math.max(0, (data.sfJumpPhase - maxJumpPhase) * 0.15D);
            }

			vDistanceAboveLimit = to.getY() - data.setBack.getY() - vAllowedDistance;
			
			if (vDistanceAboveLimit > 0) tags.add("vdist");

			// Simple-step blocker.
			if ((fromOnGround || data.noFallAssumeGround) && toOnGround && Math.abs(yDistance - 1D) <= cc.yStep && vDistanceAboveLimit <= 0D && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_STEP)) {
				vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
				tags.add("step");
			}
		}

		if (data.noFallAssumeGround || fromOnGround || toOnGround) {
			// Some reset condition.
			data.jumpAmplifier = MovingListener.getJumpAmplifier(mcPlayer);
		}
		
		// TODO: on ground -> on ground improvements
		
		if (!resetFrom && !resetTo){
			// "On-air" checks (vertical)
			vDistanceAboveLimit = Math.max(vDistanceAboveLimit, verticalAccounting(now, yDistance, data, cc));
		}
		

        final double result = (Math.max(hDistanceAboveLimit, 0D) + Math.max(vDistanceAboveLimit, 0D)) * 100D;

        data.sfJumpPhase++;

		if (cc.debug) {
			// TODO: also show resetcond (!)
			StringBuilder builder = new StringBuilder(500);
			builder.append(player.getName() + " vfreedom: " +  CheckUtils.fdec3.format(data.verticalFreedom) + " (vv=" +  CheckUtils.fdec3.format(data.verticalVelocity) + "/vvc=" + data.verticalVelocityCounter + "), jumpphase: " + data.sfJumpPhase + "\n");
			builder.append(player.getName() + " hDist: " + CheckUtils.fdec3.format(hDistance) + " / " +  CheckUtils.fdec3.format(hAllowedDistance) + " , vDist: " +  CheckUtils.fdec3.format(yDistance) + " / " +  CheckUtils.fdec3.format(vAllowedDistance) + "\n");
			final double plY = player.getLocation().getY();
			final String plYs = from.getY() == plY ? "" : ("|" +  CheckUtils.fdec3.format(plY));
			builder.append(player.getName() + " y: " +  CheckUtils.fdec3.format(from.getY()) + plYs + (fromOnGround ? "(onground)" : "") + (data.noFallAssumeGround ? "(assumeonground)" : "") + " -> " +  CheckUtils.fdec3.format(to.getY()) + (toOnGround ? "(onground)" : "") + "\n");
			if (!resetFrom && !resetTo) {
				if (cc.survivalFlyAccountingH && data.hDistCount.bucketScore(1) > 0 && data.hDistCount.bucketScore(2) > 0) builder.append(player.getName() + " hacc=" + data.hDistSum.bucketScore(2) + "->" + data.hDistSum.bucketScore(1) + "\n");
				if (cc.survivalFlyAccountingV && data.vDistCount.bucketScore(1) > 0 && data.vDistCount.bucketScore(2) > 0) builder.append(player.getName() + " vacc=" + data.vDistSum.bucketScore(2) + "->" + data.vDistSum.bucketScore(1) + "\n");
			}
			if (!tags.isEmpty()) builder.append(player.getName() + " tags: " + CheckUtils.join(tags, "+") + "\n");
			System.out.print(builder.toString());
		}

		// Handle violations.
		if (result > 0D) {
			final Location vLoc = handleViolation(now, result, player, from, to, data, cc);
			if (vLoc != null) return vLoc;
		}
        else{
            // Slowly reduce the level with each event, if violations have not recently happened.
            if (now - data.sfVLTime > cc.survivalFlyVLFreeze) data.survivalFlyVL *= 0.95D;
        }
        
        // Violation or not, apply reset conditions (cancel would have returned above).
        data.toWasReset = resetTo || data.noFallAssumeGround;
        data.fromWasReset = resetFrom || data.noFallAssumeGround;
        if (resetTo){
            // The player has moved onto ground.
            data.setBack = to.getLocation();
            data.sfJumpPhase = 0;
            data.clearAccounting();
        }
        else if (resetFrom){
            // The player moved from ground.
            data.setBack = from.getLocation();
            data.sfJumpPhase = 1; // TODO: ?
            data.clearAccounting();
        }
        data.sfLastYDist = yDistance;
        return null;
    }

	private boolean lostGround(final Player player, final EntityPlayer mcPlayer, final PlayerLocation from, final PlayerLocation to, final double yDistance, final MovingData data, final MovingConfig cc) {
		// Don't set "useWorkaround = x()", to avoid potential trouble with
		// reordering to come, and similar.
		boolean useWorkaround = false;
		boolean setBackSafe = false; // Let compiler remove this if necessary.
		// Check for moving off stairs.
		if (!useWorkaround && from.isAboveStairs()) {
			useWorkaround = true;
			setBackSafe = true;
		}
		// Check for "lost touch", for when moving events were not created,
		// for instance (1/256).
		if (!useWorkaround && data.fromX != Double.MAX_VALUE && yDistance > 0 && yDistance < 0.5 && data.sfLastYDist < 0) {
			final double setBackYDistance = to.getY() - data.setBack.getY();
			if (setBackYDistance > 0D && setBackYDistance <= 1.5D) {
				// Interpolate from last to-coordinates to the from
				// coordinates (with some safe-guard).
				final double dX = from.getX() - data.fromX;
				final double dY = from.getY() - data.fromY;
				final double dZ = from.getZ() - data.fromZ;
				if (dX * dX + dY * dY + dZ * dZ < 0.5) { // TODO: adjust
														 // limit maybe.
					// Check full bounding box since last from.
					final double minY = Math.min(data.toY, Math.min(data.fromY, from.getY()));
					final double iY = minY; // TODO ...
					final double r = from.getWidth() / 2.0;
					if (BlockProperties.isOnGround(from.getBlockAccess(), Math.min(data.fromX, from.getX()) - r, iY - cc.yOnGround, Math.min(data.fromZ, from.getZ()) - r, Math.max(data.fromX, from.getX()) + r, iY + 0.25, Math.max(data.fromZ, from.getZ()) + r)) {
						useWorkaround = true;
						setBackSafe = true;
					}
				}
			}
		}
		if (useWorkaround) { // !toOnGround && to.isAboveStairs()) {
			// Set the new setBack and reset the jumpPhase.
			if (setBackSafe) data.setBack = from.getLocation();
			// TODO: This seems dubious !
			data.setBack.setY(Location.locToBlock(data.setBack.getY()));
			// data.ground ?
			// ? set jumpphase to height / 0.15 ?
			data.sfJumpPhase = 0;
			data.jumpAmplifier = MovingListener.getJumpAmplifier(mcPlayer);
			data.clearAccounting();
			// Tell NoFall that we assume the player to have been on ground somehow.
			data.noFallAssumeGround = true;
			tags.add("lostground");
			return true; 
		}
		else return false;
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
	private double getAllowedhDist(final Player player, final EntityPlayer mcPlayer, final PlayerLocation from, final PlayerLocation to, final boolean sprinting, final double hDistance, final MovingData data, final MovingConfig cc, boolean checkPermissions)
	{
		if (checkPermissions) tags.add("permchecks");
		// TODO: re-arrange for fastest checks first (check vs. allowed distance
		// multiple times if necessary.
		double hAllowedDistance = 0D;
		// Player on ice? Give him higher max speed.
		// TODO: maybe re-model ice stuff (check what is really needed).
		if (from.isOnIce() || to.isOnIce()) data.sfFlyOnIce = 20;
		else if (data.sfFlyOnIce > 0) data.sfFlyOnIce--;

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
		} else if (player.isSneaking() && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SNEAKING))) hAllowedDistance = sneakingSpeed * cc.survivalFlySneakingSpeed / 100D;
		else if (player.isBlocking() && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))) hAllowedDistance = blockingSpeed * cc.survivalFlyBlockingSpeed / 100D;
		else {
			if (!sprinting) hAllowedDistance = walkingSpeed * cc.survivalFlyWalkingSpeed / 100D;
			else hAllowedDistance = sprintingSpeed * cc.survivalFlySprintingSpeed / 100D;

			// Speeding bypass permission (can be combined with other bypasses).
			// TODO: How exactly to bring it on finally.
			if (checkPermissions && player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPEEDING)) hAllowedDistance *= cc.survivalFlySpeedingSpeed / 100D;
		}

		// If the player is on ice, give him an higher maximum speed.
		if (data.sfFlyOnIce > 0) hAllowedDistance *= modIce;
		
		if (hDistance <= hAllowedDistance) return hAllowedDistance;
		
		// Speed amplifier.
		if (mcPlayer.hasEffect(MobEffectList.FASTER_MOVEMENT)) hAllowedDistance *= 1.0D + 0.2D * (mcPlayer.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);
		
		return hAllowedDistance;
	}

	private final Location handleViolation(final long now, final double result, final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc)
	{
		// Increment violation level.
		data.survivalFlyVL += result;
		data.sfVLTime = now;
		data.clearAccounting();
		data.sfJumpPhase = 0;
		final ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, result, cc.survivalFlyActions);
		if (vd.needsParameters()) {
			vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
			vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
			vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", to.getLocation().distance(from.getLocation())));
			vd.setParameter(ParameterName.TAGS, CheckUtils.join(tags, "+"));
		}
		if (executeActions(vd)) {
			data.sfLastYDist = Double.MAX_VALUE;
			data.toWasReset = false;
			data.fromWasReset = false;
			// Set-back + view direction of to (more smooth).
			return new Location(player.getWorld(), data.setBack.getX(), data.setBack.getY(), data.setBack.getZ(), to.getYaw(), to.getPitch());
		}
		else{
			// Cancelled by other plugin, or no cancel set by configuration.
			return null;
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
	private double verticalAccounting(final long now, final double yDistance, final MovingData data, final MovingConfig cc) {
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
				data.vDistCount.clear(now);
				data.vDistSum.clear(now);
				data.vDistCount.add(1f);
				data.vDistSum.add((float) yDistance);
			}
			else if (data.verticalFreedom <= 0.001D) {
				// Here yDistance can be negative and positive (!).
				if (yDistance != 0D) vDistanceAboveLimit = Math.max(vDistanceAboveLimit, verticalAccounting(now, yDistance, data.vDistSum, data.vDistCount ,tags, "vacc"));
			}
			else{
				// TODO: Just to exclude source of error, might be redundant.
				data.vDistCount.clear(now);
				data.vDistSum.clear(now);
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
	 * @param value
	 * @param sum
	 * @param count
	 * @param tags
	 * @param tag
	 * @return absolute difference on violation.;
	 */
	private static final double verticalAccounting(final long now, final double value, final ActionFrequency sum, final ActionFrequency count, final ArrayList<String> tags, String tag)
	{
		sum.add(now, (float) value);
		count.add(now, 1f);
		// TODO: Add on-eq-return parameter
		if (count.bucketScore(2) > 0 && count.bucketScore(1) > 0) {
			final float sc1 = sum.bucketScore(1);
			final float sc2 = sum.bucketScore(2);
			final double diff = sc1 - sc2;
			if (diff > 0 || value > -1.5 && diff == 0) {
				if (value < -1.5 && (Math.abs(diff) < Math.abs(value) || sc2 < - 10)){
					tags.add(tag+"grace");
					return 0;
				}
				tags.add(tag);
				return diff;
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
			if (data.setBack == null) data.setBack = player.getLocation();
			data.sfJumpPhase = 0;
			data.setBack.setYaw(to.getYaw());
			data.setBack.setPitch(to.getPitch());
			data.sfLastYDist = Double.MAX_VALUE;
			return data.setBack;
		} else return null;
	}
	
}
