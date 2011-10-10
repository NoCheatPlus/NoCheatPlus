package cc.co.evenprime.bukkit.nocheat.log;

import java.util.logging.Level;

/**
 * Define the available log levels (low, med, high, off)
 * 
 * @author Evenprime
 * 
 */
public enum LogLevel {

    OFF("off", Level.OFF), LOW("low", Level.INFO), MED("med", Level.WARNING), HIGH("high", Level.SEVERE);

    public final String  name;
    public final Level   level;

    private LogLevel(String name, Level level) {
        this.name = name;
        this.level = level;
    }

    public static LogLevel getLogLevelFromString(String s) {
        if("off".equals(s))
            return OFF;
        else if("low".equals(s))
            return LOW;
        else if("med".equals(s))
            return MED;
        else if("high".equals(s))
            return HIGH;
        else
            throw new IllegalArgumentException("Unknown log level "+s);
    }

    public String toString() {
        return this.name;
    }

    /**
     * Is this level smaller or equal to "level"
     * 
     * @param level
     */
    public boolean matches(LogLevel level) {
        if(this == OFF || level == OFF) {
            return false;
        } else {
            return this.level.intValue() <= level.level.intValue();
        }
    }
}
