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
package fr.neatmonster.nocheatplus.logging.details;

import java.io.File;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.LoggerID;
import fr.neatmonster.nocheatplus.logging.StreamID;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Central access point for logging. Abstract class providing basic registration functionality.
 * @author dev1mc
 *
 */
public abstract class AbstractLogManager implements LogManager {

    // TODO: Visibility of methods.
    // TODO: Add option for per stream prefixes.
    // TODO: Concept for adding in the time at the point of call/scheduling.

    // TODO: Re-register with other options: Add methods for LoggerID + StreamID + options.
    // TODO: Hierarchical LogNode relations, to ensure other log nodes with the same logger are removed too [necessary to allow removing individual loggers].

    // TODO: Temporary streams, e.g. for players, unregistering with command and/or logout.
    // TODO: Mechanics of removing temporary streams (flush stream, remove entries from queues, wait with removal until tasks have run once more).

    // TODO: Consider generalizing the (internal) implementation right away (sub registry by content class).
    // TODO: Consider adding a global cache (good for re-mapping, contra: reload is not intended to happen on a regular basis).

    private final LogNodeDispatcher dispatcher;
    private final String defaultPrefix;

    /**
     * Fast streamID access map (runtime). Copy on write with registryLock.
     */
    private Map<StreamID, ContentStream<String>> idStreamMap = new IdentityHashMap<StreamID, ContentStream<String>>();

    /** 
     * Map name to Stream. Copy on write with registryLock.
     */
    private Map<String, ContentStream<String>> nameStreamMap = new HashMap<String, ContentStream<String>>();

    /**
     * Lower-case name to StreamID.
     */
    private Map<String, StreamID> nameStreamIDMap = new HashMap<String, StreamID>();

    /**
     * LogNode registry by LoggerID. Copy on write with registryLock.
     */
    private Map<LoggerID, LogNode<String>> idNodeMap = new IdentityHashMap<LoggerID, LogNode<String>>();

    /**
     * LogNode registry by lower-case name. Copy on write with registryLock.
     */
    private Map<String, LogNode<String>> nameNodeMap = new HashMap<String, LogNode<String>>();

    /**
     * Lower-case name to LoggerID.
     */
    private Map<String, LoggerID> nameLoggerIDMap = new HashMap<String, LoggerID>();

    /** Registry changes have to be done under this lock (copy on write) */
    protected final Object registryCOWLock = new Object();
    // TODO: Future: Only an init string stream or (later) always "the init stream" for all content types.
    private final StreamID initStreamID;
    private final StreamID voidStreamID = new StreamID("void");
    /**
     * Fall-back StreamID, for the case of logging to a non-existing StreamID.
     * By default set to void, but can be altered. Set to null to have calls
     * fail.
     */
    private StreamID fallBackStreamID = voidStreamID;

    /**
     * Wrapping logging to the init stream.
     */
    protected final ContentLogger<String> initLogger = new ContentLogger<String>() {
        @Override
        public void log(final Level level, final String content) {
            AbstractLogManager.this.log(getInitStreamID(), level, content);
        }
    };

    /**
     * 
     * @param dispatcher
     * @param defaultPrefix
     * @param initStreamID This id is stored, the stream is created, but no loggers will be attached to it within this constructor.
     */
    public AbstractLogManager(LogNodeDispatcher dispatcher, String defaultPrefix, StreamID initStreamID) {
        this.dispatcher = dispatcher;
        this.defaultPrefix = defaultPrefix;
        this.initStreamID = initStreamID;
        createInitStream();
        registerInitLogger();
        dispatcher.setInitLogger(initLogger);
    }

    /**
     * Create INIT stream for strings, if it does not exist. Does not set a
     * prefix.
     */
    protected void createInitStream() {
        synchronized (registryCOWLock) {
            if (!hasStream(initStreamID)) {
                createStringStream(initStreamID);
            }
        }
    }

    /**
     * Create the minimal init logger(s). Synchronize over registryCOWLock. It's preferable not to duplicate loggers. Prefer LoggerID("init...").
     */
    protected abstract void registerInitLogger();

    protected LogNodeDispatcher getLogNodeDispatcher() {
        return dispatcher;
    }

