package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

public class NoswingCheck extends FightCheck {

    public NoswingCheck(NoCheat plugin) {
        super(plugin, "fight.noswing", Permissions.FIGHT_NOSWING);
    }

    public boolean check(NoCheatPlayer player, FightData data, FightConfig cc) {

        boolean cancel = false;

        // did he swing his arm before?
        if(data.armswung) {
            data.armswung = false;
            data.noswingVL *= 0.90D;
        } else {
            data.noswingVL += 1;
            incrementStatistics(player, Id.FI_NOSWING, 1);

            cancel = executeActions(player, cc.noswingActions, data.noswingVL);
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(FightConfig cc) {
        return cc.noswingCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).noswingVL);
        else
            return super.getParameter(wildcard, player);
    }
}
