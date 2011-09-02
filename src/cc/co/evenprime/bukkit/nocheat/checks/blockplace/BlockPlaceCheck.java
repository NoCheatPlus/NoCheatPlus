package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;

/**
 * 
 * @author Evenprime
 * 
 */
public class BlockPlaceCheck {

    private final ReachCheck reachCheck;
    private final OnLiquidCheck onLiquidCheck;

    public BlockPlaceCheck(NoCheat plugin) {
        
        reachCheck = new ReachCheck(plugin);
        onLiquidCheck = new OnLiquidCheck(plugin);
    }

    public boolean check(Player player, Block blockPlaced, Block blockPlacedAgainst, BlockPlaceData data, ConfigurationCache cc) {

        boolean cancel = false;

        // Which checks are going to be executed?
        final boolean onliquid = cc.blockplace.onliquidCheck && !player.hasPermission(Permissions.BLOCKPLACE_ONLIQUID);
        final boolean reach = cc.blockplace.reachCheck && !player.hasPermission(Permissions.BLOCKPLACE_REACH);

        if(!cancel && reach) {
            cancel = reachCheck.check(player, blockPlaced, blockPlacedAgainst, data, cc);
        }
        
        if(!cancel && onliquid) {
            cancel = onLiquidCheck.check(player, blockPlaced, blockPlacedAgainst, data, cc);
        }

        return cancel;
    }
}
