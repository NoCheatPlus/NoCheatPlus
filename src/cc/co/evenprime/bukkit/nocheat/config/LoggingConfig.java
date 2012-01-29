package cc.co.evenprime.bukkit.nocheat.config;

/**
 * Configurations specific for logging. Every world gets one of these.
 * 
 */
public class LoggingConfig {

    public final boolean active;
    public final boolean showactivechecks;
    public final boolean toFile;
    public final boolean toConsole;
    public final boolean toChat;
    public final String  prefix;

    public LoggingConfig(NoCheatConfiguration data) {

        active = data.getBoolean(ConfPaths.LOGGING_ACTIVE);
        showactivechecks = data.getBoolean(ConfPaths.LOGGING_SHOWACTIVECHECKS);
        prefix = data.getString(ConfPaths.LOGGING_PREFIX);
        toFile = data.getBoolean(ConfPaths.LOGGING_LOGTOFILE);
        toConsole = data.getBoolean(ConfPaths.LOGGING_LOGTOCONSOLE);
        toChat = data.getBoolean(ConfPaths.LOGGING_LOGTOINGAMECHAT);
    }
}
