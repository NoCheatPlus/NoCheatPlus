package cc.co.evenprime.bukkit.nocheat.debug;

import java.util.HashMap;
import java.util.Map;

public class PerformanceManager {

    public enum Type {
        BLOCKBREAK, BLOCKDAMAGE, BLOCKPLACE, CHAT, MOVING, VELOCITY, FIGHT, TIMED
    }



    private final Map<Type, Performance> map;

    public PerformanceManager() {

        map = new HashMap<Type, Performance>();

        for(Type type : Type.values()) {
            map.put(type, new Performance(true));
        }
    }

    public Performance get(Type type) {
        return map.get(type);
    }
}
