package fr.neatmonster.nocheatplus.logging;

/**
 * Default StreamIDs.
 * @author dev1mc
 *
 */
public class Streams {

    // Maybe temporary: StreamID instances for default usage (custom registrations can not start with "default.").
    /**
     * Default prefix, should not be used with custom registration (LoggerID, StreamID).
     */
    public static final String defaultPrefix = "default.";
    
    // More or less raw default streams.
    
    /** For initialization and shutdown, not more than primary thread only is guaranteed here. Always available (fall-back). */
    public static final StreamID INIT = new StreamID(defaultPrefix + "init");
    /** Might not allow asynchronous access. */
    public static final StreamID SERVER_LOGGER = new StreamID(defaultPrefix + "logger.server");
    /** Might not allow asynchronous access. */
    public static final StreamID PLUGIN_LOGGER = new StreamID(defaultPrefix + "logger.plugin");
    public static final StreamID NOTIFY_INGAME = new StreamID(defaultPrefix + "chat.notify");
    public static final StreamID DEFAULT_FILE = new StreamID(defaultPrefix + "file");
    public static final StreamID TRACE_FILE = new StreamID(defaultPrefix + "file.trace");
    
    // TODO: More abstract streams (init, trace, violations?), have loggers/files be LoggerID ?

}
