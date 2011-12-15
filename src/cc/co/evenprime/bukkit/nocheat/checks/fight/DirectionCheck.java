package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityComplex;
import net.minecraft.server.EntityComplexPart;
import net.minecraft.server.EntityGiantZombie;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ParameterName;
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

        // Safeguard, if entity is Giant or Ender Dragon, this check will fail
        // due to giant and hard to define hitboxes
        if(entity instanceof EntityComplex || entity instanceof EntityComplexPart || entity instanceof EntityGiantZombie) {
            return false;
        }

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
                double sqrt = Math.sqrt(off);
                data.directionVL += sqrt;
                data.directionTotalVL += sqrt;
                data.directionFailed++;
            }

            cancel = executeActions(player, cc.directionActions.getActions(data.directionVL));

            if(cancel) {
                // Needed to calculate penalty times
                data.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(data.directionLastViolationTime + cc.directionPenaltyTime > time) {
            if(data.directionLastViolationTime > time) {
                System.out.println("Nocheat noted that your time ran backwards for " + (data.directionLastViolationTime - time) + " ms");
                // Security check for server time changed situations
                data.directionLastViolationTime = 0;
            }
            return true;
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCFight cc) {
        return cc.directionCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) player.getData().fight.directionVL);
        else
            return super.getParameter(wildcard, player);
    }
}
