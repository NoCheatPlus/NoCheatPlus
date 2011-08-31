package cc.co.evenprime.bukkit.nocheat.config.cache;

import java.util.logging.Logger;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * Configurations specific for logging. Every world gets one of these.
 * 
 * @author Evenprime
 * 
 */
public class CCLogging {

    public final LogLevel fileLevel;
    public final LogLevel consoleLevel;
    public final LogLevel chatLevel;
    public final Logger   filelogger;
    public final boolean  active;

    public CCLogging(Configuration data, Logger worldSpecificFileLogger) {

        active = data.getBoolean("logging.active");
        fileLevel = data.getLogLevel("logging.filelevel");
        consoleLevel = data.getLogLevel("logging.consolelevel");
        chatLevel = data.getLogLevel("logging.chatlevel");

        filelogger = worldSpecificFileLogger;
    }
}
