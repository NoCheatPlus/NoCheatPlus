package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;

/**
 * The reach check will find out if a player interacts with something that's too
 * far away
 * 
 */
public class ReachCheck {

    private final NoCheat plugin;

    public ReachCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(final Player player, final BaseData data, final Block brokenBlock, final ConfigurationCache cc) {

        boolean cancel = false;

        final BlockBreakData blockbreak = data.blockbreak;

        final double distance = CheckUtil.reachCheck(player, brokenBlock.getX() + 0.5D, brokenBlock.getY() + 0.5D, brokenBlock.getZ() + 0.5D, player.getGameMode() == GameMode.CREATIVE ? cc.blockbreak.reachDistance + 2 : cc.blockbreak.reachDistance);

        if(distance > 0D) {
            // Player failed the check

            // Increment violation counter
            blockbreak.reachViolationLevel += distance;

            // Setup data for logging
            data.log.check = "blockbreak.reach";
            data.log.reachdistance = distance;

            cancel = plugin.execute(player, cc.blockbreak.reachActions, (int) blockbreak.reachViolationLevel, blockbreak.history, cc);
        } else {
            blockbreak.reachViolationLevel *= 0.9D;
        }

        return cancel;
    }

}
