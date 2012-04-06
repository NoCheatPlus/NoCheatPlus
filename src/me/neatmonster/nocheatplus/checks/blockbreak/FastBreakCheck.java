package me.neatmonster.nocheatplus.checks.blockbreak;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.data.Statistics.Id;

public class FastBreakCheck extends BlockBreakCheck {

    public FastBreakCheck(final NoCheatPlus plugin) {
        super(plugin, "blockbreak.fastbreak");
    }

    public boolean check(final NoCheatPlusPlayer player, final BlockBreakData data, final BlockBreakConfig cc) {

        boolean cancel = false;

        // Has the player broke blocks too quickly
        if (data.lastBreakTime != 0 && System.currentTimeMillis() - data.lastBreakTime < cc.fastBreakInterval) {
            // He failed, increase vl and statistics
            data.fastBreakVL += cc.fastBreakInterval - System.currentTimeMillis() + data.lastBreakTime;
            incrementStatistics(player, Id.BB_FASTBREAK, cc.fastBreakInterval - System.currentTimeMillis()
                    + data.lastBreakTime);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.fastBreakActions, data.fastBreakVL);
        } else
            // Reward with lowering of the violation level
            data.fastBreakVL *= 0.90D;

        data.lastBreakTime = System.currentTimeMillis();

        return cancel;

    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).fastBreakVL);
        else
            return super.getParameter(wildcard, player);
    }

}
