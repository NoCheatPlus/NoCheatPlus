package cc.co.evenprime.bukkit.nocheat.checks.fight;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.FightData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * Check various things related to fighting players/entities
 * 
 */
public class FightCheck {

    private final NoCheat plugin;

    public FightCheck(NoCheat plugin) {

        this.plugin = plugin;
    }

    public boolean check(Player player, Entity damagee, FightData data, ConfigurationCache cc) {

        boolean cancel = false;

        boolean directionCheck = cc.fight.directionCheck && !player.hasPermission(Permissions.FIGHT_DIRECTION);

        long time = System.currentTimeMillis();

        if(directionCheck) {

            Location eyes = player.getEyeLocation();

            // Get the width of the damagee
            net.minecraft.server.Entity entity = ((CraftEntity) damagee).getHandle();

            float width = entity.length > entity.width ? entity.length : entity.width;
            double height = 2.0D; // Minecraft server doesn't store the height
                                  // of entities :(

            final double p = width / 2 + cc.fight.directionPrecision;
            final double h = height / 2 + cc.fight.directionPrecision;

            // TODO: Move this into a seperate class to recycle it throughout
            // NoCheat
            final double x1 = ((double) damagee.getLocation().getX()) - eyes.getX() - p;
            final double y1 = ((double) damagee.getLocation().getY()) - eyes.getY() - cc.fight.directionPrecision;
            final double z1 = ((double) damagee.getLocation().getZ()) - eyes.getZ() - p;

            double factor = new Vector(x1 + p, y1 + h, z1 + p).length();

            Vector direction = player.getEyeLocation().getDirection();

            final double x2 = x1 + 2 * p;
            final double y2 = y1 + 2 * h;
            final double z2 = z1 + 2 * p;

            final double xPrediction = factor * direction.getX();
            final double yPrediction = factor * direction.getY();
            final double zPrediction = factor * direction.getZ();
            
            double off = 0.0D;
            
            off += Math.max(x1 - xPrediction, 0.0D);
            off += Math.max(y1 - yPrediction, 0.0D);
            off += Math.max(z1 - zPrediction, 0.0D);
            off += Math.max(xPrediction - x2, 0.0D);
            off += Math.max(yPrediction - y2, 0.0D);
            off += Math.max(zPrediction - z2, 0.0D);
            
            if(off < 0.1D) {
                // Player did probably nothing wrong
                // reduce violation counter
                data.violationLevel *= 0.80D;
            } else {
                // Player failed the check
                // Increment violation counter
                if(!plugin.skipCheck()) {
                    data.violationLevel += Math.sqrt(off);
                }

                // Prepare some event-specific values for logging and custom
                // actions
                LogData ldata = plugin.getDataManager().getData(player).log;
                ldata.check = "fight.direction";

                cancel = plugin.getActionManager().executeActions(player, cc.fight.directionActions, (int) data.violationLevel, ldata, data.history, cc);

                if(cancel) {
                    // Needed to calculate penalty times
                    data.directionLastViolationTime = time;
                }
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(data.directionLastViolationTime + cc.fight.directionPenaltyTime >= time) {
            return true;
        }

        return cancel;
    }
}
