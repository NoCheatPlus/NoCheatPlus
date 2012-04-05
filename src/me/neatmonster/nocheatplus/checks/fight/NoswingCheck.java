package me.neatmonster.nocheatplus.checks.fight;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.config.Permissions;
import me.neatmonster.nocheatplus.data.Statistics.Id;

/**
 * We require that the player moves his arm between attacks, this is
 * what gets checked here.
 * 
 */
public class NoswingCheck extends FightCheck {

    public NoswingCheck(final NoCheatPlus plugin) {
        super(plugin, "fight.noswing", Permissions.FIGHT_NOSWING);
    }

    @Override
    public boolean check(final NoCheatPlusPlayer player, final FightData data, final FightConfig cc) {

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
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).noswingVL);
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.noswingCheck;
    }
}
