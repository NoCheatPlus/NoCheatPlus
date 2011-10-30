package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.CheckUtil;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

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

    public boolean check(final Player player, final BaseData data, final ConfigurationCache cc) {

        boolean cancel = false;
        final SimpleLocation placedAgainstBlock = data.blockplace.blockPlacedAgainst;

        final double distance = CheckUtil.reachCheck(player, placedAgainstBlock.x + 0.5D, placedAgainstBlock.y + 0.5D, placedAgainstBlock.z + 0.5D, cc.blockplace.reachDistance);

        BlockPlaceData blockplace = data.blockplace;

        if(distance > 0D) {
            // Player failed the check

            // Increment violation counter
            blockplace.reachViolationLevel += distance;

            // Prepare some event-specific values for logging and custom actions
            data.log.check = "blockplace.reach";
            data.log.reachdistance = distance;

            cancel = plugin.execute(player, cc.blockplace.reachActions, (int) blockplace.reachViolationLevel, blockplace.history, cc);
        } else {
            blockplace.reachViolationLevel *= 0.9D;
        }

        return cancel;
    }

}
