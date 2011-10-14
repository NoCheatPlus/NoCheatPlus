package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * 
 * @author Evenprime
 * 
 */
public class OnLiquidCheck {

    private final NoCheat plugin;

    public OnLiquidCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(Player player, Block blockPlaced, Block blockPlacedAgainst, BlockPlaceData data, ConfigurationCache cc) {

        boolean cancel = false;

        if(blockPlaced == null || blockPlaced.isEmpty()) {
            // all ok
        } else if(blockPlacedAgainst != null && isSolid(blockPlacedAgainst)) {
            // all ok
        } else if(isSolid(blockPlaced.getRelative(BlockFace.DOWN)) || isSolid(blockPlaced.getRelative(BlockFace.WEST)) || isSolid(blockPlaced.getRelative(BlockFace.EAST)) || isSolid(blockPlaced.getRelative(BlockFace.NORTH)) || isSolid(blockPlaced.getRelative(BlockFace.SOUTH)) || isSolid(blockPlaced.getRelative(BlockFace.UP))) {
            // all ok
        } else {
            data.onliquidViolationLevel += 1;
            LogData ldata = plugin.getDataManager().getData(player).log;
            ldata.check = "blockplace.onliquid";
            ldata.placed = blockPlaced;
            ldata.placedAgainst = blockPlacedAgainst;

            cancel = plugin.getActionManager().executeActions(player, cc.blockplace.onliquidActions, (int) data.onliquidViolationLevel, ldata, data.history, cc);
        }

        data.onliquidViolationLevel *= 0.95D; // Reduce level over time

        return cancel;
    }

    private boolean isSolid(Block block) {
        Material m = block.getType();
        return !(m == Material.AIR) || (m == Material.WATER) || (m == Material.STATIONARY_WATER) || (m == Material.LAVA) || (m == Material.STATIONARY_LAVA);
    }
}
