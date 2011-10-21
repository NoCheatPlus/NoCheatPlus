package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * The reach check will find out if a player interacts with something that's too
 * far away
 * 
 * @author Evenprime
 * 
 */
public class ReachCheck {

    private final NoCheat plugin;

    public ReachCheck(NoCheat plugin) {
        this.plugin = plugin;
    }

    public boolean check(Player player, Block blockPlaced, Block placedAgainstBlock, ConfigurationCache cc) {

        boolean cancel = false;

        double distance = CheckUtil.reachCheck(player, placedAgainstBlock.getX() + 0.5D, placedAgainstBlock.getY() + 0.5D, placedAgainstBlock.getZ() + 0.5D, cc.blockplace.reachDistance);

        BaseData data = plugin.getData(player.getName());

        if(distance > 0D) {
            // Player failed the check

            // Increment violation counter
            data.blockplace.reachViolationLevel += distance;

            // Prepare some event-specific values for logging and custom actions
            data.log.check = "blockplace.reach";
            data.log.reachdistance = distance;

            cancel = plugin.execute(player, cc.blockplace.reachActions, (int) data.blockplace.reachViolationLevel, data.blockplace.history, cc);
        } else {
            data.blockplace.reachViolationLevel *= 0.9D;
        }

        return cancel;
    }

}
