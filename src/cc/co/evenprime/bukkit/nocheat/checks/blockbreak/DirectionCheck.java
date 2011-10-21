package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockBreak;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;

/**
 * The DirectionCheck will find out if a player tried to interact with something
 * that's not in his field of view.
 * 
 */
public class DirectionCheck {

    private final NoCheat plugin;

    public DirectionCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(final Player player, final BaseData data, final Block brokenBlock, final ConfigurationCache cc) {

        final BlockBreakData blockbreak = data.blockbreak;
        final CCBlockBreak ccblockbreak = cc.blockbreak;
        
        final boolean isInstaBreak = blockbreak.instaBrokeBlockLocation.equals(brokenBlock);
        
        // If the block is instabreak and we don't check instabreak, return
        if(isInstaBreak && !ccblockbreak.checkinstabreakblocks) {
            return false;
        }

        boolean cancel = false;

        double off = CheckUtil.directionCheck(player, brokenBlock.getX() + 0.5D, brokenBlock.getY() + 0.5D, brokenBlock.getZ() + 0.5D, 1D, 1D, ccblockbreak.directionPrecision);

        final long time = System.currentTimeMillis();

        if(off < 0.1D) {
            // Player did nothing wrong
            // reduce violation counter
            blockbreak.directionViolationLevel *= 0.9D;
        } else {
            // Player failed the check
            // Increment violation counter
            if(isInstaBreak) {
                // Instabreak block failures are very common, so don't be as hard on people failing them
                off /= 10;
            }
            blockbreak.directionViolationLevel += off;

            // Prepare some event-specific values for logging and custom actions
            data.log.check = "blockbreak.direction";

            cancel = plugin.execute(player, ccblockbreak.directionActions, (int) blockbreak.directionViolationLevel, blockbreak.history, cc);

            if(cancel) {
                // Needed to calculate penalty times
                blockbreak.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(blockbreak.directionLastViolationTime + ccblockbreak.directionPenaltyTime >= time) {
            return true;
        }

        return cancel;
    }
}
