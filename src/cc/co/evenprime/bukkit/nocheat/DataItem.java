package cc.co.evenprime.bukkit.nocheat;

import java.util.Map;

/**
 * 
 * Every class that is extending this has to implement an empty Constructor()
 * 
 */
public interface DataItem {

    public void clearCriticalData();

    public abstract void collectData(Map<String, Object> map);
}
