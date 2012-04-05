package me.neatmonster.nocheatplus.checks.blockbreak;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.data.Statistics.Id;

/**
 * We require that the player moves his arm between blockbreaks, this is
 * what gets checked here.
 * 
 */
public class NoswingCheck extends BlockBreakCheck {

    public NoswingCheck(final NoCheatPlus plugin) {
        super(plugin, "blockbreak.noswing");
    }

    public boolean check(final NoCheatPlusPlayer player, final BlockBreakData data, final BlockBreakConfig cc) {

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
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).noswingVL);
        else
            return super.getParameter(wildcard, player);
    }

}
