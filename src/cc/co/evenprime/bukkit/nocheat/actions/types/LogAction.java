package cc.co.evenprime.bukkit.nocheat.actions.types;

import cc.co.evenprime.bukkit.nocheat.data.LogData;
import cc.co.evenprime.bukkit.nocheat.log.Colors;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * Print a message to various locations
 * 
 */
public class LogAction extends ActionWithParameters {

    public final LogLevel level;

    public LogAction(String name, int delay, int repeat, LogLevel level, String message) {
        // Log messages may have color codes now
        super(name, delay, repeat, Colors.replaceColors(message));

        this.level = level;
    }

    public String getLogMessage(final LogData ldata) {
        return super.getMessage(ldata);
    }
}
