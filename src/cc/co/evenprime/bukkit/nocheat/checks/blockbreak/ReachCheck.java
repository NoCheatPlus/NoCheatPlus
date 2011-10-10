package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * The reach check will find out if a player interacts with something that's too
 * far away
 * 
 * @author Evenprime
 * 
 */
public class ReachCheck {

    private final NoCheat        plugin;
    private final ActionExecutor action;

    public ReachCheck(NoCheat plugin) {
        this.plugin = plugin;
        this.action = new ActionExecutor(plugin);
    }

    public boolean check(Player player, double distance, BlockBreakData data, ConfigurationCache cc) {

        boolean cancel = false;

        double limit = player.getGameMode() == GameMode.CREATIVE ? cc.blockbreak.reachDistance + 2 : cc.blockbreak.reachDistance;
        if(distance > limit) {
            // Player failed the check

            // Increment violation counter
            data.reachViolationLevel += 1;

            LogData ldata = plugin.getDataManager().getLogData(player);

            ldata.check = "blockbreak.reach";
            ldata.reachdistance = distance;
            cancel = action.executeActions(player, cc.blockbreak.reachActions, (int) data.reachViolationLevel, ldata, cc);
        } else {
            data.reachViolationLevel *= 0.9D;
        }

        return cancel;
    }

}
