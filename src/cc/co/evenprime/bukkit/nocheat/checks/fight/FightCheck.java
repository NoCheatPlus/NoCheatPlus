package cc.co.evenprime.bukkit.nocheat.checks.fight;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;

/**
 * Check various things related to fighting players/entities
 * 
 */
public abstract class FightCheck extends Check {

    private static final String id = "fight";

    public FightCheck(NoCheat plugin, String name, String permission) {
        super(plugin, id, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, FightData data, FightConfig cc);

    public abstract boolean isEnabled(FightConfig cc);

    public static FightData getData(DataStore base) {
        FightData data = base.get(id);
        if(data == null) {
            data = new FightData();
            base.set(id, data);
        }
        return data;
    }

    public static FightConfig getConfig(ConfigurationCacheStore cache) {
        FightConfig config = cache.get(id);
        if(config == null) {
            config = new FightConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }
}
