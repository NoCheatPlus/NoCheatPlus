package cc.co.evenprime.bukkit.nocheat.actions.types;

import java.util.Map;
import java.util.Map.Entry;

import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * Print a message to various locations
 * 
 * @author Evenprime
 * 
 */
public class LogAction extends Action {

    // Default stuff
    public static final String PLAYER      = "\\[player\\]";
    public static final String LOCATION    = "\\[location\\]";
    public static final String WORLD       = "\\[world\\]";
    public static final String VIOLATIONS  = "\\[violations\\]";

    // Event dependent stuff
    public static final String DISTANCE    = "\\[distance\\]";
    public static final String LOCATION_TO = "\\[locationto\\]";
    public static final String CHECK       = "\\[check\\]";
    public static final String PACKETS     = "\\[packets\\]";
    public static final String TEXT        = "\\[text\\]";

    public final LogLevel      level;
    private final String       message;

    public LogAction(int delay, int repeat, LogLevel level, String message) {
        super(delay, repeat);

        this.level = level;
        this.message = message;
    }

    public String getLogMessage(Map<String, String> values) {
        String log = message;

        for(Entry<String, String> entry : values.entrySet()) {
            log = log.replaceAll(entry.getKey(), entry.getValue());
        }

        return log;
    }

}
