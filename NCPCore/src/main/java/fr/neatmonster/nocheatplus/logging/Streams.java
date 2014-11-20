package fr.neatmonster.nocheatplus.logging;

/**
 * Default StreamIDs. Before the server is running, it's recommended only to log
 * to the INIT stream, because tasks won't be processed before all plugins have
 * been loaded.
 * <hr>
 * Default LoggerIDs have the same names and are atached to the stream of the
 * same name. Additionally there might be default loggers attached to several
 * streams with different name and options (e.g. DEFAULT_FILE.name + ".init"
 * allowing for direct asynchronous logging on the INIT stream).
 * <hr>
 * All names are processed case-insensitive within LogManager.
 * <hr>
 * 
 * @author dev1mc
 *
 */
public class Streams {

    /**
     * Default prefix, should not be used with custom registration (LoggerID, StreamID).
     */
    public static final String defaultPrefix = "default.";
    
    // More or less raw default streams.
    
    /**
     * Default stream for initialization and shutdown, should always be
     * available (fall-back). Loggers must not use tasks, not more than primary
     * thread only is guaranteed here, might drop messages sent from other
     * threads. By default the file logger gets attached, to allow direct
     * logging from other threads.
     */
    public static final StreamID INIT = new StreamID(defaultPrefix + "init");
    
    /**
     * Default stream for the server logger (think of console). Might not allow
     * asynchronous access, thus using a task in the primary thread by default.
     */
    public static final StreamID SERVER_LOGGER = new StreamID(defaultPrefix + "logger.server");
    
    /**
     * Default stream for the plugin logger (Usually the server logger using a
     * plugin-dependent prefix.). Might not allow asynchronous access, thus
     * using a task in the primary thread by default.
     */
    public static final StreamID PLUGIN_LOGGER = new StreamID(defaultPrefix + "logger.plugin");
    
    /**
     * Default ingame notifications (game chat), using a task in the primary
     * thread. (The implementation is not thread-safe, even if message sending
     * was thread-safe.)
     */
    public static final StreamID NOTIFY_INGAME = new StreamID(defaultPrefix + "chat.notify");
    
    /**
     * Default file (defaults to nocheatplus.log), thread-safe.
     */
    public static final StreamID DEFAULT_FILE = new StreamID(defaultPrefix + "file");
    
    /**
     * Trace file (might lead to the default file), thread-safe.
     */
    public static final StreamID TRACE_FILE = new StreamID(defaultPrefix + "file.trace");
    
    // TODO: Abstract streams: INIT, STATUS, ACTIONS, TRACE. STATUS being similar to INIT, just allowing to use tasks.

}
