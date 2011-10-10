package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.data.LogData;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * Print a message to various locations
 * 
 * @author Evenprime
 * 
 */
public class LogAction extends ActionWithParameters {

    public final LogLevel level;
    
    public LogAction(String name, int delay, int repeat, LogLevel level, String message) {
        super(name, delay, repeat, message);

        this.level = level;
    }
    
    public String getMessage(LogData ldata) {
        return super.getMessage(ldata);
    }
}
