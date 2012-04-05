package me.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.checks.Check;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;
import me.neatmonster.nocheatplus.data.PreciseLocation;

/**
 * Abstract base class for Moving checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class MovingCheck extends Check {

    private static final String id = "moving";

    public static MovingConfig getConfig(final ConfigurationCacheStore cache) {
        MovingConfig config = cache.get(id);
        if (config == null) {
            config = new MovingConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }

    /**
     * Get the MovingConfig object that belongs to the world that the player
     * currently resides in.
     * 
     * @param player
     * @return
     */
    public static MovingConfig getConfig(final NoCheatPlusPlayer player) {
        return getConfig(player.getConfigurationStore());
    }

    /**
     * Get the "MovingData" object that belongs to the player. Will ensure
     * that such a object exists and if not, create one
     * 
     * @param player
     * @return
     */
    public static MovingData getData(final NoCheatPlusPlayer player) {
        final DataStore base = player.getDataStore();
        MovingData data = base.get(id);
        if (data == null) {
            data = new MovingData();
            base.set(id, data);
        }
        return data;
    }

    public MovingCheck(final NoCheatPlus plugin, final String name) {
        super(plugin, id, name);
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.LOCATION) {
            final PreciseLocation from = getData(player).from;
            return String.format(Locale.US, "%.2f,%.2f,%.2f", from.x, from.y, from.z);
        } else if (wildcard == ParameterName.MOVEDISTANCE) {
            final PreciseLocation from = getData(player).from;
            final PreciseLocation to = getData(player).to;
            return String.format(Locale.US, "%.2f,%.2f,%.2f", to.x - from.x, to.y - from.y, to.z - from.z);
        } else if (wildcard == ParameterName.LOCATION_TO) {
            final PreciseLocation to = getData(player).to;
            return String.format(Locale.US, "%.2f,%.2f,%.2f", to.x, to.y, to.z);
        } else
            return super.getParameter(wildcard, player);

    }
}
