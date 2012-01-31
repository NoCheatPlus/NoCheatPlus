package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityComplex;
import net.minecraft.server.EntityComplexPart;
import net.minecraft.server.EntityGiantZombie;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class ReachCheck extends FightCheck {

    public ReachCheck(NoCheat plugin) {
        super(plugin, "fight.reach", Permissions.FIGHT_REACH);
    }

    public boolean check(NoCheatPlayer player, FightData data, FightConfig cc) {

        boolean cancel = false;

        final long time = System.currentTimeMillis();

        // Get the width of the damagee
        Entity entity = data.damagee;

        // Safeguard, if entity is Giant or Ender Dragon, this check will fail
        // due to giant and hard to define hitboxes
        if(entity instanceof EntityComplex || entity instanceof EntityComplexPart || entity instanceof EntityGiantZombie) {
            return false;
        }

        // height = 2.0D as minecraft doesn't store the height of entities,
        // and that should be enough. Because entityLocations are always set
        // to center bottom of the hitbox, increase "y" location by 1/2
        // height to get the "center" of the hitbox
        final double off = CheckUtil.reachCheck(player, entity.locX, entity.locY + 1.0D, entity.locZ, cc.reachLimit);

        if(off < 0.1D) {
            // Player did probably nothing wrong
            // reduce violation counter
            data.reachVL *= 0.80D;
        } else {
            // Player failed the check
            // Increment violation counter
            if(!plugin.skipCheck()) {
                double sqrt = Math.sqrt(off);
                data.reachVL += sqrt;
                data.reachTotalVL += sqrt;
                data.reachFailed++;
            }

            cancel = executeActions(player, cc.reachActions.getActions(data.reachVL));

            if(cancel) {
                // Needed to calculate penalty times
                data.reachLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(data.reachLastViolationTime + cc.reachPenaltyTime > time) {
            if(data.reachLastViolationTime > time) {
                System.out.println("Nocheat noted that your time ran backwards for " + (data.reachLastViolationTime - time) + " ms");
                // Security check for server time changed situations
                data.reachLastViolationTime = 0;
            }
            return true;
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(FightConfig cc) {
        return cc.reachCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).reachVL);
        else
            return super.getParameter(wildcard, player);
    }
}
