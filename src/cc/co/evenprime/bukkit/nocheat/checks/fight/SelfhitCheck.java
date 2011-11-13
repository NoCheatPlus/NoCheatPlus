package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.FightCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCFight;
import cc.co.evenprime.bukkit.nocheat.data.FightData;

public class SelfhitCheck extends FightCheck {

    public SelfhitCheck(NoCheat plugin) {
        super(plugin, "fight.selfhit", Permissions.FIGHT_SELFHIT);
    }

    public boolean check(NoCheatPlayer player, FightData data, CCFight cc) {

        boolean cancel = false;

        if(player.getPlayer().equals(data.damagee.getBukkitEntity())) {
            // Player failed the check obviously

            data.selfhitVL += 1;
            cancel = executeActions(player, cc.selfhitActions.getActions(data.selfhitVL));
        } else {
            data.selfhitVL *= 0.99D;
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCFight cc) {
        return cc.selfhitCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", (int)player.getData().fight.selfhitVL);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
