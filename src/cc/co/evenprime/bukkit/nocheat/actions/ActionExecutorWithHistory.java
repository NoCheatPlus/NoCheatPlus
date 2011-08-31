package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.history.ActionHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.ConsolecommandAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.SpecialAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;
import cc.co.evenprime.bukkit.nocheat.log.LogManager;

/**
 * Implementation of ActionExecutor, that will trace the history of action
 * executions to decide if an action 'really' gets executed.
 * 
 * @author Evenprime
 * 
 */
public class ActionExecutorWithHistory implements ActionExecutor {

    private final Map<Player, ActionHistory> actionHistory = new HashMap<Player, ActionHistory>();
    private final ActionManager              actionManager;
    private final LogManager                 log;

    private final ConsoleCommandSender       ccsender;

    public ActionExecutorWithHistory(NoCheat plugin) {
        this.actionManager = plugin.getActionManager();
        this.log = plugin.getLogManager();
        this.ccsender = new ConsoleCommandSender(plugin.getServer());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor#executeActions(
     * org.bukkit.entity.Player,
     * cc.co.evenprime.bukkit.nocheat.actions.ActionList, double,
     * java.util.HashMap,
     * cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache)
     */
    @Override
    public boolean executeActions(Player player, ActionList actions, int violationLevel, HashMap<String, String> hashMap, ConfigurationCache cc) {

        boolean special = false;

        for(String a : actions.getActions(violationLevel)) {
            Action ac = actionManager.getActionByName(a);

            Map<String, String> map = null;
            if(ac != null) {
                if(getHistory(player).executeAction(ac, System.currentTimeMillis())) {
                    if(ac instanceof LogAction) {
                        if(map == null)
                            map = generateHashMap(player, violationLevel, hashMap);
                        LogAction l = (LogAction) ac;
                        log.log(l.level, l.getLogMessage(map), cc);
                    } else if(ac instanceof SpecialAction) {
                        special = true;
                    } else if(ac instanceof ConsolecommandAction) {
                        if(map == null)
                            map = generateHashMap(player, violationLevel, hashMap);
                        executeConsoleCommand(((ConsolecommandAction) ac).getCommand(map));
                    }
                }
            } else {
                log.logToConsole(LogLevel.HIGH, "NoCheat: Couldn't find action " + a + ". You need to define it properly to use it in your config file!");
            }
        }

        return special;
    }

    private HashMap<String, String> generateHashMap(Player player, double violationLevel, HashMap<String, String> map) {
        HashMap<String, String> newMap = new HashMap<String, String>();

        newMap.put(LogAction.PLAYER, player.getName());
        Location l = player.getLocation();
        newMap.put(LogAction.LOCATION, String.format(Locale.US, "%.2f,%.2f,%.2f", l.getX(), l.getY(), l.getZ()));
        newMap.put(LogAction.WORLD, player.getWorld().getName());
        newMap.put(LogAction.VIOLATIONS, String.format(Locale.US, "%.2f", violationLevel));

        newMap.putAll(map);

        return newMap;
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
