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
import fr.neatmonster.nocheatplus.players.Permissions;
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
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final MovingData data = MovingData.getData(player);

        // Check if the player has entered the bed he is trying to leave.
        if (!data.survivalFlyWasInBed) {
            // He hasn't, increment his violation level.
            data.survivalFlyVL += 100D;

            // And return if we need to do something or not.
            return executeActions(player, data.survivalFlyVL, 100D, MovingConfig.getConfig(player).survivalFlyActions);
        } else
            // He has, everything is alright.
            data.survivalFlyWasInBed = false;

        return false;
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
        final double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
        
        // If we don't have any setBack, choose the location the player comes from.
        if (data.setBack == null)
            data.setBack = from.getLocation();
        
        boolean resetFrom = fromOnGround || from.isInLiquid() || from.isOnLadder() || from.isInWeb();
        
        final double setBackYDistance = to.getY() - data.setBack.getY();
        // If the player has touched the ground but it hasn't been noticed by the plugin, the workaround is here.
        if (!resetFrom){
            // Don't set "useWorkaround = x()", to avoid potential trouble with reordering to come, and similar.
			boolean useWorkaround = false;
			boolean setBackSafe = false; // Let compiler remove this if necessary.
			// Check for moving off stairs.
			if (!useWorkaround && from.isAboveStairs()) {
				useWorkaround = true;
				setBackSafe = true;
			}
            // Check for "lost touch",  for when moving events were not created, for instance (1/256).
            if (!useWorkaround){
                final boolean inconsistent = yDistance > 0 && yDistance < 0.5 && data.survivalFlyLastYDist < 0 
                        && setBackYDistance > 0D && setBackYDistance <= 1.5D;
                if (inconsistent){
                    if (cc.debug) System.out.println(player.getName() + " Y-INCONSISTENCY");
                    if (data.fromX != Double.MAX_VALUE){
                        // Interpolate from last to-coordinates to the from coordinates (with some safe-guard).
                        final double dX = from.getX() - data.fromX;
                        final double dY = from.getY() - data.fromY;
                        final double dZ = from.getZ() - data.fromZ;
                        if (dX * dX + dY * dY + dZ * dZ < 0.5){ // TODO: adjust limit maybe.
                            // Check full bounding box since last from.
                            if (cc.debug) System.out.println(player.getName() + " CONSIDER WORKAROUND");
                            final double minY = Math.min(data.toY, Math.min(data.fromY, from.getY()));
                            final double iY =  minY; // TODO ...
                            final double r = from.getWidth() / 2.0;
							if (BlockProperties.isOnGround(from.getBlockAccess(), Math.min(data.fromX, from.getX()) - r, iY - cc.yOnGround, Math.min(data.fromZ, from.getZ()) - r, Math.max(data.fromX, from.getX()) + r, iY + 0.25, Math.max(data.fromZ, from.getZ()) + r)) {
								useWorkaround = true;
								setBackSafe = true;
							}
                        }
                    }
                } 
            }
            if (useWorkaround){ // !toOnGround && to.isAboveStairs()) {
                // Set the new setBack and reset the jumpPhase.
				if (setBackSafe) data.setBack = from.getLocation();
                data.setBack.setY(Location.locToBlock(data.setBack.getY()));
                // data.ground ?
                // ? set jumpphase to height / 0.15 ?
                data.survivalFlyJumpPhase = 0;
                data.jumpAmplifier = MovingListener.getJumpAmplifier(mcPlayer);
                data.clearAccounting();
                // Tell NoFall that we assume the player to have been on ground somehow.
                data.noFallAssumeGround = true;
                resetFrom = true; // Note: if removing this, other conditions need to check noFallAssume... 
                if (cc.debug) System.out.println(player.getName() + " Y-INCONSISTENCY WORKAROUND USED");
            }
        }

        // Player on ice? Give him higher max speed.
        if (from.isOnIce() || to.isOnIce())
            data.survivalFlyOnIce = 20;
        else if (data.survivalFlyOnIce > 0)
            data.survivalFlyOnIce--;

        // Choose the right horizontal speed limit for the current activity.
        double hAllowedDistance = 0D;
        if (from.isInWeb()){
        	data.survivalFlyOnIce = 0;
        	// TODO: if (from.isOnIce()) <- makes it even slower !
        	// Roughly 15% of walking speed.
        	hAllowedDistance = webSpeed * cc.survivalFlyWalkingSpeed / 100D;
        }
        else if (from.isInWater() && to.isInWater())
            hAllowedDistance = swimmingSpeed * cc.survivalFlySwimmingSpeed / 100D;
        else if (player.isSneaking() && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SNEAKING))
            hAllowedDistance = sneakingSpeed * cc.survivalFlySneakingSpeed / 100D;
        else if (player.isBlocking() && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))
            hAllowedDistance = blockingSpeed * cc.survivalFlyBlockingSpeed / 100D;
        else{
        	if (!sprinting)
                hAllowedDistance = walkingSpeed * cc.survivalFlyWalkingSpeed / 100D;
            else
                hAllowedDistance = sprintingSpeed * cc.survivalFlySprintingSpeed / 100D;
        	
            // Speeding bypass permission (can be combined with other bypasses).
            // TODO: How exactly to bring it on finally.
            if (player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPEEDING))
            	hAllowedDistance *= cc.survivalFlySpeedingSpeed/ 100D;
        }
        
        // TODO: Optimize: maybe only do the permission checks and modifiers if the distance is too big.
        //       (Depending on permission plugin, with pex it will be hardly 1000 ns for all moving perms, if all false.)
        
        // If the player is on ice, give him an higher maximum speed.
        if (data.survivalFlyOnIce > 0)
            hAllowedDistance *= modIce;

        // Taken directly from Minecraft code, should work.
        
