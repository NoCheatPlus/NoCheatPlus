package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.Locale;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;

/**
 * Abstract base class for BlockPlace checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class BlockPlaceCheck extends Check {

    public BlockPlaceCheck(final String name) {
        super("blockplace." + name, BlockPlaceConfig.class, BlockPlaceData.class);
    }

    public abstract boolean check(final NCPPlayer player, final Object... args);

    public BlockPlaceConfig getConfig(final NCPPlayer player) {
        return (BlockPlaceConfig) player.getConfig(this);
    }

    public BlockPlaceData getData(final NCPPlayer player) {
        return (BlockPlaceData) player.getData(this);
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {
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
