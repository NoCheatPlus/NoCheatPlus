package cc.co.evenprime.bukkit.nocheat.debug;

import java.util.HashMap;
import java.util.Map;

public class PerformanceManager {

    public enum EventType {
        BLOCKBREAK, BLOCKDAMAGE, BLOCKPLACE, CHAT, MOVING, VELOCITY, FIGHT, TIMED
    }

    private final Map<EventType, Performance> map;

    public PerformanceManager() {

        map = new HashMap<EventType, Performance>();

        for(EventType type : EventType.values()) {
            map.put(type, new Performance(true));
        }
    }

    public Performance get(EventType type) {
        return map.get(type);
    }
}
