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

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemoryConfiguration;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ActionFactory;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

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
 * Central location for everything that's described in the configuration file(s).<br>
 * The synchronized methods are to ensure that changing the configurations won't lead to trouble for the asynchronous checks.
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

    /** The log file. */
    public static File                           logFile     = null;
    
    public static interface ActionFactoryFactory{
        public ActionFactory newActionFactory(Map<String, Object> library);
    }
    
    private static ActionFactoryFactory actionFactoryFactory = new ActionFactoryFactory() {
        @Override
        public final ActionFactory newActionFactory(final Map<String, Object> library) {
            return new ActionFactory(library);
        }
    };
    
    /**
     * Factory method.
     * @param library
     * @return
     */
    public static ActionFactory getActionFactory(final Map<String, Object> library){
        return actionFactoryFactory.newActionFactory(library);
    }
    
    /**
     * Set the factory to get actions from.
     * @param factory
     */
    public static void setActionFactoryFactory(ActionFactoryFactory factory){
        if (factory != null) actionFactoryFactory = factory;
        else actionFactoryFactory = new ActionFactoryFactory() {
            @Override
            public final ActionFactory newActionFactory(final Map<String, Object> library) {
                return new ActionFactory(library);
            }
        };
        for (final ConfigFile config : worldsMap.values()){
            config.regenerateActionLists();
        }
    }
    
    public static ActionFactoryFactory getActionFactoryFactory(){
        return actionFactoryFactory;
    }

    /**
     * Cleanup.
     */
    public static void cleanup() {
        fileHandler.flush();
        fileHandler.close();
        final Logger logger = Logger.getLogger("NoCheatPlus");
        logger.removeHandler(fileHandler);
        fileHandler = null;
        setActionFactoryFactory(null);
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
     * (Synchronized version).
     * @return
     */
    public static synchronized ConfigFile getConfigFileSync() {
    	return getConfigFile();
    }

    /**
     * Gets the configuration file.
     * 
     * @param worldName
     *            the world name
     * @return the configuration file
     */
    public static ConfigFile getConfigFile(final String worldName) {
    	final ConfigFile configFile = worldsMap.get(worldName);
        if (configFile != null) return configFile;
        // Expensive only once, for the rest of runtime the file is returned fast.
    	synchronized(ConfigManager.class){
    		// Need to check again.
    		if (worldsMap.containsKey(worldName)) return worldsMap.get(worldName);
    		final ConfigFile globalConfig = getConfigFile();
    		worldsMap.put(worldName, globalConfig);
    		return globalConfig;
    	}
    }
    
    /**
     * (Synchronized version).
     * @param worldName
     * @return
     */
    public static synchronized ConfigFile getConfigFileSync(final String worldName) {
    	return getConfigFile(worldName);
    }

    /**
     * Initializes the configuration manager.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public static synchronized void init(final NoCheatPlus plugin) {
    	worldsMap.clear();
        // Try to obtain and parse the global configuration file.
        final File globalFile = new File(plugin.getDataFolder(), "config.yml");
        final ConfigFile globalConfig = new ConfigFile();
        globalConfig.setDefaults(new DefaultConfig());
        globalConfig.options().copyDefaults(true);
        if (globalFile.exists())
            try {
                globalConfig.load(globalFile);
                // Quick shallow ugly fix: only save back if loading was successful.
                try {
                    if (globalConfig.getBoolean(ConfPaths.SAVEBACKCONFIG)) globalConfig.save(globalFile);
                } catch (final Exception e) {
                	Bukkit.getLogger().severe("[NoCheatPlus] Could not save back config.yml (see exception below).");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
            	Bukkit.getLogger().severe("[NoCheatPlus] Could not load config.yml (see exception below).  Continue with default settings...");
                e.printStackTrace();
            }
        else {
            globalConfig.options().header(
                    "Configuration generated by NoCheatPlus " + plugin.getDescription().getVersion() + ".");
            globalConfig.options().copyHeader(true);
            try {
                globalConfig.save(globalFile);
            } catch (final Exception e) {
            	Bukkit.getLogger().severe("[NoCheatPlus] Could not save default config.yml (see exception below).");
                e.printStackTrace();
            }
        }
        globalConfig.regenerateActionLists();
        worldsMap.put(null, globalConfig);

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
        logFile = new File(plugin.getDataFolder(), globalConfig.getString(ConfPaths.LOGGING_FILENAME));
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
        CheckUtils.fileLogger = logger;

        final MemoryConfiguration worldDefaults = PathUtils.getWorldsDefaultConfig(globalConfig); 
        
        // Try to obtain and parse the world-specific configuration files.
        final HashMap<String, File> worldFiles = new HashMap<String, File>();
        if (plugin.getDataFolder().isDirectory())
            for (final File file : plugin.getDataFolder().listFiles())
                if (file.isFile()) {
                    final String fileName = file.getName();
                    if (fileName.matches(".+_config.yml$")) {
                        final String worldname = fileName.substring(0, fileName.length() - 11);
                        worldFiles.put(worldname, file);
                    }
                }
        for (final Entry<String, File> worldEntry : worldFiles.entrySet()) {
            final File worldFile = worldEntry.getValue();
            PathUtils.warnGlobalOnlyPaths(worldFile, worldEntry.getKey());
            final ConfigFile worldConfig = new ConfigFile();
            worldConfig.setDefaults(worldDefaults);
            worldConfig.options().copyDefaults(true);
            try {
            	worldConfig.load(worldFile);
                worldsMap.put(worldEntry.getKey(), worldConfig);
                try{
                	if (worldConfig.getBoolean(ConfPaths.SAVEBACKCONFIG)) worldConfig.save(worldFile);
                } catch (final Exception e){
                	Bukkit.getLogger().severe("[NoCheatPlus] Couldn't save back world-specific configuration for "
                            + worldEntry.getKey() + " (see exception below).");
                	e.printStackTrace();
                }
            } catch (final Exception e) {
            	Bukkit.getLogger().severe("[NoCheatPlus] Couldn't load world-specific configuration for "
                        + worldEntry.getKey() + " (see exception below). Continue with global default settings...");
                e.printStackTrace();
            }
            worldConfig.setDefaults(globalConfig);
            worldConfig.options().copyDefaults(true);
            worldConfig.regenerateActionLists();
        }
    }
}
