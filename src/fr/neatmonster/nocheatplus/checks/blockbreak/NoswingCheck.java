package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * We require that the player moves his arm between blockbreaks, this is
 * what gets checked here.
 * 
 */
public class NoswingCheck extends BlockBreakCheck {

    public class NoswingCheckEvent extends BlockBreakEvent {

        public NoswingCheckEvent(final NoswingCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public NoswingCheck() {
        super("noswing");
    }

    @Override
    public boolean check(final fr.neatmonster.nocheatplus.players.NCPPlayer player, final Object... args) {
        final BlockBreakConfig cc = getConfig(player);
        final BlockBreakData data = getData(player);

        boolean cancel = false;

        // did he swing his arm before
        if (data.armswung) {
            // "consume" the flag
            data.armswung = false;
            // reward with lowering of the violation level
            data.noswingVL *= 0.90D;
        } else {
            // he failed, increase vl and statistics
            data.noswingVL += 1;
            incrementStatistics(player, Id.BB_NOSWING, 1);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.noswingActions, data.noswingVL);
        }

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final NoswingCheckEvent event = new NoswingCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).noswingVL));
        else
            return super.getParameter(wildcard, player);
    }
}
