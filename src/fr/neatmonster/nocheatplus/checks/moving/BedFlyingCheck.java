package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * A check preventing players from flying by sending bed leaving packets
 */
public class BedFlyingCheck extends MovingCheck {

    public class BedFlyingCheckEvent extends MovingEvent {

        public BedFlyingCheckEvent(final BedFlyingCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public BedFlyingCheck() {
        super("bedflying");
    }

    public boolean check(final NCPPlayer player, final Object... args) {
        final MovingData data = getData(player);

        // If the player wasn't sleeping but is only sending packets, he is cheating!
        if (!data.wasSleeping) {
            final MovingConfig cc = getConfig(player);

            // Increment violation counter
            data.bedFlyVL++;

            // Increment player's statistics
            incrementStatistics(player, Id.MOV_BEDFLYING, 1);

            // Execute the actions
            return executeActions(player, cc.bedFlyActions, data.bedFlyVL);
        }

        // Otherwise reward the player for his legit bed leave
        else
            data.bedFlyVL = Math.max(0D, data.bedFlyVL - 1);

        return false;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final BedFlyingCheckEvent event = new BedFlyingCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).bedFlyVL));
        else
            return super.getParameter(wildcard, player);
    }
}
