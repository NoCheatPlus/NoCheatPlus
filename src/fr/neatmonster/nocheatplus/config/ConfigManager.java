package fr.neatmonster.nocheatplus.config;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.Check;

/*
 * MM'""""'YMM                   .8888b oo          M"""""`'"""`YM                                                       
 * M' .mmm. `M                   88   "             M  mm.  mm.  M                                                       
 * M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. M  MMM  MMM  M .d8888b. 88d888b. .d8888b. .d8888b. .d8888b. 88d888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 M  MMM  MMM  M 88'  `88 88'  `88 88'  `88 88'  `88 88ooood8 88'  `88 
 * M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 M  MMM  MMM  M 88.  .88 88    88 88.  .88 88.  .88 88.  ... 88       
 * MM.     .dM `88888P' dP    dP dP     dP `8888P88 M  MMM  MMM  M `88888P8 dP    dP `88888P8 `8888P88 `88888P' dP       
 * MMMMMMMMMMM                                  .88 MMMMMMMMMMMMMM                                 .88                   
 *                                          d8888P                                             d8888P                    
 */
/**
 * Central location for everything that's described in the configuration file(s).
 */
public class ConfigManager {

    /**
     * The formatter that is used to format the log file.
     */
    private static class LogFileFormatter extends Formatter {

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
                final StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }

    /** The map containing the configuration files per world. */
    private static final Map<String, ConfigFile> worldsMap   = new HashMap<String, ConfigFile>();

    /** The file handler. */
    private static FileHandler                   fileHandler = null;

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

    /**
     * Gets the configuration file.
     * 
     * @return the configuration file
     */
    public static ConfigFile getConfigFile() {
        return worldsMap.get(null);
    }

    /**
     * Gets the configuration file.
     * 
     * @param worldName
     *            the world name
     * @return the configuration file
     */
    public static ConfigFile getConfigFile(final String worldName) {
        if (worldsMap.containsKey(worldName))
            return worldsMap.get(worldName);
        return getConfigFile();
    }

    /**
     * Initializes the configuration manager.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public static void init(final NoCheatPlus plugin) {
        // First try to obtain and parse the global configuration file.
        final File folder = plugin.getDataFolder();
        final File globalFile = new File(folder, "config.yml");

        final ConfigFile global = new ConfigFile();
        global.setDefaults(new DefaultConfig());
        global.options().copyDefaults(true);
        global.options().copyHeader(true);

        if (globalFile.exists())
            try {
                global.load(globalFile);
            } catch (final Exception e) {
                e.printStackTrace();
            }

        try {
            global.save(globalFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        global.regenerateActionLists();

        // Put the global configuration file on the configurations map.
        worldsMap.put(null, global);

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

        final File logFile = new File(folder, "nocheatplus.log");
        try {
            try {
                logFile.getParentFile().mkdirs();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            fileHandler = new FileHandler(logFile.getCanonicalPath(), true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(LogFileFormatter.newInstance());

            logger.addHandler(fileHandler);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        Check.setFileLogger(logger);

        // Try to find world-specific configuration files.
        final HashMap<String, File> worldFiles = new HashMap<String, File>();

        if (folder.isDirectory())
            for (final File file : folder.listFiles())
                if (file.isFile()) {
                    final String filename = file.getName();
                    if (filename.matches(".+_config.yml$")) {
                        final String worldname = filename.substring(0, filename.length() - 10);
                        worldFiles.put(worldname, file);
                    }
                }

        for (final Entry<String, File> worldEntry : worldFiles.entrySet()) {
            final File worldConfigFile = worldEntry.getValue();
            final ConfigFile world = new ConfigFile();
            world.setDefaults(global);

            try {
                world.load(worldConfigFile);
                worldsMap.put(worldEntry.getKey(), world);

                // Write the configuration file back to disk immediately.
                world.save(worldConfigFile);
            } catch (final Exception e) {
                System.out.println("[NoCheatPlus] Couldn't load world-specific configuration for "
                        + worldEntry.getKey() + "!");
                e.printStackTrace();
            }

            world.regenerateActionLists();
        }
    }
}
