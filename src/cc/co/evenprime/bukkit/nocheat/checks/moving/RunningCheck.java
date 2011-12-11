package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCMoving;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

/**
 * The counterpart to the FlyingCheck. People that are not allowed to fly
 * get checked by this. It will try to identify when they are jumping, check if
 * they aren't jumping too high or far, check if they aren't moving too fast on
 * normal ground, while sprinting, sneaking or swimming.
 * 
 */
public class RunningCheck extends MovingCheck {

    private final static double maxBonus     = 1D;

    // How many move events can a player have in air before he is expected to
    // lose altitude (or eventually land somewhere)
    private final static int    jumpingLimit = 6;

    private final NoFallCheck   noFallCheck;

    public RunningCheck(NoCheat plugin) {

        super(plugin, "moving.running", null);

        this.noFallCheck = new NoFallCheck(plugin);
    }

    public PreciseLocation check(NoCheatPlayer player, MovingData data, CCMoving cc) {

        // Some shortcuts:
        final PreciseLocation setBack = data.runflySetBackPoint;
        final PreciseLocation to = data.to;
        final PreciseLocation from = data.from;

        // Calculate some distances
        final double xDistance = data.to.x - from.x;
        final double zDistance = to.z - from.z;
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        if(!setBack.isSet()) {
            setBack.set(from);
        }

        // To know if a player "is on ground" is useful
        final int fromType = CheckUtil.isLocationOnGround(player.getPlayer().getWorld(), from);
        final int toType = CheckUtil.isLocationOnGround(player.getPlayer().getWorld(), to);

        final boolean fromOnGround = CheckUtil.isOnGround(fromType);
        final boolean fromInGround = CheckUtil.isInGround(fromType);
        final boolean toOnGround = CheckUtil.isOnGround(toType);
        final boolean toInGround = CheckUtil.isInGround(toType);

        PreciseLocation newToLocation = null;

        final double resultHoriz = Math.max(0.0D, checkHorizontal(player, data, CheckUtil.isLiquid(fromType) && CheckUtil.isLiquid(toType), horizontalDistance, cc));
        final double resultVert = Math.max(0.0D, checkVertical(data, fromOnGround, toOnGround, cc));

        final double result = (resultHoriz + resultVert) * 100;

        data.jumpPhase++;

        // Slowly reduce the level with each event
        data.runflyVL *= 0.95;

        if(result > 0) {

            // Increment violation counter
            data.runflyVL += result;

            if(data.checknamesuffix.equals("sneaking")) {
                data.runflySneakingTotalVL += result;
                data.runflySneakingFailed++;
            } else if(data.checknamesuffix.equals("swimming")) {
                data.runflySwimmingTotalVL += result;
                data.runflySwimmingFailed++;
            } else if(data.checknamesuffix.equals("vertical")) {
                data.runflyFlyingTotalVL += result;
                data.runflyFlyingFailed++;
            } else {
                data.runflyRunningTotalVL += result;
                data.runflyRunningFailed++;
            }

            boolean cancel = executeActions(player, cc.actions.getActions(data.runflyVL));

            // Was one of the actions a cancel? Then do it
            if(cancel) {
                newToLocation = setBack;
            } else if(toOnGround || toInGround) {
                // In case it only gets logged, not stopped by NoCheat
                // Update the setback location at least a bit
                setBack.set(to);
                data.jumpPhase = 0;

            }
        } else {
            if((toInGround && from.y >= to.y) || CheckUtil.isLiquid(toType)) {
                setBack.set(to);
                setBack.y = Math.ceil(setBack.y);
                data.jumpPhase = 0;
            } else if(toOnGround && (from.y >= to.y || setBack.y <= Math.floor(to.y))) {
                setBack.set(to);
                setBack.y = Math.floor(setBack.y);
                data.jumpPhase = 0;
            } else if(fromOnGround || fromInGround || toOnGround || toInGround) {
                data.jumpPhase = 0;
            }
        }

        /********* EXECUTE THE NOFALL CHECK ********************/
        final boolean checkNoFall = cc.nofallCheck && !player.hasPermission(Permissions.MOVING_NOFALL);

        if(checkNoFall && newToLocation == null) {
            data.fromOnOrInGround = fromOnGround || fromInGround;
            data.toOnOrInGround = toOnGround || toInGround;
            noFallCheck.check(player, data, cc);
        }

        return newToLocation;
    }

