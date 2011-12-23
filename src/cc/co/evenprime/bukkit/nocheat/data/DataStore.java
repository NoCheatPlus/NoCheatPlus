package cc.co.evenprime.bukkit.nocheat.data;

import java.util.HashMap;
import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.DataItem;

public class DataStore {

    private final Map<String, DataItem> dataMap   = new HashMap<String, DataItem>();

    private final long                  timestamp = System.currentTimeMillis();

    @SuppressWarnings("unchecked")
    public <T extends DataItem>T get(String id) {
        return (T) dataMap.get(id);
    }

    public void set(String id, DataItem data) {
        dataMap.put(id, data);
    }

    public void clearCriticalData() {
        for(DataItem data : dataMap.values()) {
            data.clearCriticalData();
        }
    }

    public void collectData(Map<String, Object> map) {
        for(DataItem data : dataMap.values()) {
            data.collectData(map);
        }

        map.put("nocheat.starttime", timestamp);
        map.put("nocheat.endtime", System.currentTimeMillis());
    }
}
