package cc.co.evenprime.bukkit.nocheat.config.cache;

import java.util.logging.Logger;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.log.Colors;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * Configurations specific for logging. Every world gets one of these.
 * 
 */
public class CCLogging {

    public final LogLevel fileLevel;
    public final LogLevel consoleLevel;
    public final LogLevel chatLevel;
    public final Logger   filelogger;
    public final boolean  active;
    public final String   prefix;

    public CCLogging(Configuration data, Logger worldSpecificFileLogger) {

        active = data.getBoolean(Configuration.LOGGING_ACTIVE);
        prefix = Colors.replaceColors(data.getString(Configuration.LOGGING_PREFIX));
        fileLevel = data.getLogLevel(Configuration.LOGGING_FILELEVEL);
        consoleLevel = data.getLogLevel(Configuration.LOGGING_CONSOLELEVEL);
        chatLevel = data.getLogLevel(Configuration.LOGGING_CHATLEVEL);

        filelogger = worldSpecificFileLogger;
    }
}
