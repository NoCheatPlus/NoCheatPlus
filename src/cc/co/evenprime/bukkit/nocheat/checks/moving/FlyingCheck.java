package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.HashMap;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * A check designed for people that are allowed to fly, but not as fast as they
 * want. The complement to the "RunningCheck", which is for people that aren't
 * allowed to fly, and therefore have tighter rules to obey.
 * 
 * @author Evenprime
 * 
 */
public class FlyingCheck {

    private final ActionExecutor action;

    public FlyingCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
    }

    public Location check(Player player, Location from, Location to, ConfigurationCache cc, MovingData data) {

        if(data.movingsetBackPoint == null) {
            data.movingsetBackPoint = player.getLocation().clone();
        }

        final double yDistance = to.getY() - from.getY();

        // Calculate some distances
        final double xDistance = to.getX() - from.getX();
        final double zDistance = to.getZ() - from.getZ();
        final double horizontalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        double result = 0;
        Location newToLocation = null;

        // super simple, just check distance compared to max distance
        result += Math.max(0.0D, yDistance - data.vertFreedom - cc.moving.flyingSpeedLimitVertical);
        result += Math.max(0.0D, horizontalDistance - data.horizFreedom - cc.moving.flyingSpeedLimitHorizontal);

        result = result * 100;

        if(result > 0) {

            // Increment violation counter
            data.movingViolationLevel += result;

            // Prepare some event-specific values for logging and custom actions
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.DISTANCE, String.format(Locale.US, "%.2f,%.2f,%.2f", xDistance, yDistance, zDistance));
            params.put(LogAction.LOCATION_TO, String.format(Locale.US, "%.2f,%.2f,%.2f", to.getX(), to.getY(), to.getZ()));
            params.put(LogAction.CHECK, "flyingspeed");

            boolean cancel = action.executeActions(player, cc.moving.flyingActions, (int) data.movingViolationLevel, params, cc);

            // Was one of the actions a cancel? Then really do it
            if(cancel) {
                newToLocation = data.movingsetBackPoint;
            }
        }

        // Slowly reduce the level with each event
        data.movingViolationLevel *= 0.97;

        // Some other cleanup 'n' stuff
        if(newToLocation == null) {
            data.movingsetBackPoint = to.clone();
        }

        data.jumpPhase = 0;
        return newToLocation;
    }

}
