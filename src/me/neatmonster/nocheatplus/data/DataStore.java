package me.neatmonster.nocheatplus.data;

import java.util.HashMap;
import java.util.Map;

import me.neatmonster.nocheatplus.DataItem;

public class DataStore {

    private final Map<String, DataItem> dataMap    = new HashMap<String, DataItem>();
    private final Statistics            statistics = new Statistics();

    private final long                  timestamp  = System.currentTimeMillis();

    public Map<String, Object> collectData() {
        final Map<String, Object> map = statistics.get();
        map.put("nocheatplus.starttime", timestamp);
        map.put("nocheatplus.endtime", System.currentTimeMillis());

        return map;
    }

    @SuppressWarnings("unchecked")
    public <T extends DataItem> T get(final String id) {
        return (T) dataMap.get(id);
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void set(final String id, final DataItem data) {
        dataMap.put(id, data);
    }
}
