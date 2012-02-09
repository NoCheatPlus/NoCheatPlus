package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

public class NoswingCheck extends BlockBreakCheck {

    public NoswingCheck(NoCheat plugin) {
        super(plugin, "blockbreak.noswing", Permissions.BLOCKBREAK_NOSWING);
    }

    public boolean check(NoCheatPlayer player, BlockBreakData data, BlockBreakConfig cc) {

        boolean cancel = false;

        // did he swing his arm before?
        if(data.armswung) {
            data.armswung = false;
            data.noswingVL *= 0.90D;
        } else {
            data.noswingVL += 1;
            incrementStatistics(player, Id.BB_NOSWING, 1);

            cancel = executeActions(player, cc.noswingActions.getActions(data.noswingVL));
        }

        return cancel;
    }

    public boolean isEnabled(BlockBreakConfig cc) {
        return cc.noswingCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).noswingVL);
        else
            return super.getParameter(wildcard, player);
    }

}
