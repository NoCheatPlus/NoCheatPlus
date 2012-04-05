package me.neatmonster.nocheatplus.config;

import java.util.HashMap;
import java.util.Map;

import me.neatmonster.nocheatplus.ConfigItem;

/**
 * A class to keep all configurables of the plugin associated with
 * a world
 * 
 */
public class ConfigurationCacheStore {

    public final LoggingConfig             logging;

    private final Map<String, ConfigItem>  configMap = new HashMap<String, ConfigItem>();

    private final NoCheatPlusConfiguration data;

    /**
     * Instantiate a config cache and populate it with the data of a
     * Config tree (and its parent tree)
     */
    public ConfigurationCacheStore(final NoCheatPlusConfiguration data) {

        logging = new LoggingConfig(data);

        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T extends ConfigItem> T get(final String id) {
        return (T) configMap.get(id);
    }

    public NoCheatPlusConfiguration getConfiguration() {
        return data;
    }

    public void set(final String id, final ConfigItem config) {

        configMap.put(id, config);
    }
}
