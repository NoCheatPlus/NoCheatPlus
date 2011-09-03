package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;

/**
 * 
 * @author Evenprime
 *
 */
public class OnLiquidCheck {

    private final ActionExecutor action;

    public OnLiquidCheck(NoCheat plugin) {
        action = new ActionExecutorWithHistory(plugin);
    }
    
    public boolean check(Player player, Block blockPlaced, Block blockPlacedAgainst, BlockPlaceData data, ConfigurationCache cc) {

        boolean cancel = false;
        
        if(blockPlaced == null || blockPlaced.isLiquid() || blockPlaced.isEmpty()) {
            // all ok
        } else if(blockPlacedAgainst != null && (blockPlacedAgainst.isLiquid() || blockPlacedAgainst.isEmpty())) {

            data.onliquidViolationLevel += 1;
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.CHECK, "blockplace.onliquid");

            cancel = action.executeActions(player, cc.blockplace.onliquidActions, (int) data.onliquidViolationLevel, params, cc);
        }

        data.onliquidViolationLevel *= 0.95D; // Reduce level over time
        
        return cancel;
    }
}
