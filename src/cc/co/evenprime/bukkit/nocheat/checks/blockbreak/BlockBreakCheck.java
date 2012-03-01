package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;

/**
 * Abstract base class for BlockBreakChecks. Provides some static convenience
 * methods for retrieving data and config objects for players
 *
 */
public abstract class BlockBreakCheck extends Check {

    private static final String id = "blockbreak";

    public BlockBreakCheck(NoCheat plugin, String name) {
        super(plugin, id, name);
    }

    /**
     * Get the "BlockBreakData" object that belongs to the player. Will ensure
     * that such a object exists and if not, create one
     * 
     * @param player
     * @return
     */
    public static BlockBreakData getData(NoCheatPlayer player) {
        DataStore base = player.getDataStore();
        BlockBreakData data = base.get(id);
        if(data == null) {
            data = new BlockBreakData();
            base.set(id, data);
        }
        return data;
    }

    /**
     * Get the BlockBreakConfig object that belongs to the world that the player
     * currently resides in.
     * 
     * @param player
     * @return
     */
    public static BlockBreakConfig getConfig(NoCheatPlayer player) {
        return getConfig(player.getConfigurationStore());
    }

    public static BlockBreakConfig getConfig(ConfigurationCacheStore cache) {
        BlockBreakConfig config = cache.get(id);
        if(config == null) {
            config = new BlockBreakConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }
}
