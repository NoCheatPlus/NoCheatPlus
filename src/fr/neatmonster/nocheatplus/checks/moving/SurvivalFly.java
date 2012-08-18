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
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to) {
        final MovingConfig cc = MovingConfig.getConfig(player);
        final MovingData data = MovingData.getData(player);

        // A player is considered sprinting if the flag is set and if he has enough food level.
        final boolean sprinting = player.isSprinting() && player.getFoodLevel() > 5;

        // If we don't have any setBack, choose the location the player comes from.
        if (data.setBack == null)
            data.setBack = from.getLocation();

        // Player on ice? Give him higher max speed.
        if (from.isOnIce() || to.isOnIce())
            data.survivalFlyOnIce = 20;
        else if (data.survivalFlyOnIce > 0)
            data.survivalFlyOnIce--;

        // Choose the right horizontal speed limit for the current activity.
        double hAllowedDistance = 0D;
        if (player.isSneaking() && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SNEAKING))
            hAllowedDistance = 0.14D * cc.survivalFlySneakingSpeed / 100D;
        else if (player.isBlocking() && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))
            hAllowedDistance = 0.16D * cc.survivalFlyBlockingSpeed / 100D;
        else if (from.isInWater() && to.isInWater())
            hAllowedDistance = 0.18D * cc.survivalFlySwimmingSpeed / 100D;
        else if (!sprinting)
            hAllowedDistance = 0.22D * cc.survivalFlyWalkingSpeed / 100D;
        else
            hAllowedDistance = 0.35D * cc.survivalFlySprintingSpeed / 100D;

        // If the player is on ice, give him an higher maximum speed.
        if (data.survivalFlyOnIce > 0)
            hAllowedDistance *= 2.5D;

        // Taken directly from Minecraft code, should work.
        final EntityPlayer entity = ((CraftPlayer) player).getHandle();
        if (entity.hasEffect(MobEffectList.FASTER_MOVEMENT))
            hAllowedDistance *= 1.0D + 0.2D * (entity.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);

        // Calculate some distances.
        final double xDistance = to.getX() - from.getX();
        final double yDistance = to.getY() - from.getY();
        final double zDistance = to.getZ() - from.getZ();
        final double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
        double hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom;

        // Prevent players from walking on a liquid.
        if (hDistanceAboveLimit <= 0D && hDistance > 0.1D && yDistance == 0D
                && MovingListener.isLiquid(to.getLocation().getBlock().getType()) && !to.isOnGround()
                && to.getY() % 1D < 0.8D)
            hDistanceAboveLimit = hDistance;

        // Prevent players from sprinting if they're moving backwards.
        if (hDistanceAboveLimit <= 0D && sprinting && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPRINTING)) {
            final float yaw = from.getYaw();
            if (xDistance < 0D && zDistance > 0D && yaw > 180F && yaw < 270F || xDistance < 0D && zDistance < 0D
                    && yaw > 270F && yaw < 360F || xDistance > 0D && zDistance < 0D && yaw > 0F && yaw < 90F
                    || xDistance > 0D && zDistance > 0D && yaw > 90F && yaw < 180F)
                hDistanceAboveLimit = hDistance;
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
                jumpAmplifier = 1.5D * (entity.getEffect(MobEffectList.JUMP).getAmplifier() + 1D);
            else
                jumpAmplifier = 1.2D * (entity.getEffect(MobEffectList.JUMP).getAmplifier() + 1D);
        }
        if (jumpAmplifier > data.jumpAmplifier)
            data.jumpAmplifier = jumpAmplifier;

        // If the player has touched the ground but it hasn't been noticed by the plugin, the workaround is here.
        final double setBackYDistance = to.getY() - data.setBack.getY();
        if (!from.isOnGround()
                && (from.getY() < data.survivalFlyLastFromY && yDistance > 0D && yDistance < 0.5D
                        && setBackYDistance > 0D && setBackYDistance <= 1.5D || !to.isOnGround() && to.isAboveStairs())) {
            // Set the new setBack and reset the jumpPhase.
            data.setBack = from.getLocation();
            data.setBack.setY(Math.floor(data.setBack.getY()));
            data.survivalFlyJumpPhase = 0;
            // Reset the no fall data.
            data.clearNoFallData();
        }
        data.survivalFlyLastFromY = from.getY();

        // Calculate the vertical speed limit based on the current jump phase.
        double vAllowedDistance = (!from.isOnGround() && !to.isOnGround() ? 1.45D : 1.35D) + data.verticalFreedom;
        vAllowedDistance *= data.jumpAmplifier;
        if (data.survivalFlyJumpPhase > 6 + data.jumpAmplifier)
            vAllowedDistance -= (data.survivalFlyJumpPhase - 6) * 0.15D;

        final double vDistanceAboveLimit = to.getY() - data.setBack.getY() - vAllowedDistance;

        if (from.isOnGround() || to.isOnGround())
            data.jumpAmplifier = 0D;

        final double result = (Math.max(hDistanceAboveLimit, 0D) + Math.max(vDistanceAboveLimit, 0D)) * 100D;

        data.survivalFlyJumpPhase++;

        // Slowly reduce the level with each event.
        data.survivalFlyVL *= 0.95D;

        // Did the player move in unexpected ways?
        if (result > 0D) {
            // Increment violation counter.
            data.survivalFlyVL += result;

            // If the other plugins haven't decided to cancel the execution of the actions, then do it. If one of the
            // actions was a cancel, cancel it.
            if (executeActions(player, data.survivalFlyVL, result, MovingConfig.getConfig(player).survivalFlyActions))
                // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()" to
                // allow the player to look somewhere else despite getting pulled back by NoCheatPlus.
                return new Location(player.getWorld(), data.setBack.getX(), data.setBack.getY(), data.setBack.getZ(),
                        to.getYaw(), to.getPitch());
            else if (to.isInLiquid() || to.isInWeb() || to.isOnGround() || to.isOnLadder()) {
                // In case it only gets logged, not stopped by NoCheatPlus, update the setback location at least a bit.
                data.setBack = to.getLocation();
                data.survivalFlyJumpPhase = 0;
            }
        }

        // Decide if we should create a new setBack point. These are the result of a lot of bug reports, experience and
        // trial and error.
        else if (to.isInLiquid()) {
            // If the player moved into liquid.
            data.setBack = to.getLocation();
            data.setBack.setY(Math.ceil(data.setBack.getY()));
            data.survivalFlyJumpPhase = 0;
        } else if ((to.isInWeb() || to.isOnLadder() || to.isOnGround())
                && (from.getY() >= to.getY() || data.setBack.getY() <= Math.floor(to.getY()))) {
            // If the player moved down "onto" the ground and the new setback point is higher up than the old or at
            // least at the same height, or if the player is in web or on a ladder.
            data.setBack = to.getLocation();
            data.survivalFlyJumpPhase = 0;
        } else {
            if (from.isInLiquid() || from.isOnGround() || from.isInWeb() || from.isOnGround())
                data.setBack = from.getLocation();
            if (from.isInLiquid() || to.isInLiquid() || from.isInWeb() || to.isInWeb() || from.isOnGround()
                    || to.isOnGround() || from.isOnLadder() || to.isOnLadder())
                // The player at least touched the ground somehow.
                data.survivalFlyJumpPhase = 0;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName,
     * org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        final MovingData data = MovingData.getData(violationData.player);
        if (wildcard == ParameterName.LOCATION_FROM)
            return String.format(Locale.US, "%.2f, %.2f, %.2f", data.from.getX(), data.from.getY(), data.from.getZ());
        else if (wildcard == ParameterName.LOCATION_TO)
            return String.format(Locale.US, "%.2f, %.2f, %.2f", data.to.getX(), data.to.getY(), data.to.getZ());
        else if (wildcard == ParameterName.DISTANCE)
            return String.format(Locale.US, "%.2f", data.to.subtract(data.from).lengthSquared());
        else
            return super.getParameter(wildcard, violationData);
    }
}
