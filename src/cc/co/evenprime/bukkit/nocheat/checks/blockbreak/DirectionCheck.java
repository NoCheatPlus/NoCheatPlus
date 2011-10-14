package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;
import cc.co.evenprime.bukkit.nocheat.data.LogData;

/**
 * The DirectionCheck will find out if a player tried to interact with something
 * that's not in his field of view.
 * 
 * @author Evenprime
 * 
 */
public class DirectionCheck {

    private final NoCheat plugin;

    public DirectionCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(Player player, double factor, double x1, double y1, double z1, Block brokenBlock, BlockBreakData data, ConfigurationCache cc) {

        // If the block is instabreak and we don't check instabreak, return
        if(!cc.blockbreak.checkinstabreakblocks && brokenBlock.getLocation().equals(data.instaBrokeBlockLocation)) {
            return false;
        }

        boolean cancel = false;

        Vector direction = player.getEyeLocation().getDirection();
        final double x2 = x1 + 2;
        final double y2 = y1 + 2;
        final double z2 = z1 + 2;
        if(factor * direction.getX() >= x1 && factor * direction.getY() >= y1 && factor * direction.getZ() >= z1 && factor * direction.getX() <= x2 && factor * direction.getY() <= y2 && factor * direction.getZ() <= z2) {
            // Player did nothing wrong
            // reduce violation counter
            data.directionViolationLevel *= 0.9D;
        } else {
            // Player failed the check
            // Increment violation counter
            data.directionViolationLevel += 1;

            // Prepare some event-specific values for logging and custom actions
            LogData ldata = plugin.getDataManager().getData(player).log;
            ldata.check = "blockbreak.direction";

            cancel = plugin.getActionManager().executeActions(player, cc.blockbreak.directionActions, (int) data.directionViolationLevel, ldata, data.history, cc);
        }

        return cancel;
    }

}
