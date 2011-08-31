package cc.co.evenprime.bukkit.nocheat.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

/**
 * Provide secure access to player-specific data objects for various checks or
 * check groups
 * 
 * @author Evenprime
 * 
 */
public class DataManager {

    // Store data between Events
    private final Map<Player, MovingData>   movingData   = new HashMap<Player, MovingData>();
    private final Map<Player, BlockBreakData> blockbreakData = new HashMap<Player, BlockBreakData>();

    public DataManager() {

    }

    public MovingData getMovingData(Player player) {

        MovingData data;

        // intentionally not thread-safe, because bukkit events are handled
        // in sequence anyway, so zero chance of two move events of the same
        // player being handled at the same time
        data = movingData.get(player);
        if(data == null) {
            data = new MovingData();
            movingData.put(player, data);
        }

        return data;
    }

    public BlockBreakData getBlockBreakData(Player player) {

        BlockBreakData data;

        // intentionally not thread-safe, because bukkit events are handled
        // in sequence anyway, so zero chance of two move events of the same
        // player being handled at the same time
        data = blockbreakData.get(player);
        if(data == null) {
            data = new BlockBreakData();
            blockbreakData.put(player, data);
        }

        return data;
    }
}
