package fr.neatmonster.nocheatplus.logging;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Could not think of anything better, likely a refactoring stage.
 * @author mc_dev
 *
 */
public class StaticLogFile {

    /**
     * The formatter that is used to format the log file.
     */
    protected static class LogFileFormatter extends Formatter {

        /**
         * Create a new instance of the log file formatter.
         * 
         * @return the log file formatter
         */
        public static LogFileFormatter newInstance() {
            return new LogFileFormatter();
        }

        /** The date formatter. */
        private final SimpleDateFormat date;

        /**
         * Instantiates a new log file formatter.
         */
        private LogFileFormatter() {
            date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        /* (non-Javadoc)
         * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
         */
        @Override
        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable ex = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if (ex != null) {
                builder.append(StringUtil.throwableToString(ex));
            }

            return builder.toString();
        }
    }
    /** The file logger. */
    public static Logger			fileLogger = null;
    /** The file handler. */
    private static FileHandler      fileHandler = null;

    /**
     * Cleanup.
     */
    public static void cleanup() {
        fileHandler.flush();
        fileHandler.close();
        final Logger logger = Logger.getLogger("NoCheatPlus");
        logger.removeHandler(fileHandler);
        fileHandler = null;
    }
    public static void setupLogger(File logFile){
        // Setup the file logger.
        final Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        for (final Handler h : logger.getHandlers())
            logger.removeHandler(h);
        if (fileHandler != null) {
            fileHandler.close();
            logger.removeHandler(fileHandler);
            fileHandler = null;
        }
        try {
            try {
                logFile.getParentFile().mkdirs();
            } catch (final Exception e) {
                LogUtil.logSevere(e);
            }
            fileHandler = new FileHandler(logFile.getCanonicalPath(), true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(StaticLogFile.LogFileFormatter.newInstance());
            logger.addHandler(fileHandler);
        } catch (final Exception e) {
            LogUtil.logSevere(e);
        }
        fileLogger = logger;
    }

}
