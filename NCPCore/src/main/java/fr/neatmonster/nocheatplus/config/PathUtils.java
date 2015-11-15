package fr.neatmonster.nocheatplus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;

import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.CharPrefixTree;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;

public class PathUtils {

    // TODO: Make this a class to be able to process multiple files with different paths and annotations.

    // Deprecated paths.
    private static final Set<String> deprecatedFields = new LinkedHashSet<String>();
    private static final SimpleCharPrefixTree deprecatedPrefixes = new SimpleCharPrefixTree();

    // Paths only for the global config.
    private static final Set<String> globalOnlyFields = new HashSet<String>();
    private static final SimpleCharPrefixTree globalOnlyPrefixes = new SimpleCharPrefixTree();

    // Paths moved to other paths.
    private static final Map<String, Moved> movedPaths = new LinkedHashMap<String, Moved>();

    static{
        initPaths();
    }

    /**
     * Initialize annotation-based path properties.
     * @return
     */
    private static void initPaths() {
        deprecatedFields.clear();
        deprecatedPrefixes.clear();
        globalOnlyFields.clear();
        globalOnlyPrefixes.clear();
        movedPaths.clear();
        for (final Field field : ConfPaths.class.getDeclaredFields()) {
            if (field.getType() != String.class) {
                // Only process strings.
                continue;
            }
            final String fieldName = field.getName();

            checkAddPrefixes(field, fieldName, GlobalConfig.class, globalOnlyFields, globalOnlyPrefixes);
            checkAddPrefixes(field, fieldName, Deprecated.class, deprecatedFields, deprecatedPrefixes);
            if (field.isAnnotationPresent(Moved.class)) {
                // TODO: Prefixes: Might later support relocating  entire sections with one annotation?
                addMoved(field, field.getAnnotation(Moved.class));
            }
        }
    }

    private static void checkAddPrefixes(Field field, String fieldName, Class<? extends Annotation> annotation, Set<String> fieldNames, SimpleCharPrefixTree pathPrefixes) {
        if (field.isAnnotationPresent(annotation)) {
            fieldNames.add(fieldName);
            addPrefixesField(field, pathPrefixes);
        }
        else {
            for (final String refName : fieldNames) {
                if (fieldName.startsWith(refName)) {
                    addPrefixesField(field, pathPrefixes);
                }
            }
        }
    }

