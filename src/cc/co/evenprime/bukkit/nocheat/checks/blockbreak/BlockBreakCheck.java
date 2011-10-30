package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * The main Check class for blockbreak event checking. It will decide which
 * checks need to be executed and in which order. It will also precalculate
 * some values that are needed by multiple checks.
 * 
 */
public class BlockBreakCheck {

    private final ReachCheck     reachCheck;
    private final DirectionCheck directionCheck;
    private final NoswingCheck   noswingCheck;
    private final NoCheat        plugin;

    public BlockBreakCheck(NoCheat plugin) {

        this.plugin = plugin;
        this.reachCheck = new ReachCheck(plugin);
        this.directionCheck = new DirectionCheck(plugin);
        this.noswingCheck = new NoswingCheck(plugin);
    }

    public boolean check(final Player player, final Block brokenBlock, final ConfigurationCache cc) {

        boolean cancel = false;

        // Reach check only if not in creative mode!
        final boolean reach = cc.blockbreak.reachCheck && !player.hasPermission(Permissions.BLOCKBREAK_REACH);
        final boolean direction = cc.blockbreak.directionCheck && !player.hasPermission(Permissions.BLOCKBREAK_DIRECTION);
        final boolean noswing = cc.blockbreak.noswingCheck && !player.hasPermission(Permissions.BLOCKBREAK_NOSWING);

        if((noswing || reach || direction) && brokenBlock != null) {

            final BaseData data = plugin.getData(player.getName());

            if(noswing) {
                cancel = noswingCheck.check(player, data, cc);
            }
            if(!cancel && reach) {
                cancel = reachCheck.check(player, data, brokenBlock, cc);
            }

            if(!cancel && direction) {
                cancel = directionCheck.check(player, data, brokenBlock, cc);
            }
        }
        return cancel;
    }
}
