package cc.co.evenprime.bukkit.nocheat.debug;

import java.util.HashMap;
import java.util.Map;

public class PerformanceManager {

    public enum Type {
        BLOCKBREAK, BLOCKDAMAGE, BLOCKPLACE, CHAT, MOVING, VELOCITY, FIGHT
    }

    private static final long            NANO   = 1;
    private static final long            MICRO  = NANO * 1000;
    private static final long            MILLI  = MICRO * 1000;
    private static final long            SECOND = MILLI * 1000;
    private static final long            MINUTE = SECOND * 60;

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

    public String getAppropriateUnit(long timeInNanoseconds) {

        // more than 10 minutes
        if(timeInNanoseconds > MINUTE * 10) {
            return "minutes";
        }
        // more than 10 seconds
        else if(timeInNanoseconds > SECOND * 10) {
            return "seconds";
        }
        // more than 10 milliseconds
        else if(timeInNanoseconds > MILLI * 10) {
            return "milliseconds";
        }
        // more than 10 microseconds
        else if(timeInNanoseconds > MICRO * 10) {
            return "microseconds";
        } else {
            return "nanoseconds";
        }
    }

    public long convertToAppropriateUnit(long timeInNanoseconds) {
        // more than 10 minutes
        if(timeInNanoseconds > MINUTE * 10) {
            return timeInNanoseconds / MINUTE;
        }
        // more than 10 seconds
        else if(timeInNanoseconds > SECOND * 10) {
            return timeInNanoseconds / SECOND;
        }
        // more than 10 milliseconds
        else if(timeInNanoseconds > MILLI * 10) {
            return timeInNanoseconds / MILLI;
        }
        // more than 10 microseconds
        else if(timeInNanoseconds > MICRO * 10) {
            return timeInNanoseconds / MICRO;
        } else {
            return timeInNanoseconds / NANO;
        }
    }
}