    @Override
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    @Override
    public StreamID getInitStreamID() {
        return initStreamID;
    }

    @Override
    public StreamID getVoidStreamID() {
        return voidStreamID;
    }

    @Override
    public StreamID getStreamID(String name) {
        return nameStreamIDMap.get(name.toLowerCase());
    }

    @Override
    public LoggerID getLoggerID(String name) {
        return nameLoggerIDMap.get(name.toLowerCase());
    }

    @Override
    public void debug(final StreamID streamID, final String message) {
        log(streamID, Level.FINE, message); // TODO: Not sure what happens with FINE and provided Logger instances.
    }

    @Override
    public void info(final StreamID streamID, final String message) {
        log(streamID, Level.INFO, message);
    }

    @Override
    public void warning(final StreamID streamID, final String message) {
        log(streamID, Level.WARNING, message);
    }

    @Override
    public void severe(final StreamID streamID, final String message) {
        log(streamID, Level.SEVERE, message);
    }

    @Override
    public void log(final StreamID streamID, final Level level, final String message) {
        if (streamID != voidStreamID) {
            final ContentStream<String> stream = idStreamMap.get(streamID);
            if (stream != null) {
                stream.log(level, message);
            } else {
                handleFallBack(streamID, level, message);
            }
        }
    }

    private void handleFallBack(final StreamID streamID, final Level level, final String message) {
        if (fallBackStreamID != null && streamID != fallBackStreamID) {
            log(fallBackStreamID, level, message);
        } else {
            throw new RuntimeException("Stream not registered: " + streamID);
        }
    }

    @Override
    public void debug(final StreamID streamID, final Throwable t) {
        log(streamID, Level.FINE, t); // TODO: Not sure what happens with FINE and provided Logger instances.
    }

    @Override
    public void info(final StreamID streamID, final Throwable t) {
        log(streamID, Level.INFO, t);
    }

    @Override
    public void warning(final StreamID streamID, final Throwable t) {
        log(streamID, Level.WARNING, t);
    }

    @Override
    public void severe(final StreamID streamID, final Throwable t) {
        log(streamID, Level.SEVERE, t);
    }

    @Override
    public void log(final StreamID streamID, final Level level, final Throwable t) {
        // Not sure adding streams for Throwable would be better.
        log(streamID, level, StringUtil.throwableToString(t));
    }

    @Override
    public boolean hasStream(final StreamID streamID) {
        return this.idStreamMap.containsKey(streamID) || this.nameStreamMap.containsKey(streamID.name.toLowerCase());
    }

    @Override
    public boolean hasStream(String name) {
        return getStreamID(name) != null;
    }

    /**
     * Call under lock.
     * @param streamID
     */
    private void testRegisterStream(final StreamID streamID) {
        if (streamID == null) {
            throw new NullPointerException("StreamID must not be null.");
        }
        else if (streamID.name == null) {
            throw new NullPointerException("StreamID.name must not be null.");
        }
        else if (streamID.name.equalsIgnoreCase(voidStreamID.name)) {
            throw new RuntimeException("Can not overrite void StreamID.");
        }
        else if (hasStream(streamID)) {
            throw new IllegalArgumentException("Stream already registered: " + streamID.name.toLowerCase());
        }
    }

    protected ContentStream<String> createStringStream(final StreamID streamID) {
        ContentStream<String> stream;
        synchronized (registryCOWLock) {
            testRegisterStream(streamID);
            Map<StreamID, ContentStream<String>> idStreamMap = new IdentityHashMap<StreamID, ContentStream<String>>(this.idStreamMap);
            Map<String, ContentStream<String>> nameStreamMap = new HashMap<String, ContentStream<String>>(this.nameStreamMap);
            Map<String, StreamID> nameStreamIDMap = new HashMap<String, StreamID>(this.nameStreamIDMap);
            stream = new DefaultContentStream<String>(dispatcher);
            idStreamMap.put(streamID, stream);
            nameStreamMap.put(streamID.name.toLowerCase(), stream);
            nameStreamIDMap.put(streamID.name.toLowerCase(), streamID);
            this.idStreamMap = idStreamMap;
            this.nameStreamMap = nameStreamMap;
            this.nameStreamIDMap = nameStreamIDMap;

        }
        return stream;
    }

