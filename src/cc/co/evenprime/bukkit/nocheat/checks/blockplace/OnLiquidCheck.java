package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

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

    public boolean check(Player player, Block blockPlaced, Block blockPlacedAgainst, ConfigurationCache cc) {

        boolean cancel = false;

        BaseData data = plugin.getPlayerData(player);

        if(blockPlaced == null || blockPlaced.isEmpty() || (blockPlacedAgainst != null && isSolid(blockPlacedAgainst.getTypeId()))) {
            // all ok
        } else if(nextToSolid(blockPlaced.getWorld(), blockPlaced.getX(), blockPlaced.getY(), blockPlaced.getZ())) {
            // all ok
        } else {
            data.blockplace.onliquidViolationLevel += 1;
            data.log.check = "blockplace.onliquid";
            data.log.placed = blockPlaced;
            data.log.placedAgainst = blockPlacedAgainst;

            cancel = plugin.getActionManager().executeActions(player, cc.blockplace.onliquidActions, (int) data.blockplace.onliquidViolationLevel, data.blockplace.history, cc);
        }

        data.blockplace.onliquidViolationLevel *= 0.95D; // Reduce level over
                                                         // time

        return cancel;
    }

    private boolean nextToSolid(World world, int x, int y, int z) {
        return isSolid(world.getBlockTypeIdAt(x, y - 1, z)) || isSolid(world.getBlockTypeIdAt(x - 1, y, z)) || isSolid(world.getBlockTypeIdAt(x + 1, y, z)) || isSolid(world.getBlockTypeIdAt(x, y, z + 1)) || isSolid(world.getBlockTypeIdAt(x, y, z - 1)) || isSolid(world.getBlockTypeIdAt(x, y + 1, z));
    }

    private boolean isSolid(int id) {
        return !((id == Material.AIR.getId()) || (id == Material.WATER.getId()) || (id == Material.STATIONARY_WATER.getId()) || (id == Material.LAVA.getId()) || (id == Material.STATIONARY_LAVA.getId()));
    }
}
