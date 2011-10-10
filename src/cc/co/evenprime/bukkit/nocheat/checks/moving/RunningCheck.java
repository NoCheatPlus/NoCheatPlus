package cc.co.evenprime.bukkit.nocheat.checks.moving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.LogData;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

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

    private final static double  maxBonus     = 1D;

    // How many move events can a player have in air before he is expected to
    // lose altitude (or eventually land somewhere)
    private final static int     jumpingLimit = 6;

    private final ActionExecutor action;
    private final NoCheat        plugin;

    private final NoFallCheck    noFallCheck;

    public RunningCheck(NoCheat plugin, NoFallCheck noFallCheck) {
        this.plugin = plugin;
        this.action = new ActionExecutor(plugin);
        this.noFallCheck = noFallCheck;
    }

    public Location check(final Player player, final Location from, final Location to, final MovingEventHelper helper, final ConfigurationCache cc, final MovingData data) {

        // Calculate some distances
        final double xDistance = to.getX() - from.getX();
        final double zDistance = to.getZ() - from.getZ();
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        if(data.runflySetBackPoint == null) {
            data.runflySetBackPoint = from.clone();
        }

        // To know if a player "is on ground" is useful
        final int fromType = helper.isLocationOnGround(from.getWorld(), from.getX(), from.getY(), from.getZ(), false);
        final int toType = helper.isLocationOnGround(to.getWorld(), to.getX(), to.getY(), to.getZ(), false);

        final boolean fromOnGround = helper.isOnGround(fromType);
        final boolean fromInGround = helper.isInGround(fromType);
        final boolean toOnGround = helper.isOnGround(toType);
        final boolean toInGround = helper.isInGround(toType);

        Location newToLocation = null;

        double resultHoriz = Math.max(0.0D, checkHorizontal(player, helper.isLiquid(fromType) && helper.isLiquid(toType), horizontalDistance, cc, data));
        double resultVert = Math.max(0.0D, checkVertical(from, fromOnGround, to, toOnGround, cc, data));

        double result = (resultHoriz + resultVert) * 100;

        data.jumpPhase++;

        // Slowly reduce the level with each event
        data.runflyViolationLevel *= 0.97;

        if(result > 0) {

            // Increment violation counter
            data.runflyViolationLevel += result;

            // Prepare some event-specific values for logging and custom actions
            LogData ldata = plugin.getDataManager().getData(player).log;
            ldata.toLocation = to;
            if(resultHoriz > 0 && resultVert > 0)
                ldata.check = "runfly/both";
            else if(resultHoriz > 0)
                ldata.check = "runfly/horizontal";
            else if(resultVert > 0)
                ldata.check = "runfly/vertical";

            boolean cancel = action.executeActions(player, cc.moving.actions, (int) data.runflyViolationLevel, ldata, cc);

            // Was one of the actions a cancel? Then do it
            if(cancel) {
                newToLocation = data.runflySetBackPoint;
            }
        } else {
            if((toInGround && from.getY() >= to.getY()) || helper.isLiquid(toType)) {
                data.runflySetBackPoint = to.clone();
                data.runflySetBackPoint.setY(Math.ceil(data.runflySetBackPoint.getY()));
                data.jumpPhase = 0;
            } else if(toOnGround && (from.getY() >= to.getY() || data.runflySetBackPoint.getY() <= Math.floor(to.getY()))) {
                data.runflySetBackPoint = to.clone();
                data.runflySetBackPoint.setY(Math.floor(data.runflySetBackPoint.getY()));
                data.jumpPhase = 0;
            } else if(fromOnGround || fromInGround || toOnGround || toInGround) {
                data.jumpPhase = 0;
            }
        }

        /********* EXECUTE THE NOFALL CHECK ********************/
        final boolean checkNoFall = cc.moving.nofallCheck && !player.hasPermission(Permissions.MOVE_NOFALL);

        if(checkNoFall && newToLocation == null) {
            noFallCheck.check(player, from, fromOnGround || fromInGround, to, toOnGround || toInGround, cc, data);
        }

        return newToLocation;
    }

    /**
     * Calculate how much the player failed this check
     * 
     * @param isSneaking
     * @param isSwimming
     * @param totalDistance
     * @param cc
     * @param data
     * @return
     */
    private double checkHorizontal(final Player player, final boolean isSwimming, final double totalDistance, final ConfigurationCache cc, final MovingData data) {

        // How much further did the player move than expected??
        double distanceAboveLimit = 0.0D;

        boolean sprinting = true;
        try {
            sprinting = !(player instanceof CraftPlayer) || player.isSprinting();
        } catch(Exception e) {
            e.printStackTrace();
        }

        if(cc.moving.sneakingCheck && player.isSneaking() && !player.hasPermission(Permissions.MOVE_SNEAK)) {
            distanceAboveLimit = totalDistance - cc.moving.sneakingSpeedLimit - data.horizFreedom;
        } else if(cc.moving.swimmingCheck && isSwimming && !player.hasPermission(Permissions.MOVE_SWIM)) {
            distanceAboveLimit = totalDistance - cc.moving.swimmingSpeedLimit - data.horizFreedom;
        } else if(!sprinting) {
            distanceAboveLimit = totalDistance - cc.moving.walkingSpeedLimit - data.horizFreedom;
        } else {
            distanceAboveLimit = totalDistance - cc.moving.sprintingSpeedLimit - data.horizFreedom;
        }

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

        return distanceAboveLimit;
    }

    /**
     * Calculate if and how much the player "failed" this check.
     * 
     */
    private double checkVertical(final Location from, final boolean fromOnGround, final Location to, final boolean toOnGround, final ConfigurationCache cc, final MovingData data) {

        // How much higher did the player move than expected??
        double distanceAboveLimit = 0.0D;

        final double toY = to.getY();

        double limit = data.vertFreedom + cc.moving.jumpheight;

        if(data.jumpPhase > jumpingLimit) {
            limit -= (data.jumpPhase - jumpingLimit) * 0.15D;
        }
        distanceAboveLimit = toY - data.runflySetBackPoint.getY() - limit;

        return distanceAboveLimit;

    }
}