    @Override
    public boolean hasLogger(final LoggerID loggerID) {
        return this.idNodeMap.containsKey(loggerID) || this.nameNodeMap.containsKey(loggerID.name.toLowerCase());
    }

    @Override
    public boolean hasLogger(String name) {
        return getLoggerID(name) != null;
    }

    /**
     * Call under lock.
     * @param loggerID
     */
    private void testRegisterLogger(final LoggerID loggerID) {
        if (loggerID == null) {
            throw new NullPointerException("LoggerID must not be null.");
        }
        else if (loggerID.name == null) {
            throw new NullPointerException("LoggerID.name must not be null.");
        }
        else if (hasLogger(loggerID)) {
            throw new IllegalArgumentException("Logger already registered: " + loggerID.name.toLowerCase());
        }
    }

    /**
     * Convenience method.
     * @param logger
     * @param options
     * @return
     */
    protected LoggerID registerStringLogger(final ContentLogger<String> logger, final LogOptions options) {
        LoggerID loggerID = new LoggerID(options.name);
        registerStringLogger(loggerID, logger, options);
        return loggerID;
    }

    /**
     * Convenience method.
     * @param logger
     * @param options
     * @return
     */
    protected LoggerID registerStringLogger(final Logger logger, final LogOptions options) {
        LoggerID loggerID = new LoggerID(options.name);
        registerStringLogger(loggerID, logger, options);
        return loggerID;
    }

    /**
     * Convenience method.
     * @param logger
     * @param options
     * @return
     */
    protected LoggerID registerStringLogger(final File file, final LogOptions options) {
        return registerStringLogger(file, null, options);
    }

    /**
     * Convenience method.
     * @param logger
     * @param prefix Prefix for all log messages.
     * @param options
     * @return
     */
    protected LoggerID registerStringLogger(final File file, final String prefix, final LogOptions options) {
        LoggerID loggerID = new LoggerID(options.name);
        registerStringLogger(loggerID, file, prefix, options);
        return loggerID;
    }

    protected LogNode<String> registerStringLogger(final LoggerID loggerID, final ContentLogger<String> logger, final LogOptions options) {
        LogNode<String> node;
        synchronized (registryCOWLock) {
            testRegisterLogger(loggerID);
            Map<LoggerID, LogNode<String>> idNodeMap = new IdentityHashMap<LoggerID, LogNode<String>>(this.idNodeMap);
            Map<String, LogNode<String>> nameNodeMap = new HashMap<String, LogNode<String>>(this.nameNodeMap);
            Map<String, LoggerID> nameLoggerIDMap = new HashMap<String, LoggerID>(this.nameLoggerIDMap);
            node = new LogNode<String>(loggerID, logger, options);
            idNodeMap.put(loggerID, node);
            nameNodeMap.put(loggerID.name.toLowerCase(), node);
            nameLoggerIDMap.put(loggerID.name.toLowerCase(), loggerID);
            this.idNodeMap = idNodeMap;
            this.nameNodeMap = nameNodeMap;
            this.nameLoggerIDMap = nameLoggerIDMap;
        }
        return node;
    }

    protected LogNode<String> registerStringLogger(LoggerID loggerID, Logger logger, LogOptions options) {
        LogNode<String> node;
        synchronized (registryCOWLock) {
            LoggerAdapter adapter = new LoggerAdapter(logger); // Low cost.
            // TODO: Store loggers too to prevent redundant registration.
            node = registerStringLogger(loggerID, adapter, options);
        }
        return node;
    }

    protected LogNode<String> registerStringLogger(LoggerID loggerID, File file, LogOptions options) {
        return registerStringLogger(loggerID, file, null, options);
    }

    protected LogNode<String> registerStringLogger(LoggerID loggerID, File file, String prefix, LogOptions options) {
        LogNode<String> node;
        synchronized (registryCOWLock) {
            testRegisterLogger(loggerID); // Redundant checking, because file loggers are expensive.
            // TODO: Detect duplicate loggers (register same logger with given id and options).
            FileLoggerAdapter adapter = new FileLoggerAdapter(file, prefix);
            if (adapter.isInoperable()) {
                adapter.detachLogger();
                throw new RuntimeException("Failed to set up file logger for id '" + loggerID + "': " + file);
            }
            try {
                node = registerStringLogger(loggerID, adapter, options);
            } catch (Exception ex) {
                // TODO: Exception is still bad.
                adapter.detachLogger();
                throw new RuntimeException(ex);
            }
        }
        return node;
    }

