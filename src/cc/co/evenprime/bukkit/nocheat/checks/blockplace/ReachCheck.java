package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.HashMap;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;

/**
 * The reach check will find out if a player interacts with something that's too
 * far away
 * 
 * @author Evenprime
 * 
 */
public class ReachCheck {

    private final ActionExecutor action;

    public ReachCheck(NoCheat plugin) {
        this.action = new ActionExecutorWithHistory(plugin);
    }

    public boolean check(Player player, Block blockPlaced, Block placedAgainstBlock, BlockPlaceData data, ConfigurationCache cc) {

        boolean cancel = false;
        
        Location eyes = player.getEyeLocation();

        final double x1 = ((double) placedAgainstBlock.getX()) - eyes.getX();
        final double y1 = ((double) placedAgainstBlock.getY()) - eyes.getY();
        final double z1 = ((double) placedAgainstBlock.getZ()) - eyes.getZ();

        double distance = new Vector(x1 + 0.5, y1 + + 0.5, z1 + + 0.5).length();

        if(distance > cc.blockplace.reachDistance) {
            // Player failed the check

            // Increment violation counter
            data.reachViolationLevel += 1;

            // Prepare some event-specific values for logging and custom actions
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(LogAction.CHECK, "blockplace.reach");
            params.put(LogAction.DISTANCE, String.format(Locale.US, "%.2f", distance));
            cancel = action.executeActions(player, cc.blockplace.reachActions, (int) data.reachViolationLevel, params, cc);
        } else {
            data.reachViolationLevel *= 0.9D;
        }

        return cancel;
    }

}
