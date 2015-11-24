package fr.neatmonster.nocheatplus.actions.types;

import java.util.logging.Level;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.logging.Streams;

/**
 * Default log action for standard targets.
 */
public class LogAction extends GenericLogAction {

    protected static final GenericLogActionConfig configIngame = new GenericLogActionConfig(
            ConfPaths.LOGGING_BACKEND_INGAMECHAT_ACTIVE, Streams.NOTIFY_INGAME, true, Level.INFO, "i");
    protected static final GenericLogActionConfig configConsole = new GenericLogActionConfig(
            ConfPaths.LOGGING_BACKEND_CONSOLE_ACTIVE, Streams.SERVER_LOGGER, false, Level.INFO, "c");
    protected static final GenericLogActionConfig configFile = new GenericLogActionConfig(
            ConfPaths.LOGGING_BACKEND_FILE_ACTIVE, Streams.DEFAULT_FILE, false, Level.INFO, "f");

    /**
     * Instantiates a new log action (not optimized).
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
        super(name, delay, repeat, message, true, toChat ? configIngame : null, toConsole ? configConsole : null, toFile ? configFile : null);
    }

}
