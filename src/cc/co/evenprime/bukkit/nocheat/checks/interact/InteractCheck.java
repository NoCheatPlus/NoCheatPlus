package cc.co.evenprime.bukkit.nocheat.checks.interact;

import java.util.HashMap;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.InteractData;

/**
 * 
 * @author Evenprime
 * 
 */
public class InteractCheck {

    private final ActionExecutor action;

    public InteractCheck(NoCheat plugin) {
        action = new ActionExecutorWithHistory(plugin);
    }

    public boolean check(Player player, InteractData data, ConfigurationCache cc) {

        boolean cancel = false;

        final boolean durability = cc.interact.durabilityCheck && !player.hasPermission(Permissions.INTERACT_DURABILITY);

        if(durability) {
            // It's so simple, I'll just do the check in place
            if(player.getInventory().getHeldItemSlot() == 9) {

                data.violationLevel += 1;
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(LogAction.CHECK, "interact.durability");

                cancel = action.executeActions(player, cc.interact.durabilityActions, (int) data.violationLevel, params, cc);
            }

            data.violationLevel *= 0.95D; // Reduce level over time
        }

        return cancel;
    }

}
