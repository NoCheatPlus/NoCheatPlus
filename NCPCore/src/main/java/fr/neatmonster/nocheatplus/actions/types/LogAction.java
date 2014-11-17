package fr.neatmonster.nocheatplus.actions.types;

import org.bukkit.ChatColor;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.StaticLogFile;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

/**
 * Print a log message to various locations.
 */
public class LogAction extends ActionWithParameters<ViolationData, ActionList> {

    private static final String PREFIX_CHAT = ChatColor.RED + "NCP: "+ ChatColor.WHITE ;
    private static final String PREFIX_CONSOLE= "[NoCheatPlus] ";
    private static final String PREFIX_FILE = "";

    // TODO: pull down to providers for (console), !chat!, (file) - then move to NCPCompat.

    // Some flags to decide where the log message should show up, based on the configuration file.
    /** Log to chat? */
    public final boolean toChat;

    /** Log to console? */
    public final boolean toConsole;

    /** Log to file? */
    public final boolean toFile;

    /** Message prefixes. */
    public final String prefixChat, prefixConsole, prefixFile;

    /**
     * Instantiates a new log action.
     * 
     * @param name
     *            the name
     * @param delay
     *            the delay
     * @param repeat
     *            the repeat
     * @param toChat
     *            the to chat
     * @param toConsole
     *            the to console
     * @param toFile
     *            the to file
     * @param message
     *            the message
     */
    public LogAction(final String name, final int delay, final int repeat, final boolean toChat,
            final boolean toConsole, final boolean toFile, final String message) {
        super(name, delay, repeat, message);
        // Might switch to only store the prefixes (null = deactivated).
        this.toChat = toChat;
        this.toConsole = toConsole;
        this.toFile = toFile;
        prefixChat = PREFIX_CHAT;
        prefixConsole = PREFIX_CONSOLE;
        prefixFile = PREFIX_FILE;
    }

    /**
     * Constructor for optimized actions.
     * @param name
     * @param delay
     * @param repeat
     * @param prefixChat Prefixes set to null means deactivated.
     * @param prefixConsole
     * @param prefixFile
     * @param message
     */
    protected LogAction(final String name, final int delay, final int repeat, final String prefixChat,
            final String prefixConsole, final String prefixFile, final String message) {
        super(name, delay, repeat, message);
        this.prefixChat = prefixChat;
        this.prefixConsole = prefixConsole;
        this.prefixFile = prefixFile;
        toChat = prefixChat != null;
        toConsole = prefixConsole != null;
        toFile = prefixFile != null;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.neatmonster.nocheatplus.actions.Action#execute(fr.neatmonster.nocheatplus
     * .checks.ViolationData)
     */
    @Override
    public boolean execute(final ViolationData violationData) {
        if (!violationData.player.hasPermission(violationData.getPermissionSilent())) {
            final String message = super.getMessage(violationData);
            if (toChat) {
                NCPAPIProvider.getNoCheatPlusAPI().sendAdminNotifyMessage(ColorUtil.replaceColors(prefixChat + message));
            }
            if (toConsole) {
                StaticLog.logInfo(ColorUtil.removeColors(prefixConsole + message));
            }
            if (toFile) {
                StaticLogFile.fileLogger.info(ColorUtil.removeColors(prefixFile + message));
            }
        }
        return false;
    }

    /**
     * Create the string that's used to define the action in the logfile.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        return "log:" + name + ":" + delay + ":" + repeat + ":" + (toConsole ? "c" : "") + (toChat ? "i" : "")
                + (toFile ? "f" : "");
    }

    @Override
    public Action<ViolationData, ActionList> getOptimizedCopy(final ConfigFileWithActions<ViolationData, ActionList> config, final Integer threshold) {
        if (!config.getBoolean(ConfPaths.LOGGING_ACTIVE)) {
            return null;
        }
        final String prefixChat = filterPrefix(config, ConfPaths.LOGGING_BACKEND_INGAMECHAT_PREFIX, PREFIX_CHAT,  this.toChat && config.getBoolean(ConfPaths.LOGGING_BACKEND_INGAMECHAT_ACTIVE));
        final String prefixConsole = filterPrefix(config, ConfPaths.LOGGING_BACKEND_CONSOLE_PREFIX, PREFIX_CONSOLE,  this.toConsole && config.getBoolean(ConfPaths.LOGGING_BACKEND_CONSOLE_ACTIVE));
        final String prefixFile = filterPrefix(config, ConfPaths.LOGGING_BACKEND_FILE_PREFIX, PREFIX_FILE,  this.toFile && config.getBoolean(ConfPaths.LOGGING_BACKEND_FILE_ACTIVE));
        if (allNull(prefixChat, prefixConsole, prefixFile)) {
            return null;
        }
        return new LogAction(name, delay, repeat, prefixChat, prefixConsole, prefixFile, message);
    }

    private static boolean allNull(Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] != null) {
                return false;
            }
        }
        return true;
    }

    private static final String filterPrefix(final ConfigFileWithActions<ViolationData, ActionList> config, final String path, final String defaultValue, final boolean use) {
        if (!use) {
            return null;
        }
        final String prefix = config.getString(path);
        return prefix == null ? defaultValue : prefix;
    }

}
