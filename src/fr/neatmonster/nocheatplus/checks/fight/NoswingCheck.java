package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * We require that the player moves his arm between attacks, this is
 * what gets checked here.
 * 
 */
public class NoswingCheck extends FightCheck {

    public class NoswingCheckEvent extends FightEvent {

        public NoswingCheckEvent(final NoswingCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public NoswingCheck() {
        super("noswing", Permissions.FIGHT_NOSWING);
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final FightConfig cc = getConfig(player);
        final FightData data = getData(player);

        boolean cancel = false;

        // did he swing his arm before?
        if (data.armswung) {
            // Yes, reward him with reduction of his vl
            data.armswung = false;
            data.noswingVL *= 0.90D;
        } else {
            // No, increase vl and statistics
            data.noswingVL += 1;
            incrementStatistics(player, Id.FI_NOSWING, 1);

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

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.noswingCheck;
    }
}
