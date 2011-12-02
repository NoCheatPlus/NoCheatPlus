package cc.co.evenprime.bukkit.nocheat.data;

import java.util.Map;

/**
 * 
 * Every class that is extending this has to implement an empty Constructor()
 * 
 */
public abstract class Data {

    public void clearCriticalData() {

    }

    public abstract void collectData(Map<String, Object> map);
}
