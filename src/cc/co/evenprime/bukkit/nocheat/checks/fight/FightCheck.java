package cc.co.evenprime.bukkit.nocheat.checks.fight;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * Check various things related to fighting players/entities
 * 
 */
public class FightCheck {

    private final NoCheat plugin;

    public FightCheck(NoCheat plugin) {

        this.plugin = plugin;
    }

    public boolean check(Player player, Entity damagee, ConfigurationCache cc) {

        boolean cancel = false;

        boolean directionCheck = cc.fight.directionCheck && !player.hasPermission(Permissions.FIGHT_DIRECTION);

        long time = System.currentTimeMillis();

        if(directionCheck) {

            // Get the width of the damagee
            net.minecraft.server.Entity entity = ((CraftEntity) damagee).getHandle();

            float width = entity.length > entity.width ? entity.length : entity.width;

            // height = 2.0D as minecraft doesn't store the height of entities,
            // and that should be enough. Because entityLocations are always set
            // to center bottom of the hitbox, increase "y" location by 1/2
            // height to get the "center" of the hitbox
            double off = CheckUtil.directionCheck(player, entity.locX, entity.locY + 1.0D, entity.locZ, width, 2.0D, cc.fight.directionPrecision);

            BaseData data = plugin.getData(player);
            
            if(off < 0.1D) {
                // Player did probably nothing wrong
                // reduce violation counter
                data.fight.violationLevel *= 0.80D;
            } else {
                // Player failed the check
                // Increment violation counter
                if(!plugin.skipCheck()) {
                    data.fight.violationLevel += Math.sqrt(off);
                }

                // Prepare some event-specific values for logging and custom
                // actions
                data.log.check = "fight.direction";

                cancel = plugin.execute(player, cc.fight.directionActions, (int) data.fight.violationLevel, data.fight.history, cc);

                if(cancel) {
                    // Needed to calculate penalty times
                    data.fight.directionLastViolationTime = time;
                }
            }
            
            // If the player is still in penalty time, cancel the event anyway
            if(data.fight.directionLastViolationTime + cc.fight.directionPenaltyTime >= time) {
                return true;
            }
        }

        return cancel;
    }
}
