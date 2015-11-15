package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

/**
 * Print a log message to various locations.
 */
public class LogAction extends ActionWithParameters<ViolationData, ActionList> {

    // Some flags to decide where the log message should show up, based on the configuration file.
    /** Log to chat? */
    public final boolean toChat;

    /** Log to console? */
    public final boolean toConsole;

    /** Log to file? */
    public final boolean toFile;

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
    }

    @Override
    public Action<ViolationData, ActionList> getOptimizedCopy(final ConfigFileWithActions<ViolationData, ActionList> config, final Integer threshold) {
        if (!config.getBoolean(ConfPaths.LOGGING_ACTIVE)) {
            return null;
        }
        return this;
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
            final LogManager logManager = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
            if (toChat) {
                logManager.info(Streams.NOTIFY_INGAME, ColorUtil.replaceColors(message));
            }
            if (toConsole) {
                logManager.info(Streams.SERVER_LOGGER, ColorUtil.removeColors(message));
            }
            if (toFile) {
                logManager.info(Streams.DEFAULT_FILE, ColorUtil.removeColors(message));
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

}
