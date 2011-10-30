package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * 
 */
public class BlockPlaceCheck {

    private final ReachCheck    reachCheck;
    private final OnLiquidCheck onLiquidCheck;
    private final NoswingCheck  noswingCheck;
    private final NoCheat       plugin;

    public BlockPlaceCheck(NoCheat plugin) {

        this.plugin = plugin;

        reachCheck = new ReachCheck(plugin);
        onLiquidCheck = new OnLiquidCheck(plugin);
        noswingCheck = new NoswingCheck(plugin);
    }

    public boolean check(final Player player, final Block blockPlaced, final Block blockPlacedAgainst, final ConfigurationCache cc) {

        boolean cancel = false;

        // Which checks are going to be executed?
        final boolean onliquid = cc.blockplace.onliquidCheck && !player.hasPermission(Permissions.BLOCKPLACE_ONLIQUID);
        final boolean reach = cc.blockplace.reachCheck && !player.hasPermission(Permissions.BLOCKPLACE_REACH);
        final boolean noswing = cc.blockplace.noswingCheck && !player.hasPermission(Permissions.BLOCKPLACE_NOSWING);


        final BaseData data = plugin.getData(player.getName());

        if(noswing) {
            cancel = noswingCheck.check(player, data, cc);
        }
        if(!cancel && reach) {
            cancel = reachCheck.check(player, data, blockPlacedAgainst, cc);
        }

        if(!cancel && onliquid) {
            cancel = onLiquidCheck.check(player, data, blockPlaced, blockPlacedAgainst, cc);
        }

        return cancel;
    }
}
