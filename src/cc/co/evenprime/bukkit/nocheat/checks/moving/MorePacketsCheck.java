package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.HashMap;

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
 * The morePacketsCheck (previously called SpeedhackCheck) will try to identify
 * players that send more than the usual amount of move-packets to the server to
 * be able to move faster than normal, without getting caught by the other
 * checks (flying/running).
 * 
 * It monitors the number of packets sent to the server within 1 second and
 * compares it to the "legal" number of packets for that timeframe (22).
 * 
 * @author Evenprime
 * 
 */
public class MorePacketsCheck {

    private final ActionExecutor action;

    private final long           timeframe           = 1000;
    private final double         packetsPerTimeframe = 22;

    private final double         lowLimit            = -20;

    public MorePacketsCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
    }

    public Location check(Player player, ConfigurationCache cc, MovingData data) {

        if(!cc.moving.morePacketsCheck || player.hasPermission(Permissions.MOVE_MOREPACKETS)) {
            return null;
        }

        Location newToLocation = null;

        data.morePacketsCounter++;
        if(data.morePacketsSetbackPoint == null) {
            data.morePacketsSetbackPoint = player.getLocation();
        }

        long currentTime = System.currentTimeMillis();
        // Is at least half a second gone by?
        if(currentTime - timeframe > data.morePacketsLastTime) {

            // Are we over the 10 event limit for that time frame now?
            final double change = data.morePacketsCounter - packetsPerTimeframe * ((double) (currentTime - data.morePacketsLastTime)) / ((double) timeframe);

            if(change > 0) {
                data.morePacketsOverLimit += change;
            } else if(data.morePacketsOverLimit + change > lowLimit) {
                data.morePacketsOverLimit += change;
            } else if(data.morePacketsOverLimit > lowLimit) {
                data.morePacketsOverLimit = lowLimit;
            }

            if(data.morePacketsOverLimit > 0 && data.morePacketsCounter > packetsPerTimeframe) {

                HashMap<String, String> params = new HashMap<String, String>();

                params.put(LogAction.PACKETS, String.valueOf(data.morePacketsCounter - packetsPerTimeframe));
                params.put(LogAction.CHECK, "morepackets");

                boolean cancel = false;
                cancel = action.executeActions(player, cc.moving.morePacketsActions, (int) data.morePacketsOverLimit, params, cc);

                if(cancel) {
                    newToLocation = data.morePacketsSetbackPoint != null ? data.morePacketsSetbackPoint : player.getLocation();
                }
            }

            if(newToLocation == null) {
                data.morePacketsSetbackPoint = player.getLocation();
            }

            if(data.morePacketsOverLimit > 0)
                data.morePacketsOverLimit *= 0.8; // Shrink the "over limit"
                                                  // value by 20 % every second
            data.morePacketsLastTime = currentTime;
            data.morePacketsCounter = 0;

        }

        return newToLocation;
    }
}
