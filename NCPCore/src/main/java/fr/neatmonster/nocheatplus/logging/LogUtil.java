package fr.neatmonster.nocheatplus.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Static access methods for logging access (mostly uses the Bukkit logger).
 * @author mc_dev
 *
 */
public class LogUtil {

    private static boolean useBukkitLogger = true;

    /**
     * This is for testing purposes only.
     * @param use
     */
    public static void setUseBukkitLogger(boolean use) {
        useBukkitLogger = use;
    }

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
            Bukkit.getLogger().log(level, msg);
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
     * Schedule a log message with given level for the Bukkit logger with the NoCheatPlus plugin.<br>
     * 
     * @param level
     * @param message
     * @return
     */
    public static boolean scheduleLog(final Level level, final String message) {
        try {
            return Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("NoCheatPlus"), new Runnable() {
                @Override
                public final void run() {
                    Bukkit.getLogger().log(level, message);
                }
            }) != -1;
        } catch (final Exception exc) {
            return false;
        }
    }

    /**
     * Schedule joined parts for info level.
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
     * Schedule joined.
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
