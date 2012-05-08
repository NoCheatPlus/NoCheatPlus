package fr.neatmonster.nocheatplus.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.Check;

public class ConfigManager {
    private static class LogFileFormatter extends Formatter {

        public static LogFileFormatter newInstance() {
            return new LogFileFormatter();
        }

        private final SimpleDateFormat date;

        private LogFileFormatter() {
            date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        @Override
        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable ex = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if (ex != null) {
                final StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }

    private static final Map<String, ConfigFile> worldsMap = new HashMap<String, ConfigFile>();

    private static FileHandler                   fileHandler;

    public static void cleanup() {
        fileHandler.flush();
        fileHandler.close();
        final Logger logger = Logger.getLogger("NoCheatPlus");
        logger.removeHandler(fileHandler);
        fileHandler = null;
    }

    public static ConfigFile getConfFile(final String worldName) {
        if (worldsMap.containsKey(worldName))
            return worldsMap.get(worldName);
        return worldsMap.get(null);
    }

    public static ConfigFile getConfigFile() {
        return worldsMap.get(null);
    }

    public static ConfigFile getConfigFile(final String worldName) {
        if (worldsMap.containsKey(worldName))
            return worldsMap.get(worldName);
        return getConfigFile();
    }

    public static void init() {
        // First try to obtain and parse the global config file
        final File rootFolder = NoCheatPlus.instance.getDataFolder();
        final ConfigFile root = new ConfigFile();
        root.setDefaults(new DefaultConfig());
        root.options().copyDefaults(true);
        root.options().copyHeader(true);

        final File globalConfigFile = new File(rootFolder, "config.yml");

        if (globalConfigFile.exists())
            try {
                root.load(globalConfigFile);
            } catch (final Exception e) {
                e.printStackTrace();
            }

        try {
            root.save(globalConfigFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        root.regenerateActionLists();

        // Create a corresponding ConfigurationByPlayer and
        // put the global configuration in the worlds map
        worldsMap.put(null, root);

        // Setup the file logger used by the plugin
        final Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.INFO);
        // Ignore parent's settings
        logger.setUseParentHandlers(false);
        for (final Handler h : logger.getHandlers())
            logger.removeHandler(h);

        if (fileHandler != null) {
            fileHandler.close();
            logger.removeHandler(fileHandler);
            fileHandler = null;
        }

        final File logFile = new File(rootFolder, root.getString(ConfPaths.LOGGING_FILENAME));
        try {
            try {
                logFile.getParentFile().mkdirs();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            fileHandler = new FileHandler(logFile.getCanonicalPath(), true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(LogFileFormatter.newInstance());

            logger.addHandler(fileHandler);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        Check.setFileLogger(logger);

        // Try to find world-specific config files
        final HashMap<String, File> worldFiles = new HashMap<String, File>();

        if (rootFolder.isDirectory())
            for (final File file : rootFolder.listFiles())
                if (file.isFile()) {
                    final String filename = file.getName();
                    if (filename.matches(".+_config.yml$")) {
                        // Get the first part = world name
                        final String worldname = filename.substring(0, filename.length() - 10);
                        worldFiles.put(worldname, file);
                    }
                }

        for (final Entry<String, File> worldEntry : worldFiles.entrySet()) {
            final File worldConfigFile = worldEntry.getValue();
            final ConfigFile world = new ConfigFile();
            world.setDefaults(root);

            try {
                world.load(worldConfigFile);
                worldsMap.put(worldEntry.getKey(), world);
                // write the config file back to disk immediately
                world.save(worldConfigFile);
            } catch (final Exception e) {
                System.out.println("NoCheatPlus: Couldn't load world-specific config for " + worldEntry.getKey());
                e.printStackTrace();
            }

            world.regenerateActionLists();
        }
    }

    public static void writeInstructions() {
        try {
            final InputStream is = NoCheatPlus.instance.getResource("Instructions.txt");
            final FileOutputStream fos = new FileOutputStream(new File(NoCheatPlus.instance.getDataFolder(),
                    "Intructions.txt"));
            final byte[] buffer = new byte[64 * 1024];
            int length = 0;
            while ((length = is.read(buffer)) != -1)
                fos.write(buffer, 0, length);
            fos.flush();
            fos.close();
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
