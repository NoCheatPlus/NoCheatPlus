package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The DropCheck will find out if a player drops too many items within a short
 * amount of time
 * 
 */
public class DropCheck extends InventoryCheck {

    public class DropCheckEvent extends InventoryEvent {

        public DropCheckEvent(final DropCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
            super(check, player, actions, vL);
        }
    }

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
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final DropCheckEvent event = new DropCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).dropVL));
        else
            return super.getParameter(wildcard, player);
    }
}
