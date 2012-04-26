package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

/**
 * Abstract base class for Moving checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class MovingCheck extends Check {

    public MovingCheck(final String name) {
        super("moving." + name, MovingConfig.class, MovingData.class);
    }

    public MovingConfig getConfig(final NCPPlayer player) {
        return (MovingConfig) player.getConfig(this);
    }

    public MovingData getData(final NCPPlayer player) {
        return (MovingData) player.getData(this);
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

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
