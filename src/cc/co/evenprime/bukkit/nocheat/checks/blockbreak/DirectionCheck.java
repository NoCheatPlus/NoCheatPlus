package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

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

    public boolean check(Player player, Block brokenBlock, ConfigurationCache cc) {

        BaseData data = plugin.getData(player.getName());

        // If the block is instabreak and we don't check instabreak, return
        if(!cc.blockbreak.checkinstabreakblocks && data.blockbreak.instaBrokeBlockLocation.equals(brokenBlock)) {
            return false;
        }

        boolean cancel = false;

        double off = CheckUtil.directionCheck(player, brokenBlock.getX() + 0.5D, brokenBlock.getY() + 0.5D, brokenBlock.getZ() + 0.5D, 1D, 1D, cc.blockbreak.directionPrecision);

        long time = System.currentTimeMillis();

        if(off < 0.1D) {
            // Player did nothing wrong
            // reduce violation counter
            data.blockbreak.directionViolationLevel *= 0.9D;
        } else {
            // Player failed the check
            // Increment violation counter
            data.blockbreak.directionViolationLevel += off;

            // Prepare some event-specific values for logging and custom actions
            data.log.check = "blockbreak.direction";

            cancel = plugin.execute(player, cc.blockbreak.directionActions, (int) data.blockbreak.directionViolationLevel, data.blockbreak.history, cc);

            if(cancel) {
                // Needed to calculate penalty times
                data.blockbreak.directionLastViolationTime = time;
            }
        }

        // If the player is still in penalty time, cancel the event anyway
        if(data.blockbreak.directionLastViolationTime + cc.blockbreak.directionPenaltyTime >= time) {
            return true;
        }

        return cancel;
    }
}
