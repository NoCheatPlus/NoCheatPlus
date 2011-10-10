package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.history.ActionHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.ConsolecommandAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.SpecialAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * Will trace the history of action executions to decide if an action 'really'
 * gets executed.
 * 
 * @author Evenprime
 * 
 */
public class ActionExecutor {

    private final Map<Player, ActionHistory> actionHistory = new HashMap<Player, ActionHistory>();
    private final NoCheat plugin;

    private final ConsoleCommandSender       ccsender;

    public ActionExecutor(NoCheat plugin) {
        this.plugin = plugin;
        this.ccsender = new ConsoleCommandSender(plugin.getServer());
    }

    public boolean executeActions(Player player, ActionList actions, int violationLevel, LogData data, ConfigurationCache cc) {

        boolean special = false;

        // Always set this here "by hand"
        data.violationLevel = violationLevel;

        long time = System.currentTimeMillis() / 1000;

        for(Action ac : actions.getActions(violationLevel)) {

            if(getHistory(player).executeAction(ac, time)) {
                if(ac instanceof LogAction) {
                    LogAction l = (LogAction) ac;
                    plugin.getLogManager().log(l.level, l.getMessage(data), cc);
                } else if(ac instanceof SpecialAction) {
                    special = true;
                } else if(ac instanceof ConsolecommandAction) {
                    executeConsoleCommand(((ConsolecommandAction) ac).getCommand(data));
                }
            }
        }

        return special;
    }

    private ActionHistory getHistory(Player player) {

        ActionHistory history = actionHistory.get(player);

        if(history == null) {
            history = new ActionHistory();
            actionHistory.put(player, history);
        }

        return history;
    }

    private void executeConsoleCommand(String command) {
        ccsender.executeConsoleCommand(command);
    }
}
