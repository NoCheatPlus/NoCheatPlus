package me.neatmonster.nocheatplus.checks.fight;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.checks.Check;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;

/**
 * Abstract base class for Fight checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class FightCheck extends Check {

    private static final String id = "fight";

    public static FightConfig getConfig(final ConfigurationCacheStore cache) {
        FightConfig config = cache.get(id);
        if (config == null) {
            config = new FightConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }

    /**
     * Get the FightConfig object that belongs to the world that the player
     * currently resides in.
     * 
     * @param player
     * @return
     */
    public static FightConfig getConfig(final NoCheatPlusPlayer player) {
        return getConfig(player.getConfigurationStore());
    }

    /**
     * Get the "FightData" object that belongs to the player. Will ensure
     * that such a object exists and if not, create one
     * 
     * @param player
     * @return
     */
    public static FightData getData(final NoCheatPlusPlayer player) {
        final DataStore base = player.getDataStore();
        FightData data = base.get(id);
        if (data == null) {
            data = new FightData();
            base.set(id, data);
        }
        return data;
    }

    public final String permission;

    public FightCheck(final NoCheatPlus plugin, final String name, final String permission) {
        super(plugin, id, name);
        this.permission = permission;
    }

    public abstract boolean check(NoCheatPlusPlayer player, FightData data, FightConfig cc);

    public abstract boolean isEnabled(FightConfig cc);
}
