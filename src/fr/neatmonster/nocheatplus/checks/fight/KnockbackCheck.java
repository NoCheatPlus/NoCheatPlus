package fr.neatmonster.nocheatplus.checks.fight;

import java.util.Locale;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * A check used to verify if players aren't knockbacking other players when it's not technically possible
 * 
 */
public class KnockbackCheck extends FightCheck {

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
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).knockbackVL);
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.knockbackCheck;
    }
}
