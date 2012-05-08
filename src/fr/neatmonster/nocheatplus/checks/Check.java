package fr.neatmonster.nocheatplus.checks;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.actions.types.ConsolecommandAction;
import fr.neatmonster.nocheatplus.actions.types.DummyAction;
import fr.neatmonster.nocheatplus.actions.types.LogAction;
import fr.neatmonster.nocheatplus.actions.types.SpecialAction;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

public abstract class Check {
    private static final Map<String, Check> checks     = new HashMap<String, Check>();
    private static Logger                   fileLogger = null;

    public static CheckConfig newConfig(final String group, final String worldName) {
        if (checks.containsKey(group))
            return checks.get(group).newConfig(worldName);
        return null;
    }

    public static CheckData newData(final String group) {
        if (checks.containsKey(group))
            return checks.get(group).newData();
        return null;
    }

    /**
     * Remove instances of &X
     * 
     * @param text
     * @return
     */
    public static String removeColors(String text) {

        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), "");

        return text;
    }

    /**
     * Replace instances of &X with a color
     * 
     * @param text
     * @return
     */
    public static String replaceColors(String text) {

        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), c.toString());

        return text;
    }

    public static void setFileLogger(final Logger logger) {
        fileLogger = logger;
    }

    private final String                       name;

    private final Class<? extends CheckConfig> configClass;

    private final Class<? extends CheckData>   dataClass;

    public Check(final String name, final Class<? extends CheckConfig> configClass,
            final Class<? extends CheckData> dataClass) {
        this.name = name;
        this.configClass = configClass;
        this.dataClass = dataClass;

        checks.put(getGroup(), this);
    }

    /**
     * Execute some actions for the specified player
     * 
     * @param player
     * @param actions
     * @return
     */
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {

        boolean special = false;

        // Get the to be executed actions
        final Action[] actions = actionList.getActions(violationLevel);

        final long time = System.currentTimeMillis() / 1000L;

        for (final Action ac : actions)
            if (player.getExecutionHistory().executeAction(getGroup(), ac, time))
                // The executionHistory said it really is time to execute the
                // action, find out what it is and do what is needed
                if (ac instanceof LogAction && !player.hasPermission(actionList.permissionSilent))
                    executeLogAction((LogAction) ac, this, player);
                else if (ac instanceof SpecialAction)
                    special = true;
                else if (ac instanceof ConsolecommandAction)
                    executeConsoleCommand((ConsolecommandAction) ac, this, player);
                else if (ac instanceof DummyAction) {
                    // nothing - it's a "DummyAction" after all
                }

        return special;
    }

    private void executeConsoleCommand(final ConsolecommandAction action, final Check check, final NCPPlayer player) {
        final String command = action.getCommand(player, check);

        try {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (final CommandException e) {
            System.out.println("[NoCheatPlus] failed to execute the command '" + command + "': " + e.getMessage()
                    + ", please check if everything is setup correct.");
        } catch (final Exception e) {
            // I don't care in this case, your problem if your command fails.
        }
    }

    private void executeLogAction(final LogAction l, final Check check, final NCPPlayer player) {

        final ConfigFile configurationFile = ConfigManager.getConfigFile();
        if (!configurationFile.getBoolean(ConfPaths.LOGGING_ACTIVE))
            return;

        final String prefix = configurationFile.getString(ConfPaths.LOGGING_PREFIX);
        final String message = l.getLogMessage(player, check);
        if (configurationFile.getBoolean(ConfPaths.LOGGING_LOGTOCONSOLE) && l.toConsole())
            // Console logs are not colored
            System.out.println(removeColors(prefix + message));
        if (configurationFile.getBoolean(ConfPaths.LOGGING_LOGTOINGAMECHAT) && l.toChat())
            for (final Player bukkitPlayer : Bukkit.getServer().getOnlinePlayers())
                if (NCPPlayer.hasPermission(bukkitPlayer, Permissions.ADMIN_CHATLOG))
                    // Chat logs are potentially colored
                    bukkitPlayer.sendMessage(replaceColors(prefix + message));
        if (configurationFile.getBoolean(ConfPaths.LOGGING_LOGTOFILE) && l.toFile())
            // File logs are not colored
            fileLogger.info(removeColors(message));
    }

    public String getGroup() {
        return name.contains(".") ? name.split("\\.")[0] : name;
    }

    public String getName() {
        return name.contains(".") ? name.split("\\.")[name.split("\\.").length - 1] : name;
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
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.PLAYER)
            return player.getName();
        else if (wildcard == ParameterName.CHECK)
            return name;
        else if (wildcard == ParameterName.LOCATION) {
            final Location l = player.getLocation();
            return String.format(Locale.US, "%.2f,%.2f,%.2f", l.getX(), l.getY(), l.getZ());
        } else if (wildcard == ParameterName.WORLD)
            return player.getWorld().getName();
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
    protected void incrementStatistics(final NCPPlayer player, final Id id, final double vl) {
        player.getStatistics().increment(id, vl);
    }

    public CheckConfig newConfig(final String worldName) {
        try {
            return configClass.getConstructor(ConfigFile.class).newInstance(ConfigManager.getConfFile(worldName));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public CheckData newData() {
        try {
            return dataClass.getConstructor().newInstance();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