    /**
     * Attach a logger to a stream. Redundant attaching will mean no changes. 
     * @param loggerID Must exist.
     * @param streamID Must exist.
     */
    protected void attachStringLogger(final LoggerID loggerID, final StreamID streamID) {
        // TODO: More light-weight locking concept (thinking of dynamically changing per player streams)?
        synchronized (registryCOWLock) {
            if (!hasLogger(loggerID)) {
                throw new RuntimeException("Logger is not registered: " + loggerID);
            }
            if (!hasStream(streamID)) {
                // Note: This should also ensure the voidStreamID can't be used, because that one can't be registered.
                throw new RuntimeException("Stream is not registered: " + streamID);
            }
            final LogNode<String> node = idNodeMap.get(loggerID);
            if (streamID == initStreamID) {
                // TODO: Not sure about restrictions here. Could allow attaching other streams temporarily if other stuff is wanted.
                switch(node.options.callContext) {
                    case PRIMARY_THREAD_ONLY:
                    case ANY_THREAD_DIRECT:
                        break;
                    default:
                        throw new RuntimeException("Unsupported call context for init stream " + streamID + ": " + node.options.callContext);
                }
            }
            idStreamMap.get(streamID).addNode(node);
        }
    }

    // TODO: Methods to replace options for loggers (+ loggers themselves)

    // TODO: Later: attach streams to streams ? [few loggers: attach loggers rather]

    // TODO: logger/stream: allow id lookup logger, file, etc. ?

    /**
     * Remove all loggers and streams including init, resulting in roughly the
     * same state as is after calling the AbstractLogger constructor. Call from
     * the primary thread (policy pending). If fallBackStreamID is set, it will be replaced by the init stream (if available) or by the void stream. 
     * 
     * @param msWaitFlush
     */
    protected void clear(final long msWaitFlush, final boolean recreateInitLogger) {
        // TODO: enum (remove_all, recreate init, remap init, remap all to init)
        synchronized (registryCOWLock) {
            // Remove loggers from string streams.
            for (ContentStream<String> stream : idStreamMap.values()) {
                stream.clear();
            }
            // Flush queues.
            dispatcher.flush(msWaitFlush);
            // Close/flush/shutdown string loggers, where possible, remove all from registry.
            for (final LogNode<String> node : idNodeMap.values()) {
                if (node.logger instanceof FileLoggerAdapter) {
                    FileLoggerAdapter logger = (FileLoggerAdapter) node.logger;
                    logger.flush();
                    logger.detachLogger();
                }
            }
            idNodeMap = new IdentityHashMap<LoggerID, LogNode<String>>();
            nameNodeMap = new HashMap<String, LogNode<String>>();
            nameLoggerIDMap = new HashMap<String, LoggerID>();
            // Remove string streams.
            idStreamMap = new IdentityHashMap<StreamID, ContentStream<String>>();
            nameStreamMap = new HashMap<String, ContentStream<String>>();
            nameStreamIDMap = new HashMap<String, StreamID>();
            if (recreateInitLogger) {
                createInitStream();
                registerInitLogger();
                if (fallBackStreamID != null && fallBackStreamID != voidStreamID) {
                    fallBackStreamID = initStreamID;
                }
            }
            else if (fallBackStreamID != null) {
                fallBackStreamID = voidStreamID;
            }
        }
    }

    //    /**
    //     * Remove all registered streams and loggers, recreates init logger (and stream).
    //     */
    //    public void clear(final long msWaitFlush) {
    //        clear(msWaitFlush, true);
    //    }

    /**
     * Rather a graceful shutdown, including waiting for the asynchronous task, if necessary. Clear the registry. Also removes the init logger [subject to change].
     * Call from the primary thread (policy pending).
     */
    public void shutdown() {
        clear(500, false); // TODO: Policy / making sense.
    }

}
