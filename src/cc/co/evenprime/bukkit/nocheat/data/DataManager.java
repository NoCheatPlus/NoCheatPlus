package cc.co.evenprime.bukkit.nocheat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
     * Reset data that may cause problems after e.g. changing the config
     * 
     */
    public void clearCriticalData() {
        for(BaseData b : this.map.values()) {
            b.clearCriticalData();
        }
    }

    /**
     * put a players data on the queue for later deletion (unless it comes back
     * before)
     * 
     */
    public void queueForRemoval(Player player) {
        BaseData bd = this.map.get(player);

        if(bd != null) {
            bd.markForRemoval(true);
        }
    }

    public void unqueueForRemoval(Player player) {
        BaseData bd = this.map.get(player);

        if(bd != null) {
            bd.markForRemoval(false);
        }
    }

    /**
     * check if queued for removal data is ready to get removed
     * 
     */
    public void cleanDataMap() {
        try {
            List<Player> removals = new ArrayList<Player>();

            for(Entry<Player, BaseData> p : this.map.entrySet()) {
                if(p.getValue().shouldBeRemoved()) {
                    removals.add(p.getKey());
                }
            }

            for(Player p : removals) {
                this.map.remove(p);
            }
        } catch(Exception e) {
            // Ignore problems, as they really don't matter much
        }
    }

    public void clearCriticalData(Player player) {
        BaseData data = this.map.get(player);
        data.clearCriticalData();
    }

}
