package fr.neatmonster.nocheatplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;

import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;

public class PathUtils {
	
	/** Field names of ConfPaths. */
	private static final Set<String> globalOnlyFields = new HashSet<String>();
	
	/** Paths of ConfPaths (field values). */
	private static final Set<String> globalOnlyPaths = new LinkedHashSet<String>();
	
	private static final SimpleCharPrefixTree globalOnlyPrefixes = new SimpleCharPrefixTree();
	
	static{
		initGlobalOnlyPaths();
	}
	
    /**
     * Test/evaluation of using annotations to confine config paths to the global config.
     * @return
     */
    private static void initGlobalOnlyPaths(){
    	globalOnlyFields.clear();
    	globalOnlyPaths.clear();
    	globalOnlyPrefixes.clear();
    	for (final Field field : ConfPaths.class.getDeclaredFields()){
    		final String name = field.getName();
    		if (field.isAnnotationPresent(GlobalConfig.class)){
    			globalOnlyFields.add(name);
    			addGlobalOnlyPath(field);
    		}
    		else{
    			for (final String refName : globalOnlyFields){
    				if (name.startsWith(refName) && field.getType() == String.class){
    					addGlobalOnlyPath(field);
    				}
    			}
    		}
    	}
    }
    
    private static void addGlobalOnlyPath(final Field field) {
    	try {
			final String path = field.get(null).toString();
			globalOnlyPaths.add(path);
			globalOnlyPrefixes.feed(path);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

	/**
     * Warn on the console if paths are used.
     * @param config
     * @param paths
     * @param msgHeader
     */
    public static void warnPaths(ConfigFile config, Collection<String> paths, String msgPrefix){
    	final Logger log = Bukkit.getLogger();
    	for (final String path : config.getKeys(true)){
    		if (globalOnlyPrefixes.hasPrefix(path)) 
    			log.warning("[NoCheatPlus] Config path '" + path + "'" + msgPrefix);
    	}
    }
    
    /**
     * Warn about paths that are only to be set in the global config.
     * @param config
     * @param paths
     * @param configName
     */
    public static void warnGlobalOnlyPaths(ConfigFile config, String configName){
    	warnPaths(config, globalOnlyPaths, " (" + configName + ") should only be set in the global configuration.");
    }
    
    public static void warnGlobalOnlyPaths(File file, String configName){
    	final ConfigFile config = new ConfigFile();
    	try {
			config.load(file);
			warnGlobalOnlyPaths(config, configName);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (InvalidConfigurationException e) {
		}
    }
    
    /**
     * A config file only containing the entries that are not set as global only.
     * @param defaultConfig
     * @return
     */
    public static MemoryConfiguration getWorldsDefaultConfig(final ConfigFile defaultConfig){
    	final char sep = defaultConfig.options().pathSeparator();
    	final MemoryConfiguration config = new ConfigFile();
    	config.options().pathSeparator(sep);
    	final Map<String, Object> defaults = defaultConfig.getValues(false);
    	for (final Entry<String, Object> entry : defaults.entrySet()){
    		final String part = entry.getKey();
    		if (!part.isEmpty() && globalOnlyPrefixes.hasPrefix(part)) continue;
    		final Object value = entry.getValue();
    		if (value instanceof ConfigurationSection) addSection(config, (ConfigurationSection) value, part, sep);
    		else config.set(part, value);
    	}
    	return config;
    }

	public static void addSection(final MemoryConfiguration config, final ConfigurationSection section, final String path, final char sep) {
		final Map<String, Object> values = section.getValues(false);
		for (final Entry<String, Object> entry : values.entrySet()){
			final String fullPath = path + sep + entry.getKey();
			if (globalOnlyPrefixes.hasPrefix(fullPath)) continue;
    		final Object value = entry.getValue();
    		if (value instanceof ConfigurationSection) addSection(config, (ConfigurationSection) value, fullPath, sep);
    		else config.set(fullPath, value);
    	}
	}
}
