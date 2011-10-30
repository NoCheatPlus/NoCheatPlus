package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * 
 */
public class OnLiquidCheck {

    private final NoCheat plugin;

    public OnLiquidCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(final Player player, final BaseData data, final ConfigurationCache cc) {

        boolean cancel = false;

        final BlockPlaceData blockplace = data.blockplace;
        final SimpleLocation blockplaced = blockplace.blockPlaced;
        final LogData log = data.log;

        if(isSolid(blockplace.placedType.getId())) {
            // all ok
        } else if(nextToSolid(player.getWorld(), blockplaced.x, blockplaced.y, blockplaced.z)) {
            // all ok
        } else {
            blockplace.onliquidViolationLevel += 1;
            log.check = "blockplace.onliquid";

            cancel = plugin.execute(player, cc.blockplace.onliquidActions, (int) blockplace.onliquidViolationLevel, blockplace.history, cc);
        }

        blockplace.onliquidViolationLevel *= 0.95D; // Reduce level over
                                                    // time

        return cancel;
    }

    private static final boolean nextToSolid(final World world, final int x, final int y, final int z) {
        return isSolid(world.getBlockTypeIdAt(x, y - 1, z)) || isSolid(world.getBlockTypeIdAt(x - 1, y, z)) || isSolid(world.getBlockTypeIdAt(x + 1, y, z)) || isSolid(world.getBlockTypeIdAt(x, y, z + 1)) || isSolid(world.getBlockTypeIdAt(x, y, z - 1)) || isSolid(world.getBlockTypeIdAt(x, y + 1, z));
    }

    private static final boolean isSolid(int id) {
        return !((id == Material.AIR.getId()) || (id == Material.WATER.getId()) || (id == Material.STATIONARY_WATER.getId()) || (id == Material.LAVA.getId()) || (id == Material.STATIONARY_LAVA.getId()));
    }
}
