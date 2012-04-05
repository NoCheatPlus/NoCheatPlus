package me.neatmonster.nocheatplus.checks.inventory;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.checks.Check;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;

/**
 * Abstract base class for Inventory checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class InventoryCheck extends Check {

    private static final String id = "inventory";

    public static InventoryConfig getConfig(final ConfigurationCacheStore cache) {
        InventoryConfig config = cache.get(id);
        if (config == null) {
            config = new InventoryConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }

    /**
     * Get the InventoryConfig object that belongs to the world that the player
     * currently resides in.
     * 
     * @param player
     * @return
     */
    public static InventoryConfig getConfig(final NoCheatPlusPlayer player) {
        return getConfig(player.getConfigurationStore());
    }

    /**
     * Get the "InventoryData" object that belongs to the player. Will ensure
     * that such a object exists and if not, create one
     * 
     * @param player
     * @return
     */
    public static InventoryData getData(final NoCheatPlusPlayer player) {
        final DataStore base = player.getDataStore();
        InventoryData data = base.get(id);
        if (data == null) {
            data = new InventoryData();
            base.set(id, data);
        }
        return data;
    }

    public InventoryCheck(final NoCheatPlus plugin, final String name) {
        super(plugin, id, name);
    }
}
