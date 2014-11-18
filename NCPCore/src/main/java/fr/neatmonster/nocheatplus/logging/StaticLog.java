package fr.neatmonster.nocheatplus.logging;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Static access methods for more or less direct logging using either LogManager/INIT or System.out.
 * @author mc_dev
 *
 */
public class StaticLog {
    
    // TODO: Remove this class (needs a version of LogManager for testing, i.e. ).

    private static boolean useBukkitLogger = false; // Let the plugin control this.

    /**
     * This is for testing purposes only.
     * @param use
     */
    public static void setUseBukkitLogger(boolean use) {
        useBukkitLogger = use;
    }

    // TODO: Remove toString method !
    public static String toString(final Throwable t){
        return StringUtil.throwableToString(t);
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
        log(Level.INFO, toString(t));
    }

    public static void logWarning(final Throwable t) {
        log(Level.WARNING, toString(t));
    }

    public static void logSevere(final Throwable t) {
        log(Level.SEVERE, toString(t));
    }

    public static void log(final Level level, final String msg) {
        if (useBukkitLogger) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().log(Streams.INIT, level, msg);
        } else {
            System.out.println("[" + level + "] " + new Date());
            System.out.println(msg);
        }
    }

    /**
     * Schedule a message to be output by the bukkit logger at info level, scheduled as task for NoCheatPlus.
     * 
     * @param message
     * @return If scheduled successfully.
     */
    public static boolean scheduleLogInfo(final String message) {
        return scheduleLog(Level.INFO, message);
    }

    /**
     * Schedule a message to be output by the bukkit logger at warning level, scheduled as task for NoCheatPlus.
     * 
     * @param message
     * @return If scheduled successfully.
     */
    public static boolean scheduleLogWarning(final String message) {
        return scheduleLog(Level.WARNING, message);
    }

    /**
     * Schedule a message to be output by the bukkit logger at warning level, scheduled as task for NoCheatPlus.
     * 
     * @param message
     * @return If scheduled successfully.
     */
    public static boolean scheduleLogSevere(final String message) {
        return scheduleLog(Level.SEVERE, message);
    }

    public static boolean scheduleLogInfo(final Throwable t) {
        return scheduleLog(Level.INFO, toString(t));
    }

    public static boolean scheduleLogWarning(final Throwable t) {
        return scheduleLog(Level.WARNING, toString(t));
    }

    public static boolean scheduleLogSevere(final Throwable t) {
        return scheduleLog(Level.SEVERE, toString(t));
    }

    /**
     * Same as log(level, message).
     * 
     * @deprecated Same as log(level, message).
     * 
     * @param level
     * @param message
     * @return
     */
    public static boolean scheduleLog(final Level level, final String message) {
        StaticLog.log(level, message);
        return true;
    }

    /**
     * Log joined parts on info level.
     * @param level
     * @param parts
     * @param link
     * @return
     */
    public static <O extends Object> boolean scheduleLogInfo(final List<O> parts, final String link)
    {
        return scheduleLog(Level.INFO, parts, link);
    }

    /**
     * Log joined parts on the given level.
     * @param level
     * @param parts
     * @param link
     * @return
     */
    public static <O extends Object> boolean scheduleLog(final Level level, final List<O> parts, final String link)
    {
        return scheduleLog(level, StringUtil.join(parts, link));
    }

}
