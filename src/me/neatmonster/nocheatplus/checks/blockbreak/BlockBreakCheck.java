package me.neatmonster.nocheatplus.checks.blockbreak;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.checks.Check;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;

/**
 * Abstract base class for BlockBreakChecks. Provides some static convenience
 * methods for retrieving data and config objects for players
 * 
 */
public abstract class BlockBreakCheck extends Check {

    private static final String id = "blockbreak";

    public static BlockBreakConfig getConfig(final ConfigurationCacheStore cache) {
        BlockBreakConfig config = cache.get(id);
        if (config == null) {
            config = new BlockBreakConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }

    /**
     * Get the BlockBreakConfig object that belongs to the world that the player
     * currently resides in.
     * 
     * @param player
     * @return
     */
    public static BlockBreakConfig getConfig(final NoCheatPlusPlayer player) {
        return getConfig(player.getConfigurationStore());
    }

    /**
     * Get the "BlockBreakData" object that belongs to the player. Will ensure
     * that such a object exists and if not, create one
     * 
     * @param player
     * @return
     */
    public static BlockBreakData getData(final NoCheatPlusPlayer player) {
        final DataStore base = player.getDataStore();
        BlockBreakData data = base.get(id);
        if (data == null) {
            data = new BlockBreakData();
            base.set(id, data);
        }
        return data;
    }

    public BlockBreakCheck(final NoCheatPlus plugin, final String name) {
        super(plugin, id, name);
    }
}