//        player.hasPotionEffect(PotionEffectType.SPEED);
        if (mcPlayer.hasEffect(MobEffectList.FASTER_MOVEMENT))
            hAllowedDistance *= 1.0D + 0.2D * (mcPlayer.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);
        
        // Account for flowing liquids (only if needed).
        if (hDistance > swimmingSpeed && from.isInLiquid() && from.isDownStream(xDistance, zDistance)){
                hAllowedDistance *= modDownStream;
        }

        // Judge if horizontal speed is above limit.
        double hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom;

        if (hDistanceAboveLimit > 0) tags.add("hspeed");
        
		// Prevent players from walking on a liquid.
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
            		tags.add("sprintback");
            	}
            }
        }

        data.bunnyhopDelay--;

        // Did he go too far?
        if (hDistanceAboveLimit > 0 && sprinting)
            // Try to treat it as a the "bunnyhop" problem.
            if (data.bunnyhopDelay <= 0 && hDistanceAboveLimit > 0.05D && hDistanceAboveLimit < 0.28D) {
                data.bunnyhopDelay = 9;
                hDistanceAboveLimit = 0D;
                tags.clear();
                tags.add("bunny"); // TODO: Which here...
            }

        if (hDistanceAboveLimit > 0D) {
            // Try to consume the "buffer".
            hDistanceAboveLimit -= data.horizontalBuffer;
            data.horizontalBuffer = 0D;

            // Put back the "overconsumed" buffer.
            if (hDistanceAboveLimit < 0D){
            	data.horizontalBuffer = -hDistanceAboveLimit;
            }
            if (hDistanceAboveLimit >= 0){
            	tags.clear();
            	tags.add("hbuffer"); // TODO: ...
            }
        } else
            data.horizontalBuffer = Math.min(1D, data.horizontalBuffer - hDistanceAboveLimit);

        // Calculate the vertical speed limit based on the current jump phase.
        double vAllowedDistance, vDistanceAboveLimit;
        if (from.isInWeb()){
        	// Very simple: force players to descend or stay.
         	vAllowedDistance = from.isOnGround() ? 0.1D : 0;
         	data.jumpAmplifier = 0; // TODO: later maybe fetch.
        	vDistanceAboveLimit = yDistance;
        	if (cc.survivalFlyCobwebHack && vDistanceAboveLimit > 0 && hDistanceAboveLimit <= 0){
        		if (now - data.survivalFlyCobwebTime > 3000){
        			data.survivalFlyCobwebTime = now;
        			data.survivalFlyCobwebVL = vDistanceAboveLimit * 100D;
        		}
        		else data.survivalFlyCobwebVL += vDistanceAboveLimit * 100D;
        		if (data.survivalFlyCobwebVL < 550) { // Totally random !
        		    if (cc.debug) System.out.println(player.getName()+ " (Cobweb: silent set-back-)");
        		    // Silently set back.
        			if (data.setBack == null) data.setBack = player.getLocation();
        			data.survivalFlyJumpPhase = 0;
        			data.setBack.setYaw(to.getYaw());
        			data.setBack.setPitch(to.getPitch());
        			data.survivalFlyLastYDist = Double.MAX_VALUE;
        			return data.setBack;
        		}
        		if (vDistanceAboveLimit > 0) tags.add("vweb");
        	}
        }
        else{
        	vAllowedDistance = (!(fromOnGround || data.noFallAssumeGround) && !toOnGround ? 1.45D : 1.35D) + data.verticalFreedom;
        	final int maxJumpPhase;
            if (data.jumpAmplifier > 0){
                vAllowedDistance += 0.5 + data.jumpAmplifier - 1.0;
                maxJumpPhase = (int) (9 + (data.jumpAmplifier - 1.0) * 6);
            }
            else maxJumpPhase = 6;
            if (data.survivalFlyJumpPhase > maxJumpPhase && data.verticalVelocityCounter <= 0){
            	vAllowedDistance -= Math.max(0, (data.survivalFlyJumpPhase - maxJumpPhase) * 0.15D);
            }

			vDistanceAboveLimit = to.getY() - data.setBack.getY() - vAllowedDistance;

//            System.out.println("vda = " +vDistanceAboveLimit + " / vc = " + data.verticalVelocityCounter + " / vf = " + data.verticalFreedom + " / v = " + player.getVelocity().length());

			// Step can also be blocked.
			if ((fromOnGround || data.noFallAssumeGround) && toOnGround && Math.abs(yDistance - 1D) <= cc.yStep && vDistanceAboveLimit <= 0D && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_STEP)) {
				vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
				tags.add("step");
			}
		}
        
		if (data.noFallAssumeGround || fromOnGround || toOnGround) {
			// Some reset condition.
			data.jumpAmplifier = MovingListener.getJumpAmplifier(mcPlayer);
		}
		
		// Accounting support.
		if (cc.survivalFlyAccounting && !resetFrom) {
			// Horizontal.
			if (data.horizontalFreedom <= 0.001D){
				// This only checks general speed decrease oncevelocity is smoked up.
				hDistanceAboveLimit = Math.max(hDistanceAboveLimit, doAccounting(now, hDistance, data.hDistSum, data.hDistCount, tags, "hacc"));
			}
			// Vertical.
			if (data.verticalFreedom <= 0.001D) {
				// Here yDistance can be negative and positive (!).
				// TODO: Might demand resetting on some direction changes (bunny,)
				vDistanceAboveLimit = Math.max(vDistanceAboveLimit, doAccounting(now, yDistance, data.vDistSum, data.vDistCount, tags, "vacc"));
				// Check if y-direction is going upwards without speed / ground.
				if (yDistance >= 0 && data.survivalFlyLastYDist < 0) {
					// Moving upwards without having touched the ground.
					vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
					tags.add("ychange");
				}
			}
		}

        final double result = (Math.max(hDistanceAboveLimit, 0D) + Math.max(vDistanceAboveLimit, 0D)) * 100D;

        data.survivalFlyJumpPhase++;
        
        if (cc.debug){
            System.out.println(player.getName() + " vertical freedom: " + data.verticalFreedom + " ("+data.verticalVelocity+"/"+data.verticalVelocityCounter+"), jumpphase: " + data.survivalFlyJumpPhase);
            System.out.println(player.getName() + " hDist: " + hDistance + " / " + hAllowedDistance + " , vDist: " + (yDistance) + " ("+player.getVelocity().getY()+")" + " / " + vAllowedDistance);
            System.out.println(player.getName() + " y" + (fromOnGround?"(onground)":"") + (data.noFallAssumeGround?"(assumeonground)":"") + ": " + from.getY() +"(" + player.getLocation().getY() + ") -> " + to.getY()+ (toOnGround?"(onground)":""));
            if (cc.survivalFlyAccounting) System.out.println(player.getName() + " h=" + data.hDistSum.getScore(1f)+"/" + data.hDistSum.getScore(1) + " , v=" + data.vDistSum.getScore(1f)+"/"+data.vDistSum.getScore(1) );
        }

        // Did the player move in unexpected ways?// Did the player move in unexpected ways?
        if (result > 0D) {
//            System.out.println(BlockProperties.isStairs(from.getTypeIdBelow()) + " / " + BlockProperties.isStairs(to.getTypeIdBelow()));
            // Increment violation counter.
            data.survivalFlyVL += result;
            data.clearAccounting();
            data.survivalFlyJumpPhase = 0;
            // If the other plugins haven't decided to cancel the execution of the actions, then do it. If one of the
            // actions was a cancel, cancel it.
            final ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, result, cc.survivalFlyActions);
            if (vd.needsParameters()){
                vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
                vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
                vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", to.getLocation().distance(from.getLocation())));
                vd.setParameter(ParameterName.TAGS, CheckUtils.join(tags, "+"));
            }
            data.survivalFlyVLTime = now;
            if (executeActions(vd)){
                data.survivalFlyLastYDist = Double.MAX_VALUE;
                // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()" to
                // allow the player to look somewhere else despite getting pulled back by NoCheatPlus.
                return new Location(player.getWorld(), data.setBack.getX(), data.setBack.getY(), data.setBack.getZ(),
                        to.getYaw(), to.getPitch());
            }
        }
        else{
            // Slowly reduce the level with each event, if violations have not recently happened.
            if (now - data.survivalFlyVLTime > cc.survivalFlyVLFreeze) data.survivalFlyVL *= 0.95D;
        }
        
        // Violation or not, apply reset conditions (cancel would have returned above).
        final boolean resetTo = toOnGround || to.isInLiquid()  || to.isOnLadder()|| to.isInWeb();
        if (resetTo){
            // The player has moved onto ground.
            data.setBack = to.getLocation();
            data.survivalFlyJumpPhase = 0;
            data.clearAccounting();
        }
        else if (resetFrom){
            // The player moved from ground.
            data.setBack = from.getLocation();
            data.survivalFlyJumpPhase = 1; // TODO: ?
            data.clearAccounting();
        }
        data.survivalFlyLastYDist = yDistance;
        return null;
    }

    /**
     * Keep track of values, demanding that with time the values decrease.<br>
     * The ActionFrequency objects have 3 buckets.
     * @param now
     * @param value
     * @param sum
     * @param count
     * @param tags
     * @param tag
     * @return
     */
	private static final double doAccounting(final long now, final double value, final ActionFrequency sum, final ActionFrequency count, final ArrayList<String> tags, String tag)
	{
		sum.add(now, (float) value);
		count.add(now, 1f);
		if (count.getScore(2) > 0 && count.getScore(1) > 0) {
			final float sc0 = sum.getScore(1);
			final float sc1 = sum.getScore(2);
			if (sc0 < sc1 || value < 3.9 && sc0 == sc1) {
				tags.add(tag);
				return 	sc0 - sc1;
			}
		}
		return 0;
	}
}
