package fr.neatmonster.nocheatplus.config;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.actions.ActionFactory;
import fr.neatmonster.nocheatplus.logging.LogUtil;

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
    
    public static interface ActionFactoryFactory{
        public ActionFactory newActionFactory(Map<String, Object> library);
    }
    
    private static ActionFactoryFactory actionFactoryFactory = new ActionFactoryFactory() {
        @Override
        public final ActionFactory newActionFactory(final Map<String, Object> library) {
            return new ActionFactory(library);
        }
    };
    
    /** The map containing the configuration files per world. */
    private static final Map<String, ConfigFile> worldsMap   = new HashMap<String, ConfigFile>();
    
    private static final WorldConfigProvider<ConfigFile> worldConfigProvider = new WorldConfigProvider<ConfigFile>() {
		
		@Override
		public ConfigFile getDefaultConfig() {
			return ConfigManager.getConfigFile();
		}
		
		@Override
		public ConfigFile getConfig(String worldName) {
			return ConfigManager.getConfigFile(worldName);
		}
		
		@Override
		public Collection<ConfigFile> getAllConfigs() {
			return ConfigManager.worldsMap.values();
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
        if (factory != null){
        	actionFactoryFactory = factory;
        }
        else{
        	actionFactoryFactory = new ActionFactoryFactory() {
        		@Override
                public final ActionFactory newActionFactory(final Map<String, Object> library) {
                    return new ActionFactory(library);
                }
        	};
        }   
        for (final ConfigFile config : worldsMap.values()){
            config.regenerateActionLists();
        }
    }
    
    public static ActionFactoryFactory getActionFactoryFactory(){
        return actionFactoryFactory;
    }
    
    /**
     * Get the WorldConfigProvider in use.
     * @return
     */
	public static WorldConfigProvider<ConfigFile> getWorldConfigProvider() {
		return worldConfigProvider;
	}

    /**
     * Cleanup.
     */
    public static void cleanup() {
    	
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
        if (configFile != null){
        	return configFile;
        }
        // Expensive only once, for the rest of runtime the file is returned fast.
    	synchronized(ConfigManager.class){
    		// Need to check again.
    		if (worldsMap.containsKey(worldName)){
    			return worldsMap.get(worldName);
    		}
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
    public static synchronized void init(final Plugin plugin) {
    	// (This can lead to minor problems with async checks during reloading.)
    	worldsMap.clear();
        // Try to obtain and parse the global configuration file.
        final File globalFile = new File(plugin.getDataFolder(), "config.yml");
        PathUtils.processPaths(globalFile, "global config", false);
        final ConfigFile globalConfig = new ConfigFile();
        globalConfig.setDefaults(new DefaultConfig());
        globalConfig.options().copyDefaults(true);
        if (globalFile.exists()){
        	try {
                globalConfig.load(globalFile);
                // Quick shallow ugly fix: only save back if loading was successful.
                try {
                    if (globalConfig.getBoolean(ConfPaths.SAVEBACKCONFIG)){
                    	if (!globalConfig.contains(ConfPaths.CONFIGVERSION_CREATED)){
                    		// Workaround.
                    		globalConfig.set(ConfPaths.CONFIGVERSION_CREATED, DefaultConfig.buildNumber);
                    	}
                    	globalConfig.set(ConfPaths.CONFIGVERSION_SAVED, DefaultConfig.buildNumber);
                    	globalConfig.save(globalFile);
                    }
                } catch (final Exception e) {
                	LogUtil.logSevere("[NoCheatPlus] Could not save back config.yml (see exception below).");
                    LogUtil.logSevere(e);
                }
            } catch (final Exception e) {
            	LogUtil.logSevere("[NoCheatPlus] Could not load config.yml (see exception below).  Continue with default settings...");
            	LogUtil.logSevere(e);
            }
        }
        else {
            globalConfig.options().header("This configuration was auto-generated by NoCheatPlus.");
            globalConfig.options().copyHeader(true);
            try {
            	globalConfig.set(ConfPaths.CONFIGVERSION_CREATED, DefaultConfig.buildNumber);
            	globalConfig.set(ConfPaths.CONFIGVERSION_SAVED, DefaultConfig.buildNumber);
                globalConfig.save(globalFile);
            } catch (final Exception e) {
            	LogUtil.logSevere(e);
            }
        }
        globalConfig.regenerateActionLists();
        worldsMap.put(null, globalConfig);

        
        final MemoryConfiguration worldDefaults = PathUtils.getWorldsDefaultConfig(globalConfig); 
        
        // Try to obtain and parse the world-specific configuration files.
        final HashMap<String, File> worldFiles = new HashMap<String, File>();
        if (plugin.getDataFolder().isDirectory()){
        	for (final File file : plugin.getDataFolder().listFiles()){
            	if (file.isFile()) {
                    final String fileName = file.getName();
                    if (fileName.matches(".+_config.yml$")) {
                        final String worldname = fileName.substring(0, fileName.length() - 11);
                        worldFiles.put(worldname, file);
                    }
                }
            } 
        }   
        for (final Entry<String, File> worldEntry : worldFiles.entrySet()) {
            final File worldFile = worldEntry.getValue();
            PathUtils.processPaths(worldFile, "world " + worldEntry.getKey(), true);
            final ConfigFile worldConfig = new ConfigFile();
            worldConfig.setDefaults(worldDefaults);
            worldConfig.options().copyDefaults(true);
            try {
            	worldConfig.load(worldFile);
                worldsMap.put(worldEntry.getKey(), worldConfig);
                try{
                	if (worldConfig.getBoolean(ConfPaths.SAVEBACKCONFIG)) worldConfig.save(worldFile);
                } catch (final Exception e){
                	LogUtil.logSevere("[NoCheatPlus] Couldn't save back world-specific configuration for " + worldEntry.getKey() + " (see exception below).");
                	LogUtil.logSevere(e);
                }
            } catch (final Exception e) {
            	LogUtil.logSevere("[NoCheatPlus] Couldn't load world-specific configuration for " + worldEntry.getKey() + " (see exception below). Continue with global default settings...");
            	LogUtil.logSevere(e);
            }
            worldConfig.setDefaults(globalConfig);
            worldConfig.options().copyDefaults(true);
            worldConfig.regenerateActionLists();
        }
    }
    
    /**
     * Set a property in all configs. Might use with DataManager.clearConfigs if configs might already be in use.
     * @param path
     * @param value
     */
    public static void setForAllConfigs(String path, Object value){
    	for (final ConfigFile cfg : worldsMap.values()){
    		cfg.set(path, value);
    	}
    }
    
    /**
     * Get the maximally found number for the given config path. This does not throw errors. It will return null, if nothing is found or all lookups failed otherwise.
     * <br>
     * Note: What happens with things like NaN is unspecified.
     * @param path Config path.
     * @return Value or null.
     */
    public static Double getMaxNumberForAllConfigs(final String path){
    	Number max = null;  
    	for (final ConfigFile config : worldsMap.values()){
    		try{
    			final Object obj = config.get(path);
    			if (obj instanceof Number){
    				final Number num = (Number) obj;
    				if (max == null || num.doubleValue() > max.doubleValue()){
    					max = num; 
    				}
    			}
    		}
    		catch (Throwable t){
    			// Holzhammer
    		}
    	}
    	return max.doubleValue();
    }
    
    /**
     * Get the minimally found number for the given config path. This does not throw errors. It will return null, if nothing is found or all lookups failed otherwise.
     * <br>
     * Note: What happens with things like NaN is unspecified.
     * @param path Config path.
     * @return Value or null.
     */
    public static Double getMinNumberForAllConfigs(final String path){
    	Number min = null;  
    	for (final ConfigFile config : worldsMap.values()){
    		try{
    			final Object obj = config.get(path);
    			if (obj instanceof Number){
    				final Number num = (Number) obj;
    				if (min == null || num.doubleValue() < min.doubleValue()){
    					min = num; 
    				}
    			}
    		}
    		catch (Throwable t){
    			// Holzhammer
    		}
    	}
    	return min.doubleValue();
    }
    
    // TODO: consider: filter(Max|Min)NumberForAllConfigs(String path, String filerPath, boolean filterPreset)
    
}
