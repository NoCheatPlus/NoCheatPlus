package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.HashMap;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * The counterpart to the FlyingCheck. People that are not allowed to fly at all
 * get checked by this. It will try to identify when they are jumping, check if
 * they aren't jumping too high or far, check if they aren't moving too fast on
 * normal ground, while sneaking or while swimming.
 * 
 * @author Evenprime
 * 
 */
public class RunningCheck {

    private final static double  maxBonus     = 1D;

    // How many move events can a player have in air before he is expected to
    // lose altitude (or eventually land somewhere)
    private final static int     jumpingLimit = 6;

    // How high may a player get compared to his last location with ground
    // contact
    private final static double  jumpHeight   = 1.35D;

    private final ActionExecutor action;

    public RunningCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
    }

    public Location check(final Player player, final Location from, final Location to, final MovingEventHelper helper, final ConfigurationCache cc, final MovingData data) {

        // Calculate some distances
        final double xDistance = to.getX() - from.getX();
        final double zDistance = to.getZ() - from.getZ();
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        if(data.movingsetBackPoint == null) {
            data.movingsetBackPoint = player.getLocation().clone();
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

        // Slowly reduce the level with each event
        data.movingViolationLevel *= 0.97;

        if(result > 0) {

            // Increment violation counter
            data.movingViolationLevel += result;

            // Prepare some event-specific values for logging and custom actions
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.DISTANCE, String.format(Locale.US, "%.2f,%.2f,%.2f", xDistance, to.getY() - from.getY(), zDistance));
            params.put(LogAction.LOCATION_TO, String.format(Locale.US, "%.2f,%.2f,%.2f", to.getX(), to.getY(), to.getZ()));
            if(resultHoriz > 0 && resultVert > 0)
                params.put(LogAction.CHECK, "running/both");
            else if(resultHoriz > 0)
                params.put(LogAction.CHECK, "running/horizontal");
            else if(resultVert > 0)
                params.put(LogAction.CHECK, "running/vertical");

            boolean cancel = action.executeActions(player, cc.moving.runningActions, (int) data.movingViolationLevel, params, cc);

            // Was one of the actions a cancel? Then do it
            if(cancel) {
                newToLocation = data.movingsetBackPoint;
            }
        } else {
            if((toInGround && from.getY() >= to.getY()) || helper.isLiquid(toType)) {
                data.movingsetBackPoint = to.clone();
                data.movingsetBackPoint.setY(Math.ceil(data.movingsetBackPoint.getY()));
                data.jumpPhase = 0;
            } else if(toOnGround && (from.getY() >= to.getY() || data.movingsetBackPoint.getY() <= Math.floor(to.getY()))) {
                data.movingsetBackPoint = to.clone();
                data.movingsetBackPoint.setY(Math.floor(data.movingsetBackPoint.getY()));
                data.jumpPhase = 0;
            } else if(fromOnGround || fromInGround || toOnGround || toInGround) {
                data.jumpPhase = 0;
            }
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

        if(cc.moving.sneakingCheck && player.isSneaking() && !player.hasPermission(Permissions.MOVE_SNEAK)) {
            distanceAboveLimit = totalDistance - cc.moving.sneakingSpeedLimit - data.horizFreedom;
        } else if(cc.moving.swimmingCheck && isSwimming && !player.hasPermission(Permissions.MOVE_SWIM)) {
            distanceAboveLimit = totalDistance - cc.moving.swimmingSpeedLimit - data.horizFreedom;
        } else {
            distanceAboveLimit = totalDistance - cc.moving.runningSpeedLimit - data.horizFreedom;
        }

        // Did he go too far?
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
        final double fromY = from.getY();

        double limit = data.vertFreedom + jumpHeight;

        final Location l;

        if(fromY - toY > 0.5D) {
            distanceAboveLimit = 0;
            data.jumpPhase++;
        } else {

            if(data.movingsetBackPoint == null)
                l = from;
            else
                l = data.movingsetBackPoint;

            if(data.jumpPhase > jumpingLimit) {
                limit -= (data.jumpPhase - jumpingLimit) * 0.15D;
            }
            distanceAboveLimit = toY - l.getY() - limit;
        }

        return distanceAboveLimit;

    }
}
