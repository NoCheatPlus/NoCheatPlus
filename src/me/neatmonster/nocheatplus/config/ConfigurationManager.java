package me.neatmonster.nocheatplus.config;

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

import me.neatmonster.nocheatplus.NoCheatPlus;

/**
 * Central location for everything that's described in the configuration file(s)
 * 
 */
public class ConfigurationManager {

    private static class LogFileFormatter extends Formatter {

        private final SimpleDateFormat date;

        public LogFileFormatter() {
            date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

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

    private final static String configFileName = "config.yml";

    private static File getGlobalConfigFile(final File rootFolder) {

        final File globalConfig = new File(rootFolder, configFileName);

        return globalConfig;
    }

    private static Map<String, File> getWorldSpecificConfigFiles(final File rootFolder) {

        final HashMap<String, File> files = new HashMap<String, File>();

        if (rootFolder.isDirectory())
            for (final File f : rootFolder.listFiles())
                if (f.isFile()) {
                    final String filename = f.getName();
                    if (filename.matches(".+_" + configFileName + "$")) {
                        // Get the first part = world name
                        final String worldname = filename.substring(0, filename.length()
                                - (configFileName.length() + 1));
                        files.put(worldname, f);
                    }
                }
        return files;
    }

    private final Map<String, ConfigurationCacheStore> worldnameToConfigCacheMap = new HashMap<String, ConfigurationCacheStore>();

    private FileHandler                                fileHandler;

    private final NoCheatPlus                          plugin;

    public ConfigurationManager(final NoCheatPlus plugin, final File rootConfigFolder) {

        this.plugin = plugin;

        // Setup the real configuration
        initializeConfig(rootConfigFolder);

    }

    /**
     * Reset the loggers and flush and close the fileHandlers
     * to be able to use them next time without problems
     */
    public void cleanup() {
        fileHandler.flush();
        fileHandler.close();
        final Logger l = Logger.getLogger("NoCheatPlus");
        l.removeHandler(fileHandler);
        fileHandler = null;
    }

    /**
     * Get the cache of the specified world, or the default cache,
     * if no cache exists for that world.
     * 
     * @param worldname
     * @return
     */
    public ConfigurationCacheStore getConfigurationCacheForWorld(final String worldname) {

        ConfigurationCacheStore cache = worldnameToConfigCacheMap.get(worldname);

        if (cache != null)
            return cache;
        else {
            // Enter a reference to the cache under the new name
            // to be faster in looking it up later
            cache = worldnameToConfigCacheMap.get(null);
            worldnameToConfigCacheMap.put(worldname, cache);

            return cache;
        }
    }

    /**
     * Read the configuration file and assign either standard values or whatever
     * is declared in the file
     * 
     * @param configurationFile
     */
    private void initializeConfig(final File rootConfigFolder) {

        // First try to obtain and parse the global config file
        final NoCheatPlusConfiguration root = new NoCheatPlusConfiguration();
        root.setDefaults(new DefaultConfiguration());
        root.options().copyDefaults(true);
        root.options().copyHeader(true);

        final File globalConfigFile = getGlobalConfigFile(rootConfigFolder);

        if (globalConfigFile.exists())
            try {
                root.load(globalConfigFile);
            } catch (final Exception e) {
                e.printStackTrace();
            }

        try {
            root.save(globalConfigFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        root.regenerateActionLists();

        // Create a corresponding Configuration Cache
        // put the global config on the config map
        worldnameToConfigCacheMap.put(null, new ConfigurationCacheStore(root));

        plugin.setFileLogger(setupFileLogger(new File(rootConfigFolder, root.getString(ConfPaths.LOGGING_FILENAME))));

        // Try to find world-specific config files
        final Map<String, File> worldFiles = getWorldSpecificConfigFiles(rootConfigFolder);

        for (final Entry<String, File> worldEntry : worldFiles.entrySet()) {

            final File worldConfigFile = worldEntry.getValue();

            final NoCheatPlusConfiguration world = new NoCheatPlusConfiguration();
            world.setDefaults(root);

            try {
                world.load(worldConfigFile);

                worldnameToConfigCacheMap.put(worldEntry.getKey(), new ConfigurationCacheStore(world));

                // write the config file back to disk immediately
                world.save(worldConfigFile);

            } catch (final Exception e) {
                System.out.println("NoCheatPlus: Couldn't load world-specific config for " + worldEntry.getKey());
                e.printStackTrace();
            }

            world.regenerateActionLists();
        }
    }

    private Logger setupFileLogger(final File logfile) {

        final Logger l = Logger.getAnonymousLogger();
        l.setLevel(Level.INFO);
        // Ignore parent's settings
        l.setUseParentHandlers(false);
        for (final Handler h : l.getHandlers())
            l.removeHandler(h);

        if (fileHandler != null) {
            fileHandler.close();
            l.removeHandler(fileHandler);
            fileHandler = null;
        }

        try {
            try {
                logfile.getParentFile().mkdirs();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            fileHandler = new FileHandler(logfile.getCanonicalPath(), true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFileFormatter());

            l.addHandler(fileHandler);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return l;
    }
}
