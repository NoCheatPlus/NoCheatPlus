package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import java.util.HashMap;
import java.util.Locale;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;

/**
 * The reach check will find out if a player interacts with something that's too
 * far away
 * 
 * @author Evenprime
 * 
 */
public class ReachCheck {

    private final ActionExecutor action;

    public ReachCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
    }

    public boolean check(Player player, double distance, BlockBreakData data, ConfigurationCache cc) {

        boolean cancel = false;

        if(distance > cc.blockbreak.reachDistance) {
            // Player failed the check

            // Increment violation counter
            data.reachViolationLevel += 1;

            // Prepare some event-specific values for logging and custom actions
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.CHECK, "blockbreak.reach");
            params.put(LogAction.DISTANCE, String.format(Locale.US, "%.2f", distance));
            cancel = action.executeActions(player, cc.blockbreak.reachActions, (int) data.reachViolationLevel, params, cc);
        } else {
            data.reachViolationLevel *= 0.9D;
        }

        return cancel;
    }

}
