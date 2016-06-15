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

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.registry.feature.INotifyReload;
import fr.neatmonster.nocheatplus.components.registry.order.SetupOrder;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.details.AbstractLogManager;
import fr.neatmonster.nocheatplus.logging.details.BukkitLogNodeDispatcher;
import fr.neatmonster.nocheatplus.logging.details.ContentLogger;
import fr.neatmonster.nocheatplus.logging.details.FileLoggerAdapter;
import fr.neatmonster.nocheatplus.logging.details.LogOptions;
import fr.neatmonster.nocheatplus.logging.details.LogOptions.CallContext;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;


/**
 * Central access point for logging. The default loggers use the stream names,
 * at least as prefixes).<br>
 * Note that logging to the init/plugin/server with debug/fine or finer, might
 * result in the server loggers suppressing those. As long as default file is
 * activated, logging to init will log all levels to the file.<hr>
 * Not intended to be added as a component (INotifyReload is a band-aid and may be checked "early" by the reloading routine).
 * 
 * @author dev1mc
 *
 */
@SetupOrder(priority = Integer.MIN_VALUE) // Just in case.
public class BukkitLogManager extends AbstractLogManager implements INotifyReload {

    // TODO: Make LogManager an interface <- AbstractLogManager <- BukkitLogManager (hide some / instanceof).

    // TODO: ingame logging [ingame needs api to keep track of players who receive notifications.].
    // TODO: Later: Custom loggers (file, other), per-player-streams (debug per player), custom ingame loggers (one or more players).

    private static ContentLogger<String> serverLogger = new ContentLogger<String>() {
        @Override
        public void log(Level level, String content) {
            try {
                Bukkit.getLogger().log(level, "[NoCheatPlus] " + content);
            } catch (Throwable t) {}
        }
    };

    protected final Plugin plugin;

    /**
     * This will create all default loggers as well.
     * @param plugin
     */
    public BukkitLogManager(Plugin plugin) {
        super(new BukkitLogNodeDispatcher(plugin), Streams.defaultPrefix, Streams.INIT);
        this.plugin = plugin;
        ConfigFile config = ConfigManager.getConfigFile();
        createDefaultLoggers(config);
        getLogNodeDispatcher().setMaxQueueSize(config.getInt(ConfPaths.LOGGING_MAXQUEUESIZE));
    }

