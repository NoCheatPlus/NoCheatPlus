package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

/**
 * The main Check class for blockbreak event checking. It will decide which
 * checks need to be executed and in which order. It will also precalculate
 * some values that are needed by multiple checks.
 * 
 * @author Evenprime
 * 
 */
public class BlockBreakCheck {

    private final ReachCheck     reachCheck;
    private final DirectionCheck directionCheck;

    public BlockBreakCheck(NoCheat plugin) {

        this.reachCheck = new ReachCheck(plugin);
        this.directionCheck = new DirectionCheck(plugin);
    }

    public boolean check(final Player player, final Block brokenBlock, final ConfigurationCache cc) {

        boolean cancel = false;

        // Reach check only if not in creative mode!
        boolean reach = cc.blockbreak.reachCheck && !player.hasPermission(Permissions.BLOCKBREAK_REACH);
        boolean direction = cc.blockbreak.directionCheck && !player.hasPermission(Permissions.BLOCKBREAK_DIRECTION);

        if((reach || direction) && brokenBlock != null) {

            if(reach) {
                cancel = reachCheck.check(player, brokenBlock, cc);
            }

            if(!cancel && direction) {
                cancel = directionCheck.check(player, brokenBlock, cc);
            }
        }
        return cancel;
    }
}
