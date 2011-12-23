package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;

public abstract class BlockBreakCheck extends Check {

    private static final String id = "blockbreak";

    public BlockBreakCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, BlockBreakData data, CCBlockBreak cc);

    public abstract boolean isEnabled(CCBlockBreak cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return getData(player.getDataStore()).history;
    }

    public static BlockBreakData getData(DataStore base) {
        BlockBreakData data = base.get(id);
        if(data == null) {
            data = new BlockBreakData();
            base.set(id, data);
        }
        return data;
    }

    public static CCBlockBreak getConfig(ConfigurationCacheStore cache) {
        CCBlockBreak config = cache.get(id);
        if(config == null) {
            config = new CCBlockBreak(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }
}
