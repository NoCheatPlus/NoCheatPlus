package me.neatmonster.nocheatplus.checks;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusLogEvent;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.Action;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.actions.types.ActionList;
import me.neatmonster.nocheatplus.actions.types.ConsolecommandAction;
import me.neatmonster.nocheatplus.actions.types.DummyAction;
import me.neatmonster.nocheatplus.actions.types.LogAction;
import me.neatmonster.nocheatplus.actions.types.SpecialAction;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.Statistics.Id;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandException;

/**
 * The abstract Check class, providing some basic functionality
 * 
 */
public abstract class Check {

    private final String        name;
    // used to bundle information of multiple checks
    private final String        groupId;
    protected final NoCheatPlus plugin;

    public Check(final NoCheatPlus plugin, final String groupId, final String name) {
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
    protected boolean executeActions(final NoCheatPlusPlayer player, final ActionList actionList,
            final double violationLevel) {

        boolean special = false;

        // Get the to be executed actions
        final Action[] actions = actionList.getActions(violationLevel);

        final long time = System.currentTimeMillis() / 1000L;

        // The configuration will be needed too
        final ConfigurationCacheStore cc = player.getConfigurationStore();

        for (final Action ac : actions)
            if (player.getExecutionHistory().executeAction(groupId, ac, time))
                // The executionHistory said it really is time to execute the
                // action, find out what it is and do what is needed
                if (ac instanceof LogAction && !player.hasPermission(actionList.permissionSilent))
                    executeLogAction((LogAction) ac, this, player, cc);
                else if (ac instanceof SpecialAction)
                    special = true;
                else if (ac instanceof ConsolecommandAction)
                    executeConsoleCommand((ConsolecommandAction) ac, this, player, cc);
                else if (ac instanceof DummyAction) {
                    // nothing - it's a "DummyAction" after all
                }

        return special;
    }

    private void executeConsoleCommand(final ConsolecommandAction action, final Check check,
            final NoCheatPlusPlayer player, final ConfigurationCacheStore cc) {
        final String command = action.getCommand(player, check);

        try {
            plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (final CommandException e) {
            System.out.println("[NoCheatPlus] failed to execute the command '" + command + "': " + e.getMessage()
                    + ", please check if everything is setup correct.");
        } catch (final Exception e) {
            // I don't care in this case, your problem if your command fails
        }
    }

    private void executeLogAction(final LogAction l, final Check check, final NoCheatPlusPlayer player,
            final ConfigurationCacheStore cc) {

        if (!cc.logging.active)
            return;

        // Fire one of our custom "Log" Events
        Bukkit.getServer()
                .getPluginManager()
                .callEvent(
                        new NoCheatPlusLogEvent(cc.logging.prefix, l.getLogMessage(player, check), cc.logging.toConsole
                                && l.toConsole(), cc.logging.toChat && l.toChat(), cc.logging.toFile && l.toFile()));
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
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.PLAYER)
            return player.getName();
        else if (wildcard == ParameterName.CHECK)
            return name;
        else if (wildcard == ParameterName.LOCATION) {
            final Location l = player.getPlayer().getLocation();
            return String.format(Locale.US, "%.2f,%.2f,%.2f", l.getX(), l.getY(), l.getZ());
        } else if (wildcard == ParameterName.WORLD)
            return player.getPlayer().getWorld().getName();
        else
            return "the Author was lazy and forgot to define " + wildcard + ".";

    }

    /**
     * Collect information about the players violations
     * 
     * @param player
     * @param id
     * @param vl
     */
    protected void incrementStatistics(final NoCheatPlusPlayer player, final Id id, final double vl) {
        player.getDataStore().getStatistics().increment(id, vl);
    }
}
