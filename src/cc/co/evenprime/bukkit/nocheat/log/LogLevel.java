package cc.co.evenprime.bukkit.nocheat.log;

import java.util.logging.Level;

/**
 * Define the available log levels (low, med, high, off)
 * 
 * @author Evenprime
 * 
 */
public enum LogLevel {

    OFF("off", "never", Level.OFF), LOW("low", "all messages", Level.INFO), MED("med", "important messages", Level.WARNING), HIGH("high", "very important messages", Level.SEVERE);

    public final String  name;
    private final String description;
    public final Level   level;

    private LogLevel(String name, String description, Level level) {
        this.name = name;
        this.description = description;
        this.level = level;
    }

    public static LogLevel getLogLevelFromString(String s) {
        if(s == null)
            return OFF;
        if("off".equals(s))
            return OFF;
        else if("low".equals(s))
            return LOW;
        else if("med".equals(s))
            return MED;
        else if("high".equals(s))
            return HIGH;
        else
            return OFF;
    }

    public String toString() {
        return this.name() + ": " + description;
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
