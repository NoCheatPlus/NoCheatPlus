package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

        if(blockPlaced == null) {
            // all ok
        } else if(blockPlacedAgainst != null && isSolid(blockPlacedAgainst)) {
            // all ok
        } else if(isSolid(blockPlaced.getRelative(BlockFace.DOWN)) || isSolid(blockPlaced.getRelative(BlockFace.WEST)) || isSolid(blockPlaced.getRelative(BlockFace.EAST)) || isSolid(blockPlaced.getRelative(BlockFace.NORTH)) || isSolid(blockPlaced.getRelative(BlockFace.SOUTH)) || isSolid(blockPlaced.getRelative(BlockFace.UP))) {
            // all ok
        } else {
            data.onliquidViolationLevel += 1;
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.CHECK, "blockplace.onliquid");

            cancel = action.executeActions(player, cc.blockplace.onliquidActions, (int) data.onliquidViolationLevel, params, cc);
        }

        data.onliquidViolationLevel *= 0.95D; // Reduce level over time

        return cancel;
    }

    public boolean isSolid(Block block) {
        Material m = block.getType();
        return !(m == Material.AIR) || (m == Material.WATER) || (m == Material.STATIONARY_WATER) || (m == Material.LAVA) || (m == Material.STATIONARY_LAVA);
    }
}
