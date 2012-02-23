package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityComplex;
import net.minecraft.server.EntityComplexPart;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

public class DirectionCheck extends FightCheck {

    public DirectionCheck(NoCheat plugin) {
        super(plugin, "fight.direction", Permissions.FIGHT_DIRECTION);
    }

    public boolean check(NoCheatPlayer player, FightData data, FightConfig cc) {

        boolean cancel = false;

        final long time = System.currentTimeMillis();

        // Get the width of the damagee
        Entity entity = data.damagee;

        // Safeguard, if entity is complex, this check will fail
        // due to giant and hard to define hitboxes
        if(entity instanceof EntityComplex || entity instanceof EntityComplexPart) {
            return false;
        }

        final float width = entity.length > entity.width ? entity.length : entity.width;
        // entity.height is broken and will always be 0, therefore calculate height instead
        final double height = entity.boundingBox.e - entity.boundingBox.b;

        final double off = CheckUtil.directionCheck(player, entity.locX, entity.locY + (height / 2D), entity.locZ, width, height, cc.directionPrecision);

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
                incrementStatistics(player, Id.FI_DIRECTION, sqrt);
            }

            cancel = executeActions(player, cc.directionActions, data.directionVL);

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
    public boolean isEnabled(FightConfig cc) {
        return cc.directionCheck;
    }

    @Override
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).directionVL);
        else
            return super.getParameter(wildcard, player);
    }
}
