package fr.neatmonster.nocheatplus.config;

import java.util.HashMap;
import java.util.Map;

import fr.neatmonster.nocheatplus.utilities.ds.corw.LinkedHashMapCOW;

/**
 * Simple cache for configurations, adding some convenience functionality.
 * @author web4web1
 *
 */
public abstract class ConfigCache <K, C> {

    private final Map<K, C> configs;
    private final boolean cow;

    /**
     * 
     * @param cow Set to true, to use copy-on-write and synchronize writing.
     * @param initialCapacity
     */
    public ConfigCache(boolean cow, int initialCapacity) {
        this.cow = cow;
        // Linked or not linked ?
        if (cow) {
            configs = new LinkedHashMapCOW<K, C>(initialCapacity);
        } else {
            configs = new HashMap<K, C>(initialCapacity);
        }
    }

    public boolean hasConfig(final K key) {
        return configs.containsKey(key);
    }

    public C getConfig(final K key) {
        final C config = configs.get(key);
        if (config == null) {
            return createConfig(key);
        } else {
            return config;
        }
    }

    private C createConfig(final K key) {
        final C config = newConfig(key);
        if (!cow) {
            configs.put(key, config);
            return config;
        } else {
            return addConfigCOW(key, config);
        }
    }

    private C addConfigCOW(final K key, final C config) {
        // Re-check to ensure FCFS.
        synchronized (configs) {
            if (configs.containsKey(key)) {
                return configs.get(key);
            } else {
                configs.put(key, config);
                return config;
            }
        }
    }

    public void clearAllConfigs() {
        configs.clear();
    }

    /**
     * Factory method. In case of cow, not all returned instances might be used.
     * @param key
     * @return
     */
    protected abstract C newConfig(final K key);

}
