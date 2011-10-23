package cc.co.evenprime.bukkit.nocheat.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provide secure access to player-specific data objects for various checks or
 * check groups.
 */
public class DataManager {

    // Store data between Events
    private final Map<String, BaseData> map;
    private final List<String>          removals;

    public DataManager() {
        this.map = new HashMap<String, BaseData>();
        this.removals = new ArrayList<String>(5);
    }

    /**
     * Get a data object of the specified class. If none is stored yet, create
     * one.
     */
    public BaseData getData(String playerName) {

        BaseData data = this.map.get(playerName);

        // intentionally not thread-safe, because bukkit events are handled
        // in sequence anyway, so zero chance of two events of the same
        // player being handled at the same time
        // And if it still happens by accident, it's no real loss anyway, as
        // losing data of one instance doesn't really hurt at all
        if(data == null) {
            data = new BaseData();
            data.log.playerName = playerName;
            this.map.put(playerName, data);
        }

        data.lastUsedTime = System.currentTimeMillis();

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
     * check if some data hasn't been used for a while and remove it
     * 
     */
    public void cleanDataMap() {
        synchronized(removals) {
            long time = System.currentTimeMillis();
            try {
                for(Entry<String, BaseData> p : this.map.entrySet()) {
                    if(p.getValue().shouldBeRemoved(time)) {
                        removals.add(p.getKey());
                    }
                }

                for(String p : removals) {
                    this.map.remove(p);
                }

                removals.clear();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearCriticalData(String playerName) {
        BaseData data = this.map.get(playerName);
        if(data != null) {
            data.clearCriticalData();
        }
    }

}
