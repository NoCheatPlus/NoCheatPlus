package cc.co.evenprime.bukkit.nocheat.actions;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.types.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.ConsolecommandAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.SpecialAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;
import cc.co.evenprime.bukkit.nocheat.data.ActionData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * Will trace the history of action executions to decide if an action 'really'
 * gets executed.
 * 
 * @author Evenprime
 * 
 */
public class ActionManager {

    private final NoCheat plugin;

    public ActionManager(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean executeActions(Player player, ActionList actions, int violationLevel, LogData data, ActionData history, ConfigurationCache cc) {

        boolean special = false;

        // Always set this here "by hand"
        data.violationLevel = violationLevel;

        final long time = System.currentTimeMillis() / 1000;

        for(Action ac : actions.getActions(violationLevel)) {

            if(history.executeAction(ac, time)) {
                if(ac instanceof LogAction) {
                    executeLogAction((LogAction) ac, data, cc);
                } else if(ac instanceof SpecialAction) {
                    special = true;
                } else if(ac instanceof ConsolecommandAction) {
                    executeConsoleCommand((ConsolecommandAction) ac, data);
                }
            }
        }

        return special;
    }

    private void executeLogAction(LogAction l, LogData data, ConfigurationCache cc) {
        plugin.getLogManager().log(l.level, l.getMessage(data), cc);
    }

    private void executeConsoleCommand(ConsolecommandAction action, LogData data) {
        String command = action.getCommand(data);
        try {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        } catch(Exception e) {
            // TODO: Better error handling
            System.out.println("[NoCheat] failed to execute the command '" + command + "', please check if everything is setup correct. ");
            e.printStackTrace();
        }
    }
}
