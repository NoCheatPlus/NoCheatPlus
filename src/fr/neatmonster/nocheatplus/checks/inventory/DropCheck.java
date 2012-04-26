package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.Locale;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The DropCheck will find out if a player drops too many items within a short
 * amount of time
 * 
 */
public class DropCheck extends InventoryCheck {

    public DropCheck() {
        super("drop");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final InventoryConfig cc = getConfig(player);
        final InventoryData data = getData(player);

        boolean cancel = false;

        final long time = System.currentTimeMillis();

        // Has the configured time passed? If so, reset the counter
        if (data.dropLastTime + cc.dropTimeFrame <= time) {
            data.dropLastTime = time;
            data.dropCount = 0;
            data.dropVL = 0;
        }
        // Security check, if the system time changes
        else if (data.dropLastTime > time)
            data.dropLastTime = Integer.MIN_VALUE;

        data.dropCount++;

        // The player dropped more than he should
        if (data.dropCount > cc.dropLimit) {
            // Set vl and increment statistics
            data.dropVL = data.dropCount - cc.dropLimit;
            incrementStatistics(player, Id.INV_DROP, 1);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.dropActions, data.dropVL);
        }

        return cancel;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", getData(player).dropVL);
        else
            return super.getParameter(wildcard, player);
    }
}
