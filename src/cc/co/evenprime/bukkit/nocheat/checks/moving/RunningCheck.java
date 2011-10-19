package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * The counterpart to the FlyingCheck. People that are not allowed to fly
 * get checked by this. It will try to identify when they are jumping, check if
 * they aren't jumping too high or far, check if they aren't moving too fast on
 * normal ground, while sprinting, sneaking or swimming.
 * 
 * @author Evenprime
 * 
 */
public class RunningCheck {

    private final static double maxBonus     = 1D;

    // How many move events can a player have in air before he is expected to
    // lose altitude (or eventually land somewhere)
    private final static int    jumpingLimit = 6;

    private final NoCheat       plugin;

    private final NoFallCheck   noFallCheck;

    public RunningCheck(NoCheat plugin, NoFallCheck noFallCheck) {
        this.plugin = plugin;
        this.noFallCheck = noFallCheck;
    }

    public Location check(final Player player, final Location from, final Location to, final ConfigurationCache cc) {

        // Calculate some distances
        final double xDistance = to.getX() - from.getX();
        final double zDistance = to.getZ() - from.getZ();
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        BaseData data = plugin.getData(player);

        if(data.moving.runflySetBackPoint == null) {
            data.moving.runflySetBackPoint = from.clone();
        }

        // To know if a player "is on ground" is useful
        final int fromType = CheckUtil.isLocationOnGround(from.getWorld(), from.getX(), from.getY(), from.getZ(), false);
        final int toType = CheckUtil.isLocationOnGround(to.getWorld(), to.getX(), to.getY(), to.getZ(), false);

        final boolean fromOnGround = CheckUtil.isOnGround(fromType);
        final boolean fromInGround = CheckUtil.isInGround(fromType);
        final boolean toOnGround = CheckUtil.isOnGround(toType);
        final boolean toInGround = CheckUtil.isInGround(toType);

        Location newToLocation = null;

        double resultHoriz = Math.max(0.0D, checkHorizontal(player, CheckUtil.isLiquid(fromType) && CheckUtil.isLiquid(toType), horizontalDistance, cc));
        double resultVert = Math.max(0.0D, checkVertical(player, from, fromOnGround, to, toOnGround, cc));

        double result = (resultHoriz + resultVert) * 100;

        data.moving.jumpPhase++;

        // Slowly reduce the level with each event
        data.moving.runflyViolationLevel *= 0.97;

        if(result > 0) {

            // Increment violation counter
            data.moving.runflyViolationLevel += result;

            // Prepare some event-specific values for logging and custom actions
            data.log.toLocation = to;
            if(resultHoriz > 0 && resultVert > 0)
                data.log.check = "runfly/both";
            else if(resultHoriz > 0)
                data.log.check = "runfly/horizontal";
            else if(resultVert > 0)
                data.log.check = "runfly/vertical";

            boolean cancel = plugin.execute(player, cc.moving.actions, (int) data.moving.runflyViolationLevel, data.moving.history, cc);

            // Was one of the actions a cancel? Then do it
            if(cancel) {
                newToLocation = data.moving.runflySetBackPoint;
            }
        } else {
            if((toInGround && from.getY() >= to.getY()) || CheckUtil.isLiquid(toType)) {
                data.moving.runflySetBackPoint = to.clone();
                data.moving.runflySetBackPoint.setY(Math.ceil(data.moving.runflySetBackPoint.getY()));
                data.moving.jumpPhase = 0;
            } else if(toOnGround && (from.getY() >= to.getY() || data.moving.runflySetBackPoint.getY() <= Math.floor(to.getY()))) {
                data.moving.runflySetBackPoint = to.clone();
                data.moving.runflySetBackPoint.setY(Math.floor(data.moving.runflySetBackPoint.getY()));
                data.moving.jumpPhase = 0;
            } else if(fromOnGround || fromInGround || toOnGround || toInGround) {
                data.moving.jumpPhase = 0;
            }
        }

        /********* EXECUTE THE NOFALL CHECK ********************/
        final boolean checkNoFall = cc.moving.nofallCheck && !player.hasPermission(Permissions.MOVE_NOFALL);

        if(checkNoFall && newToLocation == null) {
            noFallCheck.check(player, from, fromOnGround || fromInGround, to, toOnGround || toInGround, cc);
        }

        return newToLocation;
    }

    /**
     * Calculate how much the player failed this check
     * 
     */
    private double checkHorizontal(final Player player, final boolean isSwimming, final double totalDistance, final ConfigurationCache cc) {

        // How much further did the player move than expected??
        double distanceAboveLimit = 0.0D;

        boolean sprinting = true;
        try {
            sprinting = !(player instanceof CraftPlayer) || player.isSprinting();
        } catch(Exception e) {
            e.printStackTrace();
        }

        BaseData data = plugin.getData(player);

        if(cc.moving.sneakingCheck && player.isSneaking() && !player.hasPermission(Permissions.MOVE_SNEAK)) {
            distanceAboveLimit = totalDistance - cc.moving.sneakingSpeedLimit - data.moving.horizFreedom;
        } else if(cc.moving.swimmingCheck && isSwimming && !player.hasPermission(Permissions.MOVE_SWIM)) {
            distanceAboveLimit = totalDistance - cc.moving.swimmingSpeedLimit - data.moving.horizFreedom;
        } else if(!sprinting) {
            distanceAboveLimit = totalDistance - cc.moving.walkingSpeedLimit - data.moving.horizFreedom;
        } else {
            distanceAboveLimit = totalDistance - cc.moving.sprintingSpeedLimit - data.moving.horizFreedom;
        }

        data.moving.bunnyhopdelay--;

        // Did he go too far?
        if(distanceAboveLimit > 0 && sprinting) {

            // Try to treat it as a the "bunnyhop" problem
            if(data.moving.bunnyhopdelay <= 0 && distanceAboveLimit > 0.05D && distanceAboveLimit < 0.4D) {
                data.moving.bunnyhopdelay = 3;
                distanceAboveLimit = 0;
            }
        }

        if(distanceAboveLimit > 0) {
            // Try to consume the "buffer"
            distanceAboveLimit -= data.moving.horizontalBuffer;
            data.moving.horizontalBuffer = 0;

            // Put back the "overconsumed" buffer
            if(distanceAboveLimit < 0) {
                data.moving.horizontalBuffer = -distanceAboveLimit;
            }
        }
        // He was within limits, give the difference as buffer
        else {
            data.moving.horizontalBuffer = Math.min(maxBonus, data.moving.horizontalBuffer - distanceAboveLimit);
        }

        return distanceAboveLimit;
    }

    /**
     * Calculate if and how much the player "failed" this check.
     * 
     */
    private double checkVertical(final Player player, final Location from, final boolean fromOnGround, final Location to, final boolean toOnGround, final ConfigurationCache cc) {

        // How much higher did the player move than expected??
        double distanceAboveLimit = 0.0D;

        final double toY = to.getY();

        BaseData data = plugin.getData(player);

        double limit = data.moving.vertFreedom + cc.moving.jumpheight;

        if(data.moving.jumpPhase > jumpingLimit) {
            limit -= (data.moving.jumpPhase - jumpingLimit) * 0.15D;
        }
        distanceAboveLimit = toY - data.moving.runflySetBackPoint.getY() - limit;

        return distanceAboveLimit;

    }
}
