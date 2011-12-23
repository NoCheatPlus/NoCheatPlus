package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * 
 */
public abstract class BlockPlaceCheck extends Check {

    private static final String id = "blockplace";

    public BlockPlaceCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, BlockPlaceData data, CCBlockPlace cc);

    public abstract boolean isEnabled(CCBlockPlace cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return getData(player.getDataStore()).history;
    }

    @Override
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {
        if(wildcard == ParameterName.PLACE_LOCATION) {
            SimpleLocation l = getData(player.getDataStore()).blockPlaced;
            if(l.isSet()) {
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            } else {
                return "null";
            }
        }

        else if(wildcard == ParameterName.PLACE_AGAINST) {
            SimpleLocation l = getData(player.getDataStore()).blockPlacedAgainst;
            if(l.isSet()) {
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            } else {
                return "null";
            }
        }

        else
            return super.getParameter(wildcard, player);
    }

    public static BlockPlaceData getData(DataStore base) {
        BlockPlaceData data = base.get(id);
        if(data == null) {
            data = new BlockPlaceData();
            base.set(id, data);
        }
        return data;
    }

    public static CCBlockPlace getConfig(ConfigurationCacheStore cache) {
        CCBlockPlace config = cache.get(id);
        if(config == null) {
            config = new CCBlockPlace(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }
}
