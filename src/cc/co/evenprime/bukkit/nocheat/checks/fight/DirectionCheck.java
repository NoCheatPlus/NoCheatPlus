package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;

import net.minecraft.server.Entity;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.checks.FightCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCFight;
import cc.co.evenprime.bukkit.nocheat.data.FightData;

public class DirectionCheck extends FightCheck {

    public DirectionCheck(NoCheat plugin) {
        super(plugin, "fight.direction", Permissions.FIGHT_DIRECTION);
    }

    public boolean check(NoCheatPlayer player, FightData data, CCFight cc) {

        boolean cancel = false;

        final long time = System.currentTimeMillis();

        // Get the width of the damagee
        Entity entity = data.damagee;

        final float width = entity.length > entity.width ? entity.length : entity.width;

        // height = 2.0D as minecraft doesn't store the height of entities,
        // and that should be enough. Because entityLocations are always set
        // to center bottom of the hitbox, increase "y" location by 1/2
        // height to get the "center" of the hitbox
        final double off = CheckUtil.directionCheck(player, entity.locX, entity.locY + 1.0D, entity.locZ, width, 2.0D, cc.directionPrecision);

        if(off < 0.1D) {
            // Player did probably nothing wrong
            // reduce violation counter
            data.directionVL *= 0.80D;
        } else {
            // Player failed the check
            // Increment violation counter
            if(!plugin.skipCheck()) {
                data.directionVL += Math.sqrt(off);
            }

            cancel = executeActions(player, cc.directionActions.getActions(data.directionVL));

            if(cancel) {
                // Needed to calculate penalty times
                data.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(data.directionLastViolationTime + cc.directionPenaltyTime > time) {
            return true;
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCFight cc) {
        return cc.directionCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", (int)player.getData().fight.directionVL);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
