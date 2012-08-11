package fr.neatmonster.nocheatplus.checks;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.actions.types.CancelAction;
import fr.neatmonster.nocheatplus.actions.types.CommandAction;
import fr.neatmonster.nocheatplus.actions.types.DummyAction;
import fr.neatmonster.nocheatplus.actions.types.LogAction;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import fr.neatmonster.nocheatplus.players.ExecutionHistory;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * MM'""""'YMM dP                         dP       
 * M' .mmm. `M 88                         88       
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. 88  .dP  
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `"" 88888"   
 * M. `MMM' .M 88    88 88.  ... 88.  ... 88  `8b. 
 * MM.     .dM dP    dP `88888P' `88888P' dP   `YP 
 * MMMMMMMMMMM                                     
 */
/**
 * The Class Check.
 */
public abstract class Check {
    protected static Map<String, ExecutionHistory> histories  = new HashMap<String, ExecutionHistory>();

    private static Logger                          fileLogger = null;

    /**
     * Gets the player's history.
     * 
     * @param player
     *            the player
     * @return the history
     */
    protected static ExecutionHistory getHistory(final Player player) {
        if (!histories.containsKey(player.getName()))
            histories.put(player.getName(), new ExecutionHistory());
        return histories.get(player.getName());
    }

    /**
     * Removes the colors of a message.
     * 
     * @param text
     *            the text
     * @return the string
     */
    public static String removeColors(String text) {
        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), "");
        return text;
    }

    /**
     * Replace colors of a message.
     * 
     * @param text
     *            the text
     * @return the string
     */
    public static String replaceColors(String text) {
        for (final ChatColor c : ChatColor.values())
            text = text.replace("&" + c.getChar(), c.toString());
        return text;
    }

    /**
     * Sets the file logger.
     * 
     * @param logger
     *            the new file logger
     */
    public static void setFileLogger(final Logger logger) {
        fileLogger = logger;
    }

    /** The type. */
    protected final CheckType type;

    /**
     * Instantiates a new check.
     * 
     * @param type
     *            the type
     */
    public Check(final CheckType type) {
        this.type = type;
    }

    /**
     * Convenience method.
     * 
     * @param player
     *            the player
     * @param VL
     *            the vL
     * @param actions
     *            the actions
     * @return true, if the event should be cancelled
     */
    protected boolean executeActions(final Player player, final double VL, final ActionList actions) {
        return executeActions(new ViolationData(this, player, VL, actions));
    }

    /**
     * Execute some actions for the specified player.
     * 
     * @param violationData
     *            the violation data
     * @return true, if the event should be cancelled
     */
    protected boolean executeActions(final ViolationData violationData) {
        try {
            boolean special = false;
            final Player player = violationData.player;

            // Check a bypass permission:
            if (violationData.bypassPermission != null)
                if (player.hasPermission(violationData.bypassPermission))
                    return false;

            final ActionList actionList = violationData.actions;
            final double violationLevel = violationData.VL;

            // Dispatch the VL processing to the hook manager.
            if (NCPHookManager.shouldCancelVLProcessing(violationData.check.type, player))
                // One of the hooks has decided to cancel the VL processing, return false.
                return false;

            // Get the to be executed actions.
            final Action[] actions = actionList.getActions(violationLevel);

            final long time = System.currentTimeMillis() / 1000L;

            for (final Action ac : actions)
                if (getHistory(player).executeAction(violationData.check.type.getName(), ac, time))
                    // The execution history said it really is time to execute the action, find out what it is and do
                    // what is needed.

                    // TODO: Check design: maybe ac.execute(this) without the instance checks ?

                    if (ac instanceof LogAction && !player.hasPermission(actionList.permissionSilent))
                        executeLogAction((LogAction) ac, violationData.check, violationData);
                    else if (ac instanceof CancelAction)
                        special = true;
                    else if (ac instanceof CommandAction)
                        executeConsoleCommand((CommandAction) ac, violationData.check, violationData);
                    else if (ac instanceof DummyAction) {
                        // Do nothing, it's a dummy action after all.
                    }

            return special;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Execute actions in a thread safe manner.
     * 
     * @param player
     *            the player
     * @param VL
     *            the vL
     * @param actions
     *            the actions
     * @param bypassPermission
     *            the bypass permission
     * @return true, if the event should be cancelled
     */
    public boolean executeActionsThreadSafe(final Player player, final double VL, final ActionList actions,
            final String bypassPermission) {
        // Sync it into the main thread by using an event.
        return executeActionsThreadSafe(new ViolationData(this, player, VL, actions, bypassPermission));
    }

    /**
     * Execute actions in a thread safe manner.
     * 
     * @param violationData
     *            the violation data
     * @return true, if if the event should be cancelled
     */
    public boolean executeActionsThreadSafe(final ViolationData violationData) {
        final ExecuteActionsEvent event = new ExecuteActionsEvent(violationData);
        Bukkit.getPluginManager().callEvent(event);
        return event.getCancel();
    }

    /**
     * Execute a console command.
     * 
     * @param action
     *            the action
     * @param check
     *            the check
     * @param violationData
     *            the violation data
     */
    private void executeConsoleCommand(final CommandAction action, final Check check, final ViolationData violationData) {
        final String command = action.getCommand(check, violationData);

        try {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (final CommandException e) {
            System.out.println("[NoCheatPlus] Failed to execute the command '" + command + "': " + e.getMessage()
                    + ", please check if everything is setup correct.");
        } catch (final Exception e) {
            // I don't care in this case, your problem if your command fails.
        }
    }

    /**
     * Execute a log action.
     * 
     * @param logAction
     *            the log action
     * @param check
     *            the check
     * @param violationData
     *            the violation data
     */
    private void executeLogAction(final LogAction logAction, final Check check, final ViolationData violationData) {
        final ConfigFile configurationFile = ConfigManager.getConfigFile();
        if (!configurationFile.getBoolean(ConfPaths.LOGGING_ACTIVE))
            return;

        final String message = logAction.getLogMessage(check, violationData);
        if (configurationFile.getBoolean(ConfPaths.LOGGING_LOGTOCONSOLE) && logAction.toConsole())
            // Console logs are not colored.
            System.out.println("[NoCheatPlus] " + removeColors(message));
        if (configurationFile.getBoolean(ConfPaths.LOGGING_LOGTOINGAMECHAT) && logAction.toChat())
            for (final Player otherPlayer : Bukkit.getServer().getOnlinePlayers())
                if (otherPlayer.hasPermission(Permissions.ADMINISTRATION_NOTIFY))
                    // Chat logs are potentially colored.
                    otherPlayer.sendMessage(replaceColors(ChatColor.RED + "NCP: " + ChatColor.WHITE + message));
        if (configurationFile.getBoolean(ConfPaths.LOGGING_LOGTOFILE) && logAction.toFile())
            // File logs are not colored.
            fileLogger.info(removeColors(message));
    }

    /**
     * Replace a parameter for commands or log actions with an actual value. Individual checks should override this to
     * get their own parameters handled too.
     * 
     * @param wildcard
     *            the wildcard
     * @param violationData
     *            the violation data
     * @return the parameter
     */
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        if (wildcard == ParameterName.CHECK)
            return getClass().getSimpleName();
        else if (wildcard == ParameterName.PLAYER)
            return violationData.player.getName();
        else if (wildcard == ParameterName.VIOLATIONS) {
            try {
                return "" + Math.round(violationData.VL);
            } catch (final Exception e) {
                Bukkit.broadcastMessage("getParameter " + type.getName());
                e.printStackTrace();
            }
            return "";
        } else
            return "The author was lazy and forgot to define " + wildcard + ".";
    }

    /**
     * Checks if this check is enabled for the specified player.
     * 
     * @param player
     *            the player
     * @return true, if the check is enabled
     */
    public boolean isEnabled(final Player player) {
        try {
            return type.isEnabled(player) && !player.hasPermission(type.getPermission());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
