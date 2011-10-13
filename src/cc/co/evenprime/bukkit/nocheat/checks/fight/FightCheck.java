package cc.co.evenprime.bukkit.nocheat.checks.fight;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.FightData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

public class FightCheck {

    private final ActionExecutor action;
    private final NoCheat        plugin;

    public FightCheck(NoCheat plugin) {

        this.plugin = plugin;
        action = new ActionExecutor(plugin);
    }

    public boolean check(Player player, Entity damagee, FightData data, ConfigurationCache cc) {

        boolean cancel = false;

        boolean directionCheck = cc.fight.directionCheck && !player.hasPermission(Permissions.FIGHT_DIRECTION);

        if(data.directionLastViolationTime + 300 > System.currentTimeMillis()) {
            return true;
        }
        
        if(directionCheck) {

            Location eyes = player.getEyeLocation();

            final double x1 = ((double) damagee.getLocation().getX()) - eyes.getX() - 1;
            final double y1 = ((double) damagee.getLocation().getY()) - eyes.getY() - 0.5;
            final double z1 = ((double) damagee.getLocation().getZ()) - eyes.getZ() - 1;

            double factor = new Vector(x1 + 1, y1 + 1, z1 + 1).length();

            Vector direction = player.getEyeLocation().getDirection();
            final double x2 = x1 + 2;
            final double y2 = y1 + 2.5;
            final double z2 = z1 + 2;
            if(factor * direction.getX() >= x1 && factor * direction.getY() >= y1 && factor * direction.getZ() >= z1 && factor * direction.getX() <= x2 && factor * direction.getY() <= y2 && factor * direction.getZ() <= z2) {
                // Player did nothing wrong
                // reduce violation counter
                data.violationLevel *= 0.9D;
            } else {
                // Player failed the check
                // Increment violation counter
                data.violationLevel += 1;

                // Prepare some event-specific values for logging and custom
                // actions
                LogData ldata = plugin.getDataManager().getData(player).log;
                ldata.check = "fight.direction";

                cancel = action.executeActions(player, cc.fight.directionActions, (int) data.violationLevel, ldata, cc);
            }
        }

        return cancel;
    }
}
