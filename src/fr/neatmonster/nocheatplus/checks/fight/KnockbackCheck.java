package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * A check used to verify if players aren't knockbacking other players when it's not technically possible
 * 
 */
public class KnockbackCheck extends FightCheck {

    public class KnockbackCheckEvent extends FightEvent {

        public KnockbackCheckEvent(final KnockbackCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public KnockbackCheck() {
        super("knockback", Permissions.FIGHT_KNOCKBACK);
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final FightConfig cc = getConfig(player);
        final FightData data = getData(player);

        boolean cancel = false;

        // Check how long ago has the player started sprinting
        if (data.sprint > 0L && System.currentTimeMillis() - data.sprint < cc.knockbackInterval) {

            // Player failed the check, but this is influenced by lag,
            // so don't do it if there was lag
            if (!NoCheatPlus.skipCheck()) {
                // The violation level if the difference between the regular and the elapsed time
                final long delta = cc.knockbackInterval - System.currentTimeMillis() + data.sprint;
                // Increment the violation level
                data.knockbackVL += delta;
                // Increment the statisctics of the player
                incrementStatistics(player, Id.FI_KNOCKBACK, delta);
            }

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.knockbackActions, data.knockbackVL);
        }

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final KnockbackCheckEvent event = new KnockbackCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).knockbackVL));
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.knockbackCheck;
    }
}
