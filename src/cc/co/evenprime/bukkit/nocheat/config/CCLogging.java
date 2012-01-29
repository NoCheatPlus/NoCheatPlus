package cc.co.evenprime.bukkit.nocheat.config;


/**
 * Configurations specific for logging. Every world gets one of these.
 * 
 */
public class CCLogging {

    public final boolean active;
    public final boolean toFile;
    public final boolean toConsole;
    public final boolean toChat;
    public final String  prefix;

    public CCLogging(Configuration data) {

        active = data.getBoolean(Configuration.LOGGING_ACTIVE);
        prefix = data.getString(Configuration.LOGGING_PREFIX);
        toFile = data.getBoolean(Configuration.LOGGING_LOGTOFILE);
        toConsole = data.getBoolean(Configuration.LOGGING_LOGTOCONSOLE);
        toChat = data.getBoolean(Configuration.LOGGING_LOGTOINGAMECHAT);
    }
}
