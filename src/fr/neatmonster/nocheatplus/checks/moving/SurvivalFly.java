package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
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
     * The event triggered by this check.
     */
    public class SurvivalFlyEvent extends CheckEvent {

        /**
         * Instantiates a new survival fly event.
         * 
         * @param player
         *            the player
         */
        public SurvivalFlyEvent(final Player player) {
            super(player);
        }
    }

    /** The common margin of error for some speeds. */
    private static final double MARGIN                  = 0.001D;

    /** The horizontal speed limit when blocking. */
    private static final double BLOCKING_MOVE           = 0.16D;

    /** The vertical speed limit when ascending into web. */
    private static final double COBWEB_ASCEND           = 0.02D + MARGIN;

    /** The vertical speed limit when descending into web. */
    private static final double COBWEB_DESCEND          = 0.062D + MARGIN;

    /** The horizontal speed limit when moving into web. */
    private static final double COBWEB_MOVE             = 0.11D;

    /** The horizontal speed amplifier when being on ice. */
    private static final double ICE_AMPLIFIER           = 2.5D;

    /** The number of events contained in a jump phase. */
    private static final int    JUMP_PHASE              = 6;

    /** The distance removed after each jumping event. */
    private static final double JUMP_STEP               = 0.15D;

    /** The vertical speed limit when ascending on a ladder. */
    private static final double LADDER_ASCEND           = 0.225D + MARGIN;

    /** The vertical speed limit when descending on a ladder. */
    private static final double LADDER_DESCEND          = 0.15D + MARGIN;

    /** The vertical speed limit when ascending into lava. */
    private static final double LAVA_ASCEND             = 0.08D + MARGIN;

    /** The vertical speed limit when descending into lava. */
    private static final double LAVA_DESCEND            = 0.085D + MARGIN;

    /** The horizontal speed limit when moving into lava. */
    private static final double LAVA_MOVE               = 0.12D;

    /** The horizontal and usual speed limit when moving. */
    private static final double MOVE                    = 0.22D;

    /** The horizontal speed limit when sneaking. */
    private static final double SNEAKING_MOVE           = 0.14D;

    /** The horizontal speed limit when moving on soul sand. */
    private static final double SOULSAND_MOVE           = 0.13D;

    /** The horizontal speed limit when sprinting on soul sand. */
    private static final double SOULSAND_SPRINTING_MOVE = 0.18D;

    /** The horizontal speed limit when sprinting. */
    private static final double SPRINTING_MOVE          = 0.37D;

    /** The vertical speed limit when ascending into water. */
    private static final double WATER_ASCEND            = 0.13D + MARGIN;

    /** The vertical speed limit when descending into water. */
    private static final double WATER_DESCEND           = 0.17D + MARGIN;

    /** The horizontal speed limit when moving into water. */
    private static final double WATER_MOVE              = 0.18D;

    /** The no fall check. */
    private final NoFall        noFall                  = new NoFall();

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

        if (data.setBack == null)
            data.setBack = from.getLocation();

        // Player on ice? Give him higher max speed.
        if (from.isOnIce() || to.isOnIce())
            data.survivalFlyOnIce = 20;
        else if (data.survivalFlyOnIce > 0)
            data.survivalFlyOnIce--;

        // A player is considered sprinting if the flag is set and if he has enough food level.
        final boolean sprinting = player.isSprinting() && player.getFoodLevel() > 5;

        boolean useBuffer = true;

        // Handle all the special cases.
        double hAllowedDistance = cc.survivalFlyMoveSpeed / 100D * MOVE;
        if (from.isInWeb() && to.isInWeb()) {
            hAllowedDistance = cc.survivalFlyCobWebSpeed / 100D * COBWEB_MOVE;
            useBuffer = false;
        } else if (from.isOnSoulSand() && to.isOnSoulSand() && !sprinting) {
            hAllowedDistance = cc.survivalFlySoulSandSpeed / 100D * SOULSAND_MOVE;
            useBuffer = false;
        } else if (from.isInLava() && to.isInLava())
            hAllowedDistance = cc.survivalFlyLavaSpeed / 100D * LAVA_MOVE;
        else if (from.isOnSoulSand() && to.isOnSoulSand() && sprinting) {
            hAllowedDistance = cc.survivalFlySoulSandSpeed / 100D * SOULSAND_SPRINTING_MOVE;
            useBuffer = false;
        } else if (player.isSneaking())
            hAllowedDistance = cc.survivalFlySneakingSpeed / 100D * SNEAKING_MOVE;
        else if (player.isBlocking())
            hAllowedDistance = cc.survivalFlyBlockingSpeed / 100D * BLOCKING_MOVE;
        else if (from.isInWater() && to.isInWater())
            hAllowedDistance = cc.survivalFlyWaterSpeed / 100D * WATER_MOVE;
        else if (player.isSprinting() && player.getFoodLevel() > 5)
            hAllowedDistance = cc.survivalFlySprintingSpeed / 100D * SPRINTING_MOVE;

        if (data.survivalFlyOnIce > 0)
            hAllowedDistance *= ICE_AMPLIFIER;

        // Taken directly from Minecraft code, should work.
        final EntityPlayer entity = ((CraftPlayer) player).getHandle();
        if (entity.hasEffect(MobEffectList.FASTER_MOVEMENT))
            hAllowedDistance *= 1.0D + 0.2D * (entity.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);

        // Calculate some distances.
        final double xDistance = to.getX() - from.getX();
        final double zDistance = to.getZ() - from.getZ();
        final double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);

        double hDistanceAboveLimit = hDistance - hAllowedDistance - data.horizontalFreedom;

        data.bunnyhopDelay--;

        if (useBuffer) {
            // Did he go too far?
            if (hDistanceAboveLimit > 0D && sprinting)
                // Try to treat it as a the "bunnyhop" problem.
                if (data.bunnyhopDelay <= 0 && hDistanceAboveLimit > 0.05D && hDistanceAboveLimit < 0.4D) {
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
                // He was within limits, give the difference as buffer.
                data.horizontalBuffer = Math.min(1D, data.horizontalBuffer - hDistanceAboveLimit);
        }

        hDistanceAboveLimit = Math.max(0D, hDistanceAboveLimit);

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

        // Remember since when the player is in lava/in water/on ladder.
        if (from.isInLava() && !to.isInLava())
            data.survivalFlyInLavaSince = 0L;
        else if (!from.isInLava() && to.isInLava())
            data.survivalFlyInLavaSince = System.currentTimeMillis();
        if (from.isInWater() && !to.isInWater())
            data.survivalFlyInWaterSince = 0L;
        else if (!from.isInWater() && to.isInWater())
            data.survivalFlyInWaterSince = System.currentTimeMillis();
        if (from.isOnLadder() && !to.isOnLadder())
            data.survivalFlyOnLadderSince = 0L;
        else if (!from.isOnLadder() && to.isOnLadder())
            data.survivalFlyOnLadderSince = System.currentTimeMillis();

        double vDistance = to.getY() - from.getY();

        // Handle all the special cases.
        double vDistanceAboveLimit = 0D;
        if (from.isInLava() && to.isInLava() && data.survivalFlyInLavaSince > 0L
                && System.currentTimeMillis() - data.survivalFlyInLavaSince > 1000L) {
            if (vDistance > cc.survivalFlyLavaSpeed / 100D * LAVA_ASCEND)
                vDistanceAboveLimit = vDistance - cc.survivalFlyLavaSpeed / 100D * LAVA_ASCEND;
            else if (vDistance < cc.survivalFlyLavaSpeed / 100D * -LAVA_DESCEND)
                vDistanceAboveLimit = cc.survivalFlyLavaSpeed / 100D * -LAVA_DESCEND - vDistance;
        } else if (from.isInWater() && to.isInWater() && data.survivalFlyInWaterSince > 0L
                && System.currentTimeMillis() - data.survivalFlyInWaterSince > 1000L) {
            if (vDistance > cc.survivalFlyWaterSpeed / 100D * WATER_ASCEND)
                vDistanceAboveLimit = vDistance - cc.survivalFlyWaterSpeed / 100D * WATER_ASCEND;
            else if (vDistance < cc.survivalFlyWaterSpeed / 100D * -WATER_DESCEND)
                vDistanceAboveLimit = cc.survivalFlyWaterSpeed / 100D * -WATER_DESCEND - vDistance;
        } else if (from.isInWeb() && to.isInWeb()) {
            if (vDistance > cc.survivalFlyCobWebSpeed / 100D * COBWEB_ASCEND)
                vDistanceAboveLimit = vDistance - cc.survivalFlyCobWebSpeed / 100D * COBWEB_ASCEND;
            else if (vDistance < cc.survivalFlyCobWebSpeed / 100D * -COBWEB_DESCEND)
                vDistanceAboveLimit = cc.survivalFlyCobWebSpeed / 100D * -COBWEB_DESCEND - vDistance;
        } else if (from.isOnLadder(true) && to.isOnLadder(true) && data.survivalFlyOnLadderSince > 0L
                && System.currentTimeMillis() - data.survivalFlyOnLadderSince > 1000L) {
            if (vDistance > cc.survivalFlyLadderSpeed / 100D * LADDER_ASCEND)
                vDistanceAboveLimit = vDistance - cc.survivalFlyLadderSpeed / 100D * LADDER_ASCEND;
            else if (vDistance < cc.survivalFlyLadderSpeed / 100D * -LADDER_DESCEND)
                vDistanceAboveLimit = cc.survivalFlyLadderSpeed / 100D * -LADDER_DESCEND - vDistance;
        } else {
            vDistance = to.getY() - data.setBack.getY();
            if (vDistance <= 0D)
                data.survivalFlyJumpPhase = 0;

            double vAllowedDistance = (data.verticalFreedom + 1.35D) * data.jumpAmplifier;
            if (data.survivalFlyJumpPhase > JUMP_PHASE + data.jumpAmplifier)
                vAllowedDistance -= (data.survivalFlyJumpPhase - JUMP_PHASE) * JUMP_STEP;

            vDistanceAboveLimit = Math.max(0D, vDistance - vAllowedDistance);
        }

        // Handle slabs placed into a liquid.
        if (from.isInLiquid()
                && to.isInLiquid()
                && (to.isOnGround() && to.getY() - from.getY() == 0.5D || !from.isOnGround() && to.isOnGround() || from
                        .isOnGround() && !to.isOnGround()))
            vDistanceAboveLimit = 0D;

        if (from.isOnGround() || to.isOnGround())
            data.jumpAmplifier = 0D;

        final double result = (hDistanceAboveLimit + vDistanceAboveLimit) * 100D;

        data.survivalFlyJumpPhase++;

        // Slowly reduce the level with each event.
        data.survivalFlyVL *= 0.95D;

        // Did the player move in unexpected ways?
        if (result > 0D) {
            // Increment violation counter.
            data.survivalFlyVL += result;

            // Dispatch a survival fly event (API).
            final SurvivalFlyEvent e = new SurvivalFlyEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // If the other plugins haven't decided to cancel the execution of the actions, then do it. If one of the
            // actions was a cancel, cancel it.
            if (!e.isCancelled() && executeActions(player, cc.survivalFlyActions, data.survivalFlyVL))
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

        if (noFall.isEnabled(player))
            // Execute the NoFall check.
            noFall.check(player, from, to);

        return null;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName,
     * org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        final MovingData data = MovingData.getData(player);
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(data.survivalFlyVL));
        else if (wildcard == ParameterName.LOCATION_FROM)
            return String.format(Locale.US, "%.2f, %.2f, %.2f", data.from.getX(), data.from.getY(), data.from.getZ());
        else if (wildcard == ParameterName.LOCATION_TO)
            return String.format(Locale.US, "%.2f, %.2f, %.2f", data.to.getX(), data.to.getY(), data.to.getZ());
        else if (wildcard == ParameterName.DISTANCE)
            return String.format(Locale.US, "%.2f", data.to.subtract(data.from).lengthSquared());
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.MOVING_SURVIVALFLY) && MovingConfig.getConfig(player).survivalFlyCheck;
    }
}
