package cc.co.evenprime.bukkit.nocheat.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import cc.co.evenprime.bukkit.nocheat.actions.ActionManager;
import cc.co.evenprime.bukkit.nocheat.actions.types.Action;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

/**
 * Central location for everything that's described in the configuration file(s)
 * 
 * @author Evenprime
 * 
 */
public class ConfigurationManager {

    private final static String                   configFileName            = "config.txt";
    private final static String                   actionFileName            = "actions.txt";
    private final static String                   defaultActionFileName     = "default_actions.txt";
    private final static String                   descriptionsFileName      = "descriptions.txt";

    private final Map<String, ConfigurationCache> worldnameToConfigCacheMap = new HashMap<String, ConfigurationCache>();

    // Only use one filehandler per file, therefore keep open filehandlers in a
    // map
    private final Map<File, FileHandler>          fileToFileHandlerMap      = new HashMap<File, FileHandler>();

    private final Configuration                   defaultConfig;

    private class LogFileFormatter extends Formatter {

        private final SimpleDateFormat date;

        public LogFileFormatter() {
            date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            Throwable ex = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if(ex != null) {
                StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }

    // Our personal logger
    // private final static String loggerName = "cc.co.evenprime.nocheat";
    // public final Logger logger = Logger.getLogger(loggerName);

    public ConfigurationManager(String rootConfigFolder) {

        ActionManager actionManager = new ActionManager();
        // Parse actions file
        // MOVE TO ACTIONMANAGER PARSER OR SOMETHING
        initializeActions(rootConfigFolder, actionManager);
        
        defaultConfig = new DefaultConfiguration(actionManager);
        
        // Setup the configuration tree
        initializeConfig(rootConfigFolder, actionManager);

    }

    private void initializeActions(String rootConfigFolder, ActionManager actionManager) {

        File defaultActionsFile = new File(rootConfigFolder, defaultActionFileName);

        // Write the current default action file into the target folder
        DefaultConfiguration.writeDefaultActionFile(defaultActionsFile);

        // now parse that file again
        FlatFileAction parser = new FlatFileAction(defaultActionsFile);
        List<Action> defaultActions = parser.read();

        for(Action a : defaultActions) {
            actionManager.addAction(a);
        }

        // Check if the "custom" action file exists, if not, create one
        File customActionsFile = new File(rootConfigFolder, actionFileName);
        if(!customActionsFile.exists()) {
            DefaultConfiguration.writeActionFile(customActionsFile);
        }

        parser = new FlatFileAction(customActionsFile);
        List<Action> customActions = parser.read();

        for(Action a : customActions) {
            actionManager.addAction(a);
        }
    }

    /**
     * Read the configuration file and assign either standard values or whatever
     * is declared in the file
     * 
     * @param configurationFile
     */
    private void initializeConfig(String rootConfigFolder, ActionManager action) {

        // First try to obtain and parse the global config file
        FlatFileConfiguration root;
        File globalConfigFile = getGlobalConfigFile(rootConfigFolder);

        root = new FlatFileConfiguration(defaultConfig, true, globalConfigFile);
        try {
            root.load(action);
        } catch(Exception e) {

        }

        try {
            root.save();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Create a corresponding Configuration Cache
        // put the global config on the config map
        worldnameToConfigCacheMap.put(null, new ConfigurationCache(root, setupFileLogger(new File(rootConfigFolder, root.getString(DefaultConfiguration.LOGGING_FILENAME)))));

        // Try to find world-specific config files
        Map<String, File> worldFiles = getWorldSpecificConfigFiles(rootConfigFolder);

        for(String worldName : worldFiles.keySet()) {

            File worldConfigFile = worldFiles.get(worldName);

            FlatFileConfiguration world = new FlatFileConfiguration(root, false, worldConfigFile);

            try {
                world.load(action);

                worldnameToConfigCacheMap.put(worldName, createConfigurationCache(rootConfigFolder, world));

                // write the config file back to disk immediately
                world.save();

            } catch(IOException e) {
                System.out.println("NoCheat: Couldn't load world-specific config for " + worldName);
            }
        }
    }

    private ConfigurationCache createConfigurationCache(String rootConfigFolder, Configuration configProvider) {

        return new ConfigurationCache(configProvider, setupFileLogger(new File(rootConfigFolder, configProvider.getString(DefaultConfiguration.LOGGING_FILENAME))));

    }

    public static File getGlobalConfigFile(String rootFolder) {

        File globalConfig = new File(rootFolder, configFileName);

        return globalConfig;
    }

    public static Map<String, File> getWorldSpecificConfigFiles(String rootConfigFolder) {

        HashMap<String, File> files = new HashMap<String, File>();

        File rootFolder = new File(rootConfigFolder);
        if(rootFolder.isDirectory()) {
            for(File f : rootFolder.listFiles()) {
                if(f.isFile()) {
                    String filename = f.getName();
                    if(filename.matches(".+_" + configFileName + "$")) {
                        // Get the first part = world name
                        String worldname = filename.substring(0, filename.length() - (configFileName.length() + 1));
                        files.put(worldname, f);
                    }
                }
            }
        }
        return files;
    }

    private Logger setupFileLogger(File logfile) {

        FileHandler fh = fileToFileHandlerMap.get(logfile);

        if(fh == null) {
            try {
                try {
                    logfile.getParentFile().mkdirs();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                fh = new FileHandler(logfile.getCanonicalPath(), true);
                // We decide before logging what gets logged there anyway
                // because different worlds may use this filehandler and
                // therefore may need to log different message levels
                fh.setLevel(Level.ALL);
                fh.setFormatter(new LogFileFormatter());
                fileToFileHandlerMap.put(logfile, fh);

            } catch(SecurityException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        // this logger will be used ONLY for logging to a single log-file and
        // only
        // in this plugin, therefore it doesn't need any namespace
        Logger l = Logger.getAnonymousLogger();
        l.setLevel(Level.INFO);
        // Ignore parent's settings
        l.setUseParentHandlers(false);
        l.addHandler(fh);

        return l;
    }

    /**
     * Reset the loggers and flush and close the fileHandlers
     * to be able to use them next time without problems
     */
    public void cleanup() {

        // Remove handlers from the logger
        for(ConfigurationCache c : worldnameToConfigCacheMap.values()) {
            for(Handler h : c.logging.filelogger.getHandlers()) {
                c.logging.filelogger.removeHandler(h);
            }
        }

        // Close all file handlers
        for(FileHandler fh : fileToFileHandlerMap.values()) {
            fh.flush();
            fh.close();
        }
    }

    /**
     * Get the cache of the specified world, or the default cache,
     * if no cache exists for that world.
     * 
     * @param worldname
     * @return
     */
    public ConfigurationCache getConfigurationCacheForWorld(String worldname) {

        ConfigurationCache cache = worldnameToConfigCacheMap.get(worldname);

        if(cache != null) {
            return cache;
        } else {
            // Enter a reference to the cache under the new name
            // to be faster in looking it up later
            cache = worldnameToConfigCacheMap.get(null);
            worldnameToConfigCacheMap.put(worldname, cache);

            return cache;
        }
    }

    public static File getDescriptionFile(String rootConfigFolder) {

        return new File(rootConfigFolder, descriptionsFileName);
    }

    public static File getDefaultActionFile(String rootConfigFolder) {
        return new File(rootConfigFolder, defaultActionFileName);
    }

    public static File getActionFile(String rootConfigFolder) {
        return new File(rootConfigFolder, actionFileName);
    }
}
