package me.neatmonster.nocheatplus.checks.blockplace;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.checks.Check;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;
import me.neatmonster.nocheatplus.data.SimpleLocation;

/**
 * Abstract base class for BlockPlace checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class BlockPlaceCheck extends Check {

    private static final String id = "blockplace";

    public static BlockPlaceConfig getConfig(final ConfigurationCacheStore cache) {
        BlockPlaceConfig config = cache.get(id);
        if (config == null) {
            config = new BlockPlaceConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }

    /**
     * Get the BlockPlaceConfig object that belongs to the world that the player
     * currently resides in.
     * 
     * @param player
     * @return
     */
    public static BlockPlaceConfig getConfig(final NoCheatPlusPlayer player) {
        return getConfig(player.getConfigurationStore());
    }

    /**
     * Get the "BlockPlaceData" object that belongs to the player. Will ensure
     * that such a object exists and if not, create one
     * 
     * @param player
     * @return
     */
    public static BlockPlaceData getData(final NoCheatPlusPlayer player) {
        final DataStore base = player.getDataStore();
        BlockPlaceData data = base.get(id);
        if (data == null) {
            data = new BlockPlaceData();
            base.set(id, data);
        }
        return data;
    }

    public BlockPlaceCheck(final NoCheatPlus plugin, final String name) {
        super(plugin, id, name);
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {
        if (wildcard == ParameterName.PLACE_LOCATION) {
            final SimpleLocation l = getData(player).blockPlaced;
            if (l.isSet())
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            else
                return "null";
        }

        else if (wildcard == ParameterName.PLACE_AGAINST) {
            final SimpleLocation l = getData(player).blockPlacedAgainst;
            if (l.isSet())
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            else
                return "null";
        }

        else
            return super.getParameter(wildcard, player);
    }
}
