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
    private final Map<Player, MovingData>     movingData     = new HashMap<Player, MovingData>();
    private final Map<Player, BlockBreakData> blockbreakData = new HashMap<Player, BlockBreakData>();
    private final Map<Player, InteractData>   interactData   = new HashMap<Player, InteractData>();
    private final Map<Player, BlockPlaceData> blockPlaceData = new HashMap<Player, BlockPlaceData>();
    private final Map<Player, ChatData>       chatData       = new HashMap<Player, ChatData>();

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

    public InteractData getInteractData(Player player) {
        InteractData data;

        // intentionally not thread-safe, because bukkit events are handled
        // in sequence anyway, so zero chance of two events of the same
        // player being handled at the same time
        data = interactData.get(player);
        if(data == null) {
            data = new InteractData();
            interactData.put(player, data);
        }

        return data;
    }

    public BlockPlaceData getBlockPlaceData(Player player) {
        BlockPlaceData data;

        // intentionally not thread-safe, because bukkit events are handled
        // in sequence anyway, so zero chance of two events of the same
        // player being handled at the same time
        data = blockPlaceData.get(player);
        if(data == null) {
            data = new BlockPlaceData();
            blockPlaceData.put(player, data);
        }

        return data;
    }

    public ChatData getChatData(Player player) {
        ChatData data;

        // intentionally not thread-safe, because bukkit events are handled
        // in sequence anyway, so zero chance of two events of the same
        // player being handled at the same time
        // And if it still happens by accident, it's no real loss anyway
        data = chatData.get(player);
        if(data == null) {
            data = new ChatData();
            chatData.put(player, data);
        }

        return data;
    }
}