    @Override
    protected void registerInitLogger() {
        synchronized (registryCOWLock) {
            if (!hasStream(Streams.INIT)) {
                createInitStream();
            }
            else if (hasLogger(Streams.INIT.name)) {
                // Shallow check.
                return;
            }
            // Attach a new restrictive init logger.
            boolean bukkitLoggerAsynchronous = ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_BACKEND_CONSOLE_ASYNCHRONOUS);
            LoggerID initLoggerID = registerStringLogger(serverLogger, new LogOptions(Streams.INIT.name, bukkitLoggerAsynchronous ? CallContext.ANY_THREAD_DIRECT : CallContext.PRIMARY_THREAD_ONLY));
            attachStringLogger(initLoggerID, Streams.INIT);
        }
    }

    /**
     * Create default loggers and streams.
     */
    protected void createDefaultLoggers(ConfigFile config) {

        // Default streams.
        for (StreamID streamID : new StreamID[] {
                Streams.STATUS,
                Streams.SERVER_LOGGER, Streams.PLUGIN_LOGGER, 
                Streams.NOTIFY_INGAME,
                Streams.DEFAULT_FILE, Streams.TRACE_FILE,

        }) {
            createStringStream(streamID);
        }

        // Default prefixes.
        final String prefixIngame = ColorUtil.replaceColors(config.getString(ConfPaths.LOGGING_BACKEND_INGAMECHAT_PREFIX));
        final String prefixFile = config.getString(ConfPaths.LOGGING_BACKEND_FILE_PREFIX);

        // Variables for temporary use.
        LoggerID tempID;

        // Configuration/defaults.
        // TODO: More configurability.
        // TODO: Might attempt to detect if a thread-safe logging framework is in use ("default" instead of false/true).
        boolean bukkitLoggerAsynchronous = config.getBoolean(ConfPaths.LOGGING_BACKEND_CONSOLE_ASYNCHRONOUS);
        // TODO: Do keep considering: AYNCHRONOUS_DIRECT -> ASYNCHRONOUS_TASK (not to delay async. event handling).
        CallContext defaultAsynchronousContext = CallContext.ASYNCHRONOUS_TASK; // Plugin runtime + asynchronous.

        // Server logger.
        tempID = registerStringLogger(serverLogger, new LogOptions(Streams.SERVER_LOGGER.name, bukkitLoggerAsynchronous ? defaultAsynchronousContext : CallContext.PRIMARY_THREAD_TASK));
        attachStringLogger(tempID, Streams.SERVER_LOGGER);

        // Plugin logger.
        tempID = registerStringLogger(plugin.getLogger(), new LogOptions(Streams.PLUGIN_LOGGER.name, bukkitLoggerAsynchronous ? defaultAsynchronousContext : CallContext.PRIMARY_THREAD_TASK));
        attachStringLogger(tempID, Streams.PLUGIN_LOGGER);

        // Ingame logger (assume not thread-safe at first).
        // TODO: Thread-safe ProtocolLib-based implementation?
        // TODO: Consider using a task.
        tempID = registerStringLogger(new ContentLogger<String>() {

            @Override
            public void log(Level level, String content) {
                // Ignore level for now.
                NCPAPIProvider.getNoCheatPlusAPI().sendAdminNotifyMessage(prefixIngame == null ? content : (prefixIngame + content));
            }

        }, new LogOptions(Streams.NOTIFY_INGAME.name, CallContext.PRIMARY_THREAD_DIRECT));
        attachStringLogger(tempID, Streams.NOTIFY_INGAME);

        // Abstract STATUS stream (efficient version of INIT during plugin runtime).
        attachStringLogger(getLoggerID(Streams.SERVER_LOGGER.name), Streams.STATUS);

        // Default file logger.
        String fileName = config.getString(ConfPaths.LOGGING_BACKEND_FILE_FILENAME).trim();
        ContentLogger<String> defaultFileLogger = null;
        if (!fileName.isEmpty() && !fileName.equalsIgnoreCase("none")) {
            defaultFileLogger = newFileLogger(fileName, plugin.getDataFolder(), prefixFile);
        }

        ContentLogger<String> traceFileLogger = null;
        // TODO: Create a dedicated trace file, if "needed".

        if (defaultFileLogger != null) {
            // Do attach the default file logger.
            tempID = registerStringLogger(defaultFileLogger, new LogOptions(Streams.DEFAULT_FILE.name, defaultAsynchronousContext));
            attachStringLogger(tempID, Streams.DEFAULT_FILE);

            // Trace file logger (if no extra file).
            if (traceFileLogger == null) {
                attachStringLogger(getLoggerID(Streams.DEFAULT_FILE.name), Streams.TRACE_FILE); // Direct to default file for now.
            }

            // Abstract INIT stream (attach file logger).
            // TODO: Consider configurability of skipping, depending on bukkitLoggerAsynchronous.
            tempID = registerStringLogger(defaultFileLogger, new LogOptions(Streams.DEFAULT_FILE.name +".init", CallContext.ANY_THREAD_DIRECT));
            attachStringLogger(tempID, Streams.INIT);

            //Abstract STATUS stream (attach file logger).
            attachStringLogger(getLoggerID(Streams.DEFAULT_FILE.name), Streams.STATUS);
        }
    }

    /**
     * Create a new file logger. Relative paths are interpreted relative to the
     * given default directory (!). The used FileLoggerAdapter will interpret
     * names without extensions as folders to be created, if not existent.
     * 
     * @param fileName
     *            Path to the log file, or path to a folder for log files by
     *            date (a folder is created, if no extension is given).
     * @param defaultDir
     *            This is used as a base, if fileName represents a relative
     *            path.
     * @param prefix
     *            A prefix to use for each message (can be null).
     * @return
     */
    protected ContentLogger<String> newFileLogger(String fileName, File defaultDir, String prefix) {
        File file = new File(fileName);
        if (!file.isAbsolute()) {
            file = new File(defaultDir, file.getPath());
        }
        // TODO: Sanity check file+extensions and fall-back if not valid [make an auxiliary method doing all this at once]!
        try {
            FileLoggerAdapter logger = new FileLoggerAdapter(file, prefix); // TODO: Method to get-or-create these (store logger by canonical abs paths).
            if (logger.isInoperable()) {
                // TODO: Might want to log this?
                logger.detachLogger();
                return null;
            } else {
                return logger;
            }
        } catch (Throwable t) {
            // TODO: Complain.
        }
        return null;
    }

    @Override
    public void onReload() {
        // Hard clear and re-do loggers. Might result in loss of content.
        // TODO: Register for "early onReload call", needs API addition.
        clear(0L, true); // Can not afford to wait.
        createDefaultLoggers(ConfigManager.getConfigFile());
    }

    /**
     * Necessary logging to a primary thread task (TickTask) or asynchronously.
     * This can be called multiple times without causing damage.
     */
    public void startTasks() {
        // TODO: Schedule / hide (redundant calls mean no harm, at present). 
        ((BukkitLogNodeDispatcher) getLogNodeDispatcher()).startTasks();
    }

}
