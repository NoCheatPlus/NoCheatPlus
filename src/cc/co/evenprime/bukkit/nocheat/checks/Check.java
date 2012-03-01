package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatLogEvent;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.actions.types.ConsolecommandAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.DummyAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.actions.types.SpecialAction;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

/**
 * The abstract Check class, providing some basic functionality
 *
 */
public abstract class Check {

    private final String    name;
    // used to bundle information of multiple checks
    private final String    groupId;
    protected final NoCheat plugin;

    public Check(NoCheat plugin, String groupId, String name) {
        this.plugin = plugin;
        this.groupId = groupId;
        this.name = name;
    }

    /**
     * Execute some actions for the specified player
     * 
     * @param player
     * @param actions
     * @return
     */
    protected final boolean executeActions(NoCheatPlayer player, ActionList actionList, double violationLevel) {

        boolean special = false;

        // Get the to be executed actions
        Action[] actions = actionList.getActions(violationLevel);

        final long time = System.currentTimeMillis() / 1000L;

        // The configuration will be needed too
        final ConfigurationCacheStore cc = player.getConfigurationStore();

        for(Action ac : actions) {
            if(player.getExecutionHistory().executeAction(groupId, ac, time)) {
                // The executionHistory said it really is time to execute the
                // action, find out what it is and do what is needed
                if(ac instanceof LogAction && !player.hasPermission(actionList.permissionSilent)) {
                    executeLogAction((LogAction) ac, this, player, cc);
                } else if(ac instanceof SpecialAction) {
                    special = true;
                } else if(ac instanceof ConsolecommandAction) {
                    executeConsoleCommand((ConsolecommandAction) ac, this, player, cc);
                } else if(ac instanceof DummyAction) {
                    // nothing - it's a "DummyAction" after all
                }
            }
        }

        return special;
    }

    /**
     * Collect information about the players violations
     * 
     * @param player
     * @param id
     * @param vl
     */
    protected void incrementStatistics(NoCheatPlayer player, Id id, double vl) {
        player.getDataStore().getStatistics().increment(id, vl);
    }

    private final void executeLogAction(LogAction l, Check check, NoCheatPlayer player, ConfigurationCacheStore cc) {

        if(!cc.logging.active)
            return;

        // Fire one of our custom "Log" Events
        Bukkit.getServer().getPluginManager().callEvent(new NoCheatLogEvent(cc.logging.prefix, l.getLogMessage(player, check), cc.logging.toConsole && l.toConsole(), cc.logging.toChat && l.toChat(), cc.logging.toFile && l.toFile()));
    }

    private final void executeConsoleCommand(ConsolecommandAction action, Check check, NoCheatPlayer player, ConfigurationCacheStore cc) {
        final String command = action.getCommand(player, check);

        try {
            plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch(CommandException e) {
            System.out.println("[NoCheat] failed to execute the command '" + command + "': " + e.getMessage() + ", please check if everything is setup correct.");
        } catch(Exception e) {
            // I don't care in this case, your problem if your command fails
        }
    }

    /**
     * Replace a parameter for commands or log actions with an actual
     * value. Individual checks should override this to get their own
     * parameters handled too.
     * 
     * @param wildcard
     * @param player
     * @return
     */
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.PLAYER)
            return player.getName();
        else if(wildcard == ParameterName.CHECK)
            return name;
        else if(wildcard == ParameterName.LOCATION) {
            Location l = player.getPlayer().getLocation();
            return String.format(Locale.US, "%.2f,%.2f,%.2f", l.getX(), l.getY(), l.getZ());
        } else if(wildcard == ParameterName.WORLD)
            return player.getPlayer().getWorld().getName();
        else
            return "Evenprime was lazy and forgot to define " + wildcard + ".";

    }
}
