package fr.neatmonster.nocheatplus.logging;

import java.util.Date;
import java.util.logging.Level;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Static access methods for more or less direct logging using either LogManager/INIT or System.out.
 * @author mc_dev
 *
 */
public class StaticLog {
    
    // TODO: Remove this class, instead use an implementation of LogManager for testing.

    private static boolean useLogManager = false; // Let the plugin control this.

    /**
     * Now needs to be set, in order to log to the INIT stream instead of the console.
     * @param useLogManager
     */
    public static void setUseLogManager(boolean useLogManager) {
        StaticLog.useLogManager = useLogManager;
    }

    public static void logInfo(final String msg) {
        log(Level.INFO, msg);
    }

    public static void logWarning(final String msg) {
        log(Level.WARNING, msg);
    }

    public static void logSevere(final String msg) {
        log(Level.SEVERE, msg);
    }

    public static void logInfo(final Throwable t) {
        log(Level.INFO, StringUtil.throwableToString(t));
    }

    public static void logWarning(final Throwable t) {
        log(Level.WARNING, StringUtil.throwableToString(t));
    }

    public static void logSevere(final Throwable t) {
        log(Level.SEVERE, StringUtil.throwableToString(t));
    }

    public static void log(final Level level, final String msg) {
        if (useLogManager) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().log(Streams.INIT, level, msg);
        } else {
            System.out.println("[" + level + "] " + new Date());
            System.out.println(msg);
        }
    }

}
