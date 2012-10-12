package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
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
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {
        final long now = System.currentTimeMillis();

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
            // TODO: check versus last to position ?
            final boolean inconsistent = yDistance > 0 && yDistance < 0.5 && data.survivalFlyLastYDist < 0 
                    && setBackYDistance > 0D && setBackYDistance <= 1.5D;
            boolean useWorkaround = from.isAboveStairs();
            if (inconsistent){
                if (cc.debug) System.out.println(player.getName() + " Y-INCONSISTENCY");
                if (!useWorkaround && data.fromX != Double.MAX_VALUE){
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
                        useWorkaround = BlockProperties.isOnGround(from.getBlockAccess(), Math.min(data.fromX, from.getX()) - r, iY - cc.yOnGround, Math.min(data.fromZ, from.getZ()) - r, Math.max(data.fromX, from.getX()) + r, iY + 0.25, Math.max(data.fromZ, from.getZ()) + r);

                    }
                }  
            }
            if (useWorkaround){ // !toOnGround && to.isAboveStairs()) {
                // Set the new setBack and reset the jumpPhase.
                
                // Maybe don't adapt the setback (unless null)!
                data.setBack = from.getLocation();
                data.setBack.setY(Location.locToBlock(data.setBack.getY()));
                // data.ground ?
                // ? set jumpphase to height / 0.15 ?
                data.survivalFlyJumpPhase = 0;
                data.clearAccounting();
                // Tell NoFall that we assume the player to have been on ground somehow.
                data.noFallAssumeGround = true;
                resetFrom = true;
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
        final EntityPlayer entity = ((CraftPlayer) player).getHandle();
        if (entity.hasEffect(MobEffectList.FASTER_MOVEMENT))
            hAllowedDistance *= 1.0D + 0.2D * (entity.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);
        
        // Account for flowing liquids (only if needed).
        if (hDistance > swimmingSpeed && from.isInLiquid() && from.isDownStream(xDistance, zDistance)){
                hAllowedDistance *= modDownStream;
        }

        // Judge if horizontal speed is above limit.
        double hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom;

        // Prevent players from walking on a liquid.
        // TODO: yDistance == 0D <- should there not be a tolerance +- or 0...x ?
        if (hDistanceAboveLimit <= 0D && hDistance > 0.1D && yDistance == 0D
                && BlockProperties.isLiquid(to.getTypeId()) && !toOnGround
                && to.getY() % 1D < 0.8D)
            hDistanceAboveLimit = hDistance;

        // Prevent players from sprinting if they're moving backwards.
        if (hDistanceAboveLimit <= 0D && sprinting) {
            final float yaw = from.getYaw();
            if (xDistance < 0D && zDistance > 0D && yaw > 180F && yaw < 270F || xDistance < 0D && zDistance < 0D
                    && yaw > 270F && yaw < 360F || xDistance > 0D && zDistance < 0D && yaw > 0F && yaw < 90F
                    || xDistance > 0D && zDistance > 0D && yaw > 90F && yaw < 180F){
            	// Assumes permission check to be the heaviest (might be mistaken).
            	if (!player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPRINTING)) hDistanceAboveLimit = hDistance;
            }
        }

        data.bunnyhopDelay--;

        // Did he go too far?
        if (hDistanceAboveLimit > 0 && sprinting)
            // Try to treat it as a the "bunnyhop" problem.
            if (data.bunnyhopDelay <= 0 && hDistanceAboveLimit > 0.05D && hDistanceAboveLimit < 0.28D) {
                data.bunnyhopDelay = 9;
                hDistanceAboveLimit = 0D;
            }

        if (hDistanceAboveLimit > 0D) {
            // Try to consume the "buffer".
            hDistanceAboveLimit -= data.horizontalBuffer;
            data.horizontalBuffer = 0D;

            // Put back the "overconsumed" buffer.
            if (hDistanceAboveLimit < 0D)
                data.horizontalBuffer = -hDistanceAboveLimit;
        } else
            data.horizontalBuffer = Math.min(1D, data.horizontalBuffer - hDistanceAboveLimit);

        // Potion effect "Jump".
        double jumpAmplifier = 1D;
        if (entity.hasEffect(MobEffectList.JUMP)) {
            final int amplifier = entity.getEffect(MobEffectList.JUMP).getAmplifier();
            if (amplifier > 20)
                jumpAmplifier = 1.5D * (amplifier + 1D);
            else
                jumpAmplifier = 1.2D * (amplifier + 1D);
        }
        if (jumpAmplifier > data.jumpAmplifier)
            data.jumpAmplifier = jumpAmplifier;

        // Calculate the vertical speed limit based on the current jump phase.
        double vAllowedDistance, vDistanceAboveLimit;
        if (from.isInWeb()){
        	// Very simple: force players to descend or stay.
         	vAllowedDistance = from.isOnGround() ? 0.1D : 0;
        	data.jumpAmplifier = 0;
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
        			data.survivalFlyLastYDist = Integer.MAX_VALUE;
        			return data.setBack;
        		}
        	}
        }
        else{
        	vAllowedDistance = (!(fromOnGround || data.noFallAssumeGround) && !toOnGround ? 1.45D : 1.35D) + data.verticalFreedom;
            vAllowedDistance *= data.jumpAmplifier;
            if (data.survivalFlyJumpPhase > 6 + data.jumpAmplifier && data.verticalVelocityCounter <= 0){
            	vAllowedDistance -= (data.survivalFlyJumpPhase - 6) * 0.15D;
            }

            vDistanceAboveLimit = to.getY() - data.setBack.getY() - vAllowedDistance;
            
//            System.out.println("vda = " +vDistanceAboveLimit + " / vc = " + data.verticalVelocityCounter + " / vf = " + data.verticalFreedom + " / v = " + player.getVelocity().length());

            // Step can also be blocked.
            if ((fromOnGround || data.noFallAssumeGround) && toOnGround && Math.abs(yDistance - 1D) <= cc.yStep && vDistanceAboveLimit <= 0D
                    && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_STEP))
                vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));

        }
        if (data.noFallAssumeGround || fromOnGround || toOnGround)
            data.jumpAmplifier = 0D;
        
        if (cc.survivalFlyAccounting && !resetFrom){
            final boolean useH = data.horizontalFreedom <= 0.001D;
            final boolean useV = data.verticalFreedom <= 0.001D;
            if (useH){
                data.hDistSum.add(now, (float) hDistance);
                data.hDistCount.add(now,  1f);
            }
            if (useV){
                data.vDistSum.add(now, (float) (yDistance));
                data.vDistCount.add(now,  1f);
            }
            if (useH && data.hDistCount.getScore(2) > 0 && data.hDistCount.getScore(1) > 0){
                final float hsc0 = data.hDistSum.getScore(1);
                final float hsc1 = data.hDistSum.getScore(2);
                if (hsc0 < hsc1 || hDistance < 3.9 && hsc0 == hsc1){
                    hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hsc0 - hsc1);
                }
            }
            if (useV && data.vDistCount.getScore(2) > 0 && data.vDistCount.getScore(1) > 0){
                final float vsc0 = data.vDistSum.getScore(1);
                final float vsc1 = data.vDistSum.getScore(2);
                if (vsc0 < vsc1 || yDistance < 3.9 && vsc0 == vsc1){
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, vsc0 - vsc1);
                }
            }
        }
        
        final double result = (Math.max(hDistanceAboveLimit, 0D) + Math.max(vDistanceAboveLimit, 0D)) * 100D;

        data.survivalFlyJumpPhase++;

        // Slowly reduce the level with each event.
        data.survivalFlyVL *= 0.95D;
        
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
            }
            if (executeActions(vd)){
                data.survivalFlyLastYDist = Integer.MAX_VALUE;
                // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()" to
                // allow the player to look somewhere else despite getting pulled back by NoCheatPlus.
                return new Location(player.getWorld(), data.setBack.getX(), data.setBack.getY(), data.setBack.getZ(),
                        to.getYaw(), to.getPitch());
            }
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
        data.fromX = from.getX();
        data.fromY = from.getY();
        data.fromZ = from.getZ();
        data.toY = to.getY();
        return null;
    }
    
}
