/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.actions.ActionFactory;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;

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
    private static Map<String, ConfigFile> worldsMap = new LinkedHashMap<String, ConfigFile>();

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

    private static boolean isInitialized = false;

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
     * This will reset all ActionFactories to null (lazy initialization),
     *  call setAllActionFactories to ensure action factories are ready. 
     *  To be on the safe side also call DataManager.clearConfigs().
     *  <hr>
     *  To Hook into NCP for setting the factories, you should register a INotifyReload instance
     *  with the NoCheatPlusAPI using the annotation SetupOrder with a higher negative value (-1000, see INotifyReload javadoc).
     * @param factory
     */
    // TODO: Register at GenericInstanceRegistry instead, store a handle at best (get rid of 'the other' static registries).
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
        // Use lazy resetting.
        for (final ConfigFile config : worldsMap.values()){
            config.setActionFactory(null);
        }
    }

    public static ActionFactoryFactory getActionFactoryFactory(){
        return actionFactoryFactory;
    }

    /**
     * Force setting up all configs action factories.
     */
    public static void setAllActionFactories(){
        for (final ConfigFile config : worldsMap.values()){
            config.setActionFactory();
        }
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
        isInitialized = false;
        setActionFactoryFactory(null);
        // TODO: Remove references of config files ?
    }

    /**
     * Gets the configuration file. Can be called from any thread.
     * 
     * @return the configuration file
     */
    public static ConfigFile getConfigFile() {
        return worldsMap.get(null);
    }

    /**
     * (Synchronized version).
     * @return
     * @deprecated getConfigFile() is thread-safe now.
     */
    @Deprecated
    public static synchronized ConfigFile getConfigFileSync() {
        return getConfigFile();
    }

    /**
     * Gets the configuration file. Can be called from any thread.
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
        // Expensive only once per world, for the rest of the runtime the file is returned fast.
        synchronized(ConfigManager.class){
            // Need to check again.
            if (worldsMap.containsKey(worldName)){
                return worldsMap.get(worldName);
            }
            // Copy the whole map with the default configuration set for this world.
            final Map<String, ConfigFile> newWorldsMap = new LinkedHashMap<String, ConfigFile>(ConfigManager.worldsMap);
            final ConfigFile globalConfig = newWorldsMap.get(null);
            newWorldsMap.put(worldName, globalConfig);
            ConfigManager.worldsMap = newWorldsMap;
            return globalConfig;
        }
    }

    /**
     * (Synchronized version).
     * @param worldName
     * @return
     * @deprecated getConfigFile() is thread-safe now.
     */
    @Deprecated
    public static synchronized ConfigFile getConfigFileSync(final String worldName) {
        return getConfigFile(worldName);
    }

    /**
     * Initializes the configuration manager. Must be called in the main thread.
     * 
     * @param plugin
     *            the instance of NoCheatPlus
     */
    public static synchronized void init(final Plugin plugin) {
        // (This can lead to minor problems with async checks during reloading.)
        LinkedHashMap<String, ConfigFile> newWorldsMap = new LinkedHashMap<String, ConfigFile>();
        // Try to obtain and parse the global configuration file.
        final File globalFile = new File(plugin.getDataFolder(), "config.yml");
        final ConfigFile defaultConfig = new DefaultConfig();
        final int maxBuildContained = defaultConfig.getMaxLastChangedBuildNumber();
        // TODO: Detect changes to the configuration (only save back if necessary.).
        PathUtils.processPaths(globalFile, "global config", false);
        final ConfigFile globalConfig = new ConfigFile();
        globalConfig.setDefaults(defaultConfig);
        globalConfig.options().copyDefaults(true);
        if (globalFile.exists()){
            try {
                globalConfig.load(globalFile);
                // Quick shallow ugly fix: only save back if loading was successful.
                try {
                    if (globalConfig.getBoolean(ConfPaths.SAVEBACKCONFIG)){
                        boolean overrideCreated = false;
                        if (!globalConfig.contains(ConfPaths.CONFIGVERSION_CREATED)){
                            // Workaround.
                            overrideCreated = true;
                        }
                        if (!overrideCreated && globalConfig.getInt(ConfPaths.CONFIGVERSION_CREATED, 0) >= 0
                                && ConfigManager.isConfigUpToDate(globalConfig, 0) == null) {
                            // Workaround: Update the created build number, to not warn on further changes.
                            overrideCreated = true;
                        }
                        globalConfig.set(ConfPaths.CONFIGVERSION_SAVED, maxBuildContained);
                        if (overrideCreated) {
                            globalConfig.set(ConfPaths.CONFIGVERSION_CREATED, maxBuildContained);
                        }
                        // TODO: Only save back if really changed?
                        globalConfig.save(globalFile);
                    }
                } catch (final Exception e) {
                    StaticLog.logSevere("Could not save back config.yml (see exception below).");
                    StaticLog.logSevere(e);
                }
            } catch (final Exception e) {
                StaticLog.logSevere("Could not load config.yml (see exception below). Continue with default settings...");
                StaticLog.logSevere(e);
            }
        }
        else {
            globalConfig.options().header("This configuration was auto-generated by NoCheatPlus.");
            globalConfig.options().copyHeader(true);
            try {
                globalConfig.set(ConfPaths.CONFIGVERSION_CREATED, maxBuildContained);
                globalConfig.set(ConfPaths.CONFIGVERSION_SAVED, maxBuildContained);
                globalConfig.save(globalFile);
            } catch (final Exception e) {
                StaticLog.logSevere(e);
            }
        }
        //        globalConfig.setActionFactory();
        newWorldsMap.put(null, globalConfig);


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
                newWorldsMap.put(worldEntry.getKey(), worldConfig);
                try{
                    if (worldConfig.getBoolean(ConfPaths.SAVEBACKCONFIG)) {
                        worldConfig.save(worldFile);
                    }
                } catch (final Exception e){
                    StaticLog.logSevere("Couldn't save back world-specific configuration for " + worldEntry.getKey() + " (see exception below).");
                    StaticLog.logSevere(e);
                }
            } catch (final Exception e) {
                StaticLog.logSevere("Couldn't load world-specific configuration for " + worldEntry.getKey() + " (see exception below). Continue with global default settings...");
                StaticLog.logSevere(e);
            }
            worldConfig.setDefaults(globalConfig);
            worldConfig.options().copyDefaults(true);
            //            worldConfig.setActionFactory();
        }
        ConfigManager.worldsMap = newWorldsMap;
        isInitialized = true;
    }

    @Deprecated
    public static String isConfigUpToDate(final ConfigFile globalConfig) {
        return isConfigUpToDate(globalConfig, -1);
    }

    /**
     * Retrieve a warning string containing information about changed default
     * configuration values.
     * 
     * @param globalConfig
     * @param maxLines
     *            Maximum number of configuration paths to include in the output
     *            - note that extra lines will always be included, for
     *            introduction and in the end some hints. A negative value means
     *            no limit.
     * @return null if everything is fine, a string with a message stating
     *         problems otherwise.
     */
    public static String isConfigUpToDate(final ConfigFile globalConfig, final int maxPaths) {
        Object created_o = globalConfig.get(ConfPaths.CONFIGVERSION_CREATED);
        int buildCreated = -1;
        if (created_o != null && created_o instanceof Integer) {
            buildCreated = ((Integer) created_o).intValue();
        }
        // Silence version checking with a value < 0.
        if (buildCreated < 0) {
            return null;
        }
        final ConfigFile defaultConfig = new DefaultConfig();
        final int maxBuildContained = defaultConfig.getMaxLastChangedBuildNumber();
        // Legacy build number comparison.
        final int currentBuild = BuildParameters.buildNumber;
        if (currentBuild != Integer.MIN_VALUE && buildCreated > Math.max(maxBuildContained, currentBuild)) {
            // Installed an older version of NCP.
            return "Your configuration seems to be created by a newer plugin version.\n" + "Some settings could have changed, you should regenerate it!";
        }
        // So far so good... test individual paths.
        final List<String> problems = new LinkedList<String>();
        final Map<String, Integer> lastChangedBuildNumbers = defaultConfig.getLastChangedBuildNumbers();
        // TODO: Consider some behavior for entire nodes ?
        for (final Entry<String, Integer> entry : lastChangedBuildNumbers.entrySet()) {
            final int defaultBuild = entry.getValue();
            if (defaultBuild <= buildCreated) {
                // Ignore, might've been changed on purpose.
                continue;
            }
            final String path = entry.getKey();
            final Object defaultValue = defaultConfig.get(path);
            if (defaultValue instanceof ConfigurationSection) {
                problems.add(path + (maxPaths >= 0 ? "" : (" - Changed with build " + defaultBuild + ", can not handle entire configuration sections yet. ")));
                continue;
            }
            final Object currentValue = globalConfig.get(path);
            if (currentValue == null || defaultValue == null) {
                // To be handled elsewhere (@Moved / whatever).
                continue;
            }
            if (defaultBuild > buildCreated && !defaultValue.equals(currentValue)) {
                problems.add(path + (maxPaths >= 0 ? "" : (" - Changed with build " + defaultBuild + ".")));
                continue;
            }
        }
        if (!problems.isEmpty()) {
            Collections.sort(problems); // Sort by path.
            final List<String> outList;
            if (maxPaths >= 0 && problems.size() > maxPaths) {
                outList = new ArrayList<String>(problems.subList(0, maxPaths));
            }
            else {
                outList = problems;
            }
            outList.add(0, "The following configuration default values have changed:");
            if (maxPaths >= 0) {
                outList.add("-> " + problems.size() + " entries in total, check the log file(s) for a complete list.");
            }
            else {
                outList.add("(Remove/update individual values or set configversion.created to " + maxBuildContained + " to ignore all, then reload the configuration with the 'ncp reload' command.)");
            }
            return StringUtil.join(outList, "\n");
        }
        // No errors could be determined (or versions coudl not be determined): ignore.
        return null;
    }

    /**
     * Informal test if the init method completed (no details are reflected).
     * @return
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Set a property for all configurations. Might use with DataManager.clearConfigs if check-configurations might already be in use.
     * @param path
     * @param value
     */
    public static synchronized void setForAllConfigs(String path, Object value){
        final Map<String, ConfigFile> newWorldsMap = new LinkedHashMap<String, ConfigFile>(ConfigManager.worldsMap);
        for (final ConfigFile cfg : newWorldsMap.values()){
            cfg.set(path, value);
        }
        ConfigManager.worldsMap = newWorldsMap;
    }

    /**
     * Check if any config has a boolean set to true for the given path.
     * 
     * @param path
     * @return True if any config has a boolean set to true for the given path.
     */
    public static boolean isTrueForAnyConfig(String path) {
        for (final ConfigFile cfg : worldsMap.values()){
            if (cfg.getBoolean(path, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if any config has the path set to true, or to default in case
     * decideOptimistically is set, or not set in case trueForNotSet is set.
     * 
     * @param path
     * @param decideOptimistically
     * @param trueForNotSet
     * @return
     */
    public static boolean isAlmostTrueForAnyConfig(String path, boolean decideOptimistically, boolean trueForNotSet) {
        for (final ConfigFile cfg : worldsMap.values()){
            if (cfg.getAlmostBoolean(path, decideOptimistically, trueForNotSet)) {
                return true;
            }
        }
        return false;
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
