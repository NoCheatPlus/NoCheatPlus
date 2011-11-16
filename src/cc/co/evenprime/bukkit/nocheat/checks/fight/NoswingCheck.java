package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.FightCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCFight;
import cc.co.evenprime.bukkit.nocheat.data.FightData;

public class NoswingCheck extends FightCheck {

    public NoswingCheck(NoCheat plugin) {
        super(plugin, "fight.noswing", Permissions.FIGHT_NOSWING);
    }

    public boolean check(NoCheatPlayer player, FightData data, CCFight cc) {

        boolean cancel = false;
        
        // did he swing his arm before?
        if(data.armswung) {
            data.armswung = false;
            data.noswingVL *= 0.90D;
        } else {
            data.noswingVL += 1;

            cancel = executeActions(player, cc.noswingActions.getActions(data.noswingVL));
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCFight cc) {
        return cc.noswingCheck;
    }
    
    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", (int)player.getData().fight.noswingVL);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
