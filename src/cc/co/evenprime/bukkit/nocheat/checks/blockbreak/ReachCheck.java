package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import org.bukkit.GameMode;
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

    public boolean check(Player player, Block brokenBlock, ConfigurationCache cc) {

        boolean cancel = false;

        double distance = CheckUtil.reachCheck(player, brokenBlock.getX() + 0.5D, brokenBlock.getY() + 0.5D, brokenBlock.getZ() + 0.5D, player.getGameMode() == GameMode.CREATIVE ? cc.blockbreak.reachDistance + 2 : cc.blockbreak.reachDistance);

        BaseData data = plugin.getPlayerData(player);
        
        if(distance > 0D) {
            // Player failed the check

            // Increment violation counter
            data.blockbreak.reachViolationLevel += distance;
            
            // Setup data for logging
            data.log.check = "blockbreak.reach";
            data.log.reachdistance = distance;

            cancel = plugin.getActionManager().executeActions(player, cc.blockbreak.reachActions, (int) data.blockbreak.reachViolationLevel, data.blockbreak.history, cc);
        } else {
            data.blockbreak.reachViolationLevel *= 0.9D;
        }

        return cancel;
    }

}