    /**
     * Calculate how much the player failed this check
     * 
     */
    private double checkHorizontal(final NoCheatPlayer player, final MovingData data, final boolean isSwimming, final double totalDistance, final CCMoving cc) {

        // How much further did the player move than expected??
        double distanceAboveLimit = 0.0D;

        // A player is considered sprinting if the flag is set and if he has
        // enough food level (configurable)
        final boolean sprinting = player.isSprinting() && (player.getPlayer().getFoodLevel() > 5 || cc.allowHungrySprinting);

        double limit = 0.0D;

        String suffix = null;

        if(cc.sneakingCheck && player.getPlayer().isSneaking() && !player.hasPermission(Permissions.MOVING_SNEAKING)) {
            limit = cc.sneakingSpeedLimit;
            suffix = "sneaking";
        } else if(cc.swimmingCheck && isSwimming && !player.hasPermission(Permissions.MOVING_SWIMMING)) {
            limit = cc.swimmingSpeedLimit;
            suffix = "swimming";
        } else if(!sprinting) {
            limit = cc.walkingSpeedLimit;
            suffix = "walking";
        } else {
            limit = cc.sprintingSpeedLimit;
            suffix = "sprinting";
        }

        // Taken directly from Minecraft code, should work
        limit *= player.getSpeedAmplifier();

        distanceAboveLimit = totalDistance - limit - data.horizFreedom;

        data.bunnyhopdelay--;

        // Did he go too far?
        if(distanceAboveLimit > 0 && sprinting) {

            // Try to treat it as a the "bunnyhop" problem
            if(data.bunnyhopdelay <= 0 && distanceAboveLimit > 0.05D && distanceAboveLimit < 0.4D) {
                data.bunnyhopdelay = 3;
                distanceAboveLimit = 0;
            }
        }

        if(distanceAboveLimit > 0) {
            // Try to consume the "buffer"
            distanceAboveLimit -= data.horizontalBuffer;
            data.horizontalBuffer = 0;

            // Put back the "overconsumed" buffer
            if(distanceAboveLimit < 0) {
                data.horizontalBuffer = -distanceAboveLimit;
            }
        }
        // He was within limits, give the difference as buffer
        else {
            data.horizontalBuffer = Math.min(maxBonus, data.horizontalBuffer - distanceAboveLimit);
        }

        if(distanceAboveLimit > 0) {
            data.checknamesuffix = suffix;
        }

        return distanceAboveLimit;
    }

    /**
     * Calculate if and how much the player "failed" this check.
     * 
     */
    private double checkVertical(final MovingData data, final boolean fromOnGround, final boolean toOnGround, final CCMoving cc) {

        // How much higher did the player move than expected??
        double distanceAboveLimit = 0.0D;

        double limit = data.vertFreedom + cc.jumpheight;

        if(data.jumpPhase > jumpingLimit) {
            limit -= (data.jumpPhase - jumpingLimit) * 0.15D;
        }
        distanceAboveLimit = data.to.y - data.runflySetBackPoint.y - limit;

        if(distanceAboveLimit > 0) {
            data.checknamesuffix = "vertical";
        }
        return distanceAboveLimit;

    }

    @Override
    public boolean isEnabled(CCMoving moving) {
        return moving.runflyCheck && !moving.allowFlying;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.CHECK)
            // Workaround for something until I find a better way to do it
            return getName() + "." + player.getData().moving.checknamesuffix;
        else if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) player.getData().moving.runflyVL);
        else
            return super.getParameter(wildcard, player);
    }
}
