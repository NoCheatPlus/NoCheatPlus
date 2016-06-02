/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.logging;

/**
 * Default StreamIDs. Before the server tick loop is running, it's recommended
 * only to log to the INIT stream, because tasks won't be processed before all
 * plugins have been loaded.
 * <hr>
 * Default LoggerIDs have the same names and are atached to the stream of the
 * same name. Additionally there might be default loggers attached to several
 * streams with different name and options (e.g. DEFAULT_FILE.name + ".init"
 * ensuring for direct asynchronous logging on the INIT stream).
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
    
    // Abstract default streams ---
    
    /**
     * Default stream for initialization and shutdown, should always be
     * available (fall-back). Loggers must not use tasks, not more than primary
     * thread only is guaranteed here, might drop messages sent from other
     * threads. By default the file logger gets attached, to ensure that direct
     * logging from other threads arrives somewhere.
     */
    public static final StreamID INIT = new StreamID(defaultPrefix + "init");
    
    /**
     * Stream for status and error messages similar to INIT, but using queues
     * and scheduling. Potentially more efficient, for less severe cases.
     * Usually logging both to console and log file.
     */
    public static final StreamID STATUS = new StreamID(defaultPrefix + "status");
    
    // TODO: Consider TRACE.
    
    // Raw default streams ---
    
    /**
     * Default stream for the server logger (think of console). Using log4j this
     * should run in an asynchronous task, configurable.
     */
    public static final StreamID SERVER_LOGGER = new StreamID(defaultPrefix + "logger.server");
    
    /**
     * Default stream for the plugin logger (Usually the server logger using a
     * plugin-dependent prefix.). Using log4j this should run in an asynchronous
     * task, configurable.
     */
    public static final StreamID PLUGIN_LOGGER = new StreamID(defaultPrefix + "logger.plugin");
    
    /**
     * Default ingame notifications (game chat), using a task in the primary
     * thread. (The implementation is not thread-safe, even if message sending
     * was thread-safe. Thus sending is tied to the primary thread.)
     */
    public static final StreamID NOTIFY_INGAME = new StreamID(defaultPrefix + "chat.notify");
    
    /**
     * Default file (defaults to nocheatplus.log), always using an asynchronous task.
     */
    public static final StreamID DEFAULT_FILE = new StreamID(defaultPrefix + "file");
    
    /**
     * Trace file (might lead to the default file), always using an asynchronous task.
     */
    public static final StreamID TRACE_FILE = new StreamID(defaultPrefix + "file.trace");
    
}
