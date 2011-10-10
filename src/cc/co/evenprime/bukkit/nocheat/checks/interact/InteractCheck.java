package cc.co.evenprime.bukkit.nocheat.checks.interact;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.InteractData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * 
 * @author Evenprime
 * 
 */
public class InteractCheck {

    private final NoCheat        plugin;
    private final ActionExecutor action;

    public InteractCheck(NoCheat plugin) {
        this.plugin = plugin;
        action = new ActionExecutor(plugin);
    }

    public boolean check(Player player, InteractData data, ConfigurationCache cc) {

        boolean cancel = false;

        final boolean durability = cc.interact.durabilityCheck && !player.hasPermission(Permissions.INTERACT_DURABILITY);

        if(durability) {
            // It's so simple, I'll just do the check in place
            if(player.getInventory().getHeldItemSlot() == 9) {

                data.violationLevel += 1;

                LogData ldata = plugin.getDataManager().getLogData(player);
                ldata.check = "interact.durability";

                cancel = action.executeActions(player, cc.interact.durabilityActions, (int) data.violationLevel, ldata, cc);
            }

            data.violationLevel *= 0.95D; // Reduce level over time
        }

        return cancel;
    }

}