    private static void addPrefixesField(Field field, SimpleCharPrefixTree pathPrefixes) {
        try {
            final String path = field.get(null).toString();
            if (path != null) {
                pathPrefixes.feed(path);
            }
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }

    private static void addMoved(final Field field, final Moved rel) {
        try {
            final String path = field.get(null).toString();
            movedPaths.put(path, rel);
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
    }

    /**
     * Warn on the console if paths are used.
     * @param config
     * @param paths
     * @param msgHeader
     * @param warnedPaths Paths which were found, can be null.
     */
    protected static void warnPaths(final ConfigurationSection config, final CharPrefixTree<?, ?> paths, final String msgPrefix, final Set<String> warnedPaths) {
        for (final String path : config.getKeys(true)) {
            if (paths.hasPrefix(path)) {
                StaticLog.logWarning("Config path '" + path + "'" + msgPrefix);
                if (warnedPaths != null) {
                    warnedPaths.add(path);
                }
            }	
        }
    }

    /**
     * Run all warning checks and alter the configuration file if necessary (GlobalConfig, Deprecated, Moved).
     * @param file
     * @param configName
     */
    public static void processPaths(File file, String configName, boolean isWorldConfig) {
        ConfigFile config = new ConfigFile();
        try {
            config.load(file);
            ConfigFile newConfig = processPaths(config, configName, isWorldConfig);
            if (newConfig != null) {
                config = newConfig;
                try{
                    config.save(file);
                }
                catch(Throwable t) {
                    // Do log this one.
                    StaticLog.logSevere("Failed to save configuration (" + configName + ") with changes: " + t.getClass().getSimpleName());
                    StaticLog.logSevere(t);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (InvalidConfigurationException e) {
        }
    }

    /**
     * Process configuration annotations, might change the given configuration or return a new one.
     * @param config Configuration instance.
     * @param configName A name for logging errors.
     * @param isWorldConfig
     * @return A new configuration instance if paths had to be removed (i.e. a
     *         new one was needed), null if the same configuration instance
     *         could be used (paths still might have been added).
     */
    public static ConfigFile processPaths(ConfigFile config, final String configName, final boolean isWorldConfig) {
        final Set<String> removePaths = new LinkedHashSet<String>();
        final Map<String, Object> addPaths = new LinkedHashMap<String, Object>();
        if (isWorldConfig) {
            // TODO: might remove these [though some global only paths might actually work].
            processGlobalOnlyPaths(config, configName, null);
        }
        processDeprecatedPaths(config, configName, removePaths);
        processMovedPaths(config, configName, removePaths, addPaths);
        boolean changed = false;
        if (!removePaths.isEmpty()) {
            config = removePaths(config, removePaths);
            changed = true;
        }
        if (!addPaths.isEmpty()) {
            setPaths(config, addPaths, false);
            changed = true;
        }
        if (changed) {
            return config;
        } else {
            return null;
        }
    }

    /**
     * Set paths.
     * @param config
     * @param addPaths
     */
    public static void setPaths(final ConfigurationSection config, final Map<String, Object> setPaths, final boolean override) {
        setPaths(config, setPaths, "", override);
    }

    /**
     * Recursive version supporting ConfigurationSection. 
     * @param config
     * @param setPaths
     * @param pathPrefix
     */
    protected static void setPaths(final ConfigurationSection config, final Map<String, Object> setPaths, final String pathPrefix, final boolean override) {
        for (final Entry<String, Object> entry : setPaths.entrySet()) {
            final String path = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof ConfigurationSection) {
                // Deep or not should not matter here.
                setPaths(config, ((ConfigurationSection) value).getValues(true), pathPrefix + path + ".", override);
            } else {
                if (override || !config.contains(path)) {
                    config.set(pathPrefix + path, value);
                }
            }
        }
    }

    /**
     * Get a new ConfigFile instance with all paths removed (recursively by prefix).
     * @param config
     * @param removePaths
     * @return
     */
    public static ConfigFile removePaths(final ConfigFile config, final Collection<String> removePaths) {
        final SimpleCharPrefixTree prefixes = new SimpleCharPrefixTree();
        for (final String path : removePaths) {
            prefixes.feed(path);
        }
        final ConfigFile newConfig = new ConfigFile();
        for (final Entry<String, Object> entry : config.getValues(true).entrySet()) {
            final String path = entry.getKey();
            final Object value = entry.getValue();
            // TODO: To support moving entire sections, this needs to be changed.
            if (value instanceof ConfigurationSection) {
                continue;
            }
            if (!prefixes.hasPrefix(path)) {
                newConfig.set(path, value);
            }
        }
        return newConfig;
    }

    /**
     * 
     * @param config
     * @param configName
     * @param removePaths
     * @param addPaths 
     */
    protected static void processMovedPaths(final ConfigurationSection config, final String configName, final Set<String> removePaths, final Map<String, Object> addPaths) {
        for (final Entry<String, Moved> entry : movedPaths.entrySet()) {
            final String path = entry.getKey();
            final Object value = config.get(path);
            if (config.contains(path)) {
                final Moved moved = entry.getValue();
                if (value instanceof ConfigurationSection) {
                    if (moved.configurationSection()) {
                        // TODO: Ensure those can be processed at all.
                    } else {
                        // Ignore configuration sections.
                        continue;
                    }
                }
                final String newPath = moved.newPath();
                final String to;
                if (newPath == null || newPath.isEmpty()) {
                    to = ".";
                }
                else {
                    to = " to '" + newPath + "'.";
                    addPaths.put(newPath, value);
                    removePaths.add(path);
                }
                StaticLog.logWarning("Config path '" + path + "' (" + configName + ") has been moved" + to);
            }
        }
    }

    /**
     * Warn about paths that are deprecated (not in use).
     * @param config
     * @param paths
     * @param configName
     */
    protected static void processDeprecatedPaths(ConfigurationSection config, String configName, final Set<String> removePaths) {
        warnPaths(config, deprecatedPrefixes, " (" + configName + ") is not in use anymore.", removePaths);
    }

    /**
     * Warn about paths that are only to be set in the global config.
     * @param config
     * @param paths
     * @param configName
     */
    protected static void processGlobalOnlyPaths(ConfigurationSection config, String configName, final Set<String> removePaths) {
        warnPaths(config, globalOnlyPrefixes, " (" + configName + ") should only be set in the global configuration.", removePaths);
    }

    /**
     * A config file only containing the entries that are not set as global only.
     * @param defaultConfig
     * @return
     */
    public static MemoryConfiguration getWorldsDefaultConfig(final MemoryConfiguration defaultConfig) {
        final char sep = defaultConfig.options().pathSeparator();
        final MemoryConfiguration config = new ConfigFile();
        config.options().pathSeparator(sep);
        final Map<String, Object> defaults = defaultConfig.getValues(false);
        for (final Entry<String, Object> entry : defaults.entrySet()) {
            final String part = entry.getKey();
            if (!part.isEmpty() && !mayBeInWorldConfig(part)) {
                continue;
            }
            final Object value = entry.getValue();
            if (value instanceof ConfigurationSection) {
                addWorldConfigSection(config, (ConfigurationSection) value, part, sep);
            }
            else {
                config.set(part, value);
            }
        }
        return config;
    }

    protected static void addWorldConfigSection(final ConfigurationSection config, final ConfigurationSection section, final String path, final char sep) {
        final Map<String, Object> values = section.getValues(false);
        for (final Entry<String, Object> entry : values.entrySet()) {
            final String fullPath = path + sep + entry.getKey();
            if (!mayBeInWorldConfig(fullPath)) {
                continue;
            }
            final Object value = entry.getValue();
            if (value instanceof ConfigurationSection) {
                addWorldConfigSection(config, (ConfigurationSection) value, fullPath, sep);
            }
            else {
                config.set(fullPath, value);
            }
        }
    }

    public static boolean mayBeInWorldConfig(final String path) {
        if (globalOnlyPrefixes.hasPrefix(path)) {
            return false;
        } else {
            return mayBeInConfig(path);
        }
    }

    public static boolean mayBeInConfig(final String path) {
        if (deprecatedPrefixes.hasPrefix(path)) {
            return false;
        }
        else if (movedPaths.containsKey(path)) {
            return false;
        } else {
            return true;
        }
    }

}
