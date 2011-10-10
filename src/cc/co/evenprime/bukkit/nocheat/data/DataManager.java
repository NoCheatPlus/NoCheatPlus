package cc.co.evenprime.bukkit.nocheat.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

/**
 * Provide secure access to player-specific data objects for various checks or
 * check groups.
 */
public class DataManager {

    // Store data between Events
    private final Map<Player, BaseData> map;

    public DataManager() {
        this.map = new HashMap<Player, BaseData>();
    }

    /**
     * Get a data object of the specified class. If none is stored yet, create
     * one.
     */
    public BaseData getData(Player player) {

        BaseData data = this.map.get(player);

        // intentionally not thread-safe, because bukkit events are handled
        // in sequence anyway, so zero chance of two events of the same
        // player being handled at the same time
        // And if it still happens by accident, it's no real loss anyway, as
        // losing data of one instance doesn't really hurt at all
        if(data == null) {
            data = new BaseData();
            data.initialize(player);
            this.map.put(player, data);
        }

        return data;
    }

    /**
     * Remove all data Objects of a specific player
     * 
     * @param player
     */
    public void removeDataForPlayer(Player player) {
        this.map.remove(player);
    }

    /**
     * Reset data that may cause problems after e.g. changing the config
     * 
     */
    public void resetAllCriticalData() {
        for(BaseData b : this.map.values()) {
            b.clearCriticalData();
        }
    }
}
