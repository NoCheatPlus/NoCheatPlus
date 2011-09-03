package cc.co.evenprime.bukkit.nocheat.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import cc.co.evenprime.bukkit.nocheat.DefaultConfiguration;
import cc.co.evenprime.bukkit.nocheat.actions.ActionManager;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.config.tree.ActionListOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationTree;
import cc.co.evenprime.bukkit.nocheat.config.tree.Option;
import cc.co.evenprime.bukkit.nocheat.config.tree.ParentOption;
import cc.co.evenprime.bukkit.nocheat.file.DescriptionGenerator;
import cc.co.evenprime.bukkit.nocheat.file.FlatActionParser;
import cc.co.evenprime.bukkit.nocheat.file.FlatConfigGenerator;
import cc.co.evenprime.bukkit.nocheat.file.FlatConfigParser;

/**
 * Central location for everything that's described in the configuration file(s)
 * 
 * @author Evenprime
 * 
 */
public class ConfigurationManager {

    private final static String                   configFileName            = "config.txt";
    private final static String                   actionFileName            = "actions.txt";
    private final static String                   defaultActionFileName     = "default_actions.txt";
    private final static String                   descriptionsFileName      = "descriptions.txt";

    private final Map<String, ConfigurationCache> worldnameToConfigCacheMap = new HashMap<String, ConfigurationCache>();

    // Only use one filehandler per file, therefore keep open filehandlers in a
    // map
    private final Map<File, FileHandler>          fileToFileHandlerMap      = new HashMap<File, FileHandler>();

    private final ConfigurationTree               defaultTree               = DefaultConfiguration.buildDefaultConfigurationTree();

    private class LogFileFormatter extends Formatter {

        private final SimpleDateFormat date;

        public LogFileFormatter() {
            date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            Throwable ex = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if(ex != null) {
                StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }

    // Our personal logger
    // private final static String loggerName = "cc.co.evenprime.nocheat";
    // public final Logger logger = Logger.getLogger(loggerName);

    public ConfigurationManager(String rootConfigFolder, ActionManager action) {

        // Parse actions file
        initializeActions(rootConfigFolder, action);
        // Setup the configuration tree
        initializeConfig(rootConfigFolder, action);

    }

    private void initializeActions(String rootConfigFolder, ActionManager action) {

        FlatActionParser parser = new FlatActionParser();

        DefaultConfiguration.writeDefaultActionFile(new File(rootConfigFolder, defaultActionFileName));
        parser.read(action, new File(rootConfigFolder, defaultActionFileName));
        parser.read(action, new File(rootConfigFolder, actionFileName));

    }

    /**
     * Read the configuration file and assign either standard values or whatever
     * is declared in the file
     * 
     * @param configurationFile
     */
    private void initializeConfig(String rootConfigFolder, ActionManager actionManager) {

        // First try to obtain and parse the global config file
        ConfigurationTree root;
        File globalConfigFile = getGlobalConfigFile(rootConfigFolder);

        try {
            root = createFullConfigurationTree(defaultTree, globalConfigFile);
        } catch(Exception e) {
            root = DefaultConfiguration.buildDefaultConfigurationTree();
        }

        writeConfigFile(globalConfigFile, root);

        // Create a corresponding Configuration Cache
        // put the global config on the config map
        worldnameToConfigCacheMap.put(null, new ConfigurationCache(root, setupFileLogger(new File(rootConfigFolder, root.getString("logging.filename")))));

        // Try to find world-specific config files
        Map<String, File> worldFiles = getWorldSpecificConfigFiles(rootConfigFolder);

        for(String worldName : worldFiles.keySet()) {

            File worldConfigFile = worldFiles.get(worldName);
            try {
                ConfigurationTree world = createPartialConfigurationTree(root, worldConfigFile);

                worldnameToConfigCacheMap.put(worldName, createConfigurationCache(rootConfigFolder, world));

                // write the config file back to disk immediately
                writeConfigFile(worldFiles.get(worldName), world);
            } catch(IOException e) {
                System.out.println("NoCheat: Couldn't load world-specific config for " + worldName);
            }
        }

        // Write the descriptions-file for the default tree
        writeDescriptionFile(new File(rootConfigFolder, descriptionsFileName), defaultTree);
    }

    private ConfigurationCache createConfigurationCache(String rootConfigFolder, Configuration configProvider) {

        return new ConfigurationCache(configProvider, setupFileLogger(new File(rootConfigFolder, configProvider.getString("logging.filename"))));

    }

    public static File getGlobalConfigFile(String rootFolder) {

        File globalConfig = new File(rootFolder, configFileName);

        return globalConfig;
    }

    public static Map<String, File> getWorldSpecificConfigFiles(String rootConfigFolder) {

        HashMap<String, File> files = new HashMap<String, File>();

        File rootFolder = new File(rootConfigFolder);
        if(rootFolder.isDirectory()) {
            for(File f : rootFolder.listFiles()) {
                if(f.isFile()) {
                    String filename = f.getName();
                    if(filename.matches(".+_" + configFileName + "$")) {
                        // Get the first part = world name
                        String worldname = filename.substring(0, filename.length() - (configFileName.length() + 1));
                        files.put(worldname, f);
                    }
                }
            }
        }
        return files;
    }

    /**
     * Create a full configuration tree based on a default tree and a
     * configuration file
     * The resulting tree will have all settings of the defaultTree with entries
     * replaced,
     * if existing, by data from the configurationFile
     * 
     * @param defaultTree
     * @param configurationFile
     * @throws IOException
     */
    public static ConfigurationTree createFullConfigurationTree(ConfigurationTree defaultTree, File configurationFile) throws IOException {

        return yamlToTree(defaultTree, configurationFile, true);

    }

    /**
     * Create a partial configuration tree based on a default tree and a
     * configuration file
     * The resulting tree will only have settings from the configurationFile,
     * but reference
     * the defaultTree as its parent.
     * 
     * @param defaultTree
     * @param configurationFile
     * @throws IOException
     */
    public static ConfigurationTree createPartialConfigurationTree(ConfigurationTree defaultTree, File configurationFile) throws IOException {

        ConfigurationTree tree = yamlToTree(defaultTree, configurationFile, false);
        tree.setParent(defaultTree);

        return tree;

    }

    private static ConfigurationTree yamlToTree(ConfigurationTree defaults, File configurationFile, boolean fullCopy) throws IOException {

        FlatConfigParser source = new FlatConfigParser();
        source.read(configurationFile);

        ConfigurationTree partial = new ConfigurationTree();

        for(Option o : defaults.getAllOptions()) {
            if(o instanceof ActionListOption) {
                ActionListOption o2 = ((ActionListOption) o).clone();
                partial.add(o.getParent().getFullIdentifier(), o2);
                // Does the new source have a node for this property??
                Object prop = source.getProperty(o2.getFullIdentifier());
                if(prop instanceof Map<?, ?> && prop != null) {
                    // Yes, so we rather take the data from that node
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> m = (Map<Object, Object>) prop;
                    o2.clear();
                    for(Entry<Object, Object> entry : m.entrySet()) {
                        try {
                            o2.add(Integer.parseInt((String) entry.getKey()), (String) entry.getValue());
                        } catch(Exception e) {
                            System.out.println("NoCheat: PROBLEM OFFICER?!?!");
                        }
                    }
                }
                if(!fullCopy && prop == null) {
                    o2.setActive(false);
                }
            } else if(o instanceof ParentOption) {
                ParentOption o2 = new ParentOption(o.getIdentifier());
                partial.add(o.getParent().getFullIdentifier(), o2);
            } else if(o instanceof ChildOption) {
                ChildOption o2 = ((ChildOption) o).clone();
                partial.add(o.getParent().getFullIdentifier(), o2);
                o2.setStringValue(source.getString(o2.getFullIdentifier(), o2.getStringValue()));
                if(!fullCopy && source.getProperty(o.getFullIdentifier()) == null) {
                    o2.setActive(false);
                }
            }
        }

        return partial;
    }

    private Logger setupFileLogger(File logfile) {

        FileHandler fh = fileToFileHandlerMap.get(logfile);

        if(fh == null) {
            try {
                try {
                    logfile.getParentFile().mkdirs();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                fh = new FileHandler(logfile.getCanonicalPath(), true);
                // We decide before logging what gets logged there anyway
                // because different worlds may use this filehandler and
                // therefore may need to log different message levels
                fh.setLevel(Level.ALL);
                fh.setFormatter(new LogFileFormatter());
                fileToFileHandlerMap.put(logfile, fh);

            } catch(SecurityException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        // this logger will be used ONLY for logging to a single log-file and
        // only
        // in this plugin, therefore it doesn't need any namespace
        Logger l = Logger.getAnonymousLogger();
        l.setLevel(Level.INFO);
        // Ignore parent's settings
        l.setUseParentHandlers(false);
        l.addHandler(fh);

        return l;
    }

    /**
     * Reset the loggers and flush and close the fileHandlers
     * to be able to use them next time without problems
     */
    public void cleanup() {

        // Remove handlers from the logger
        for(ConfigurationCache c : worldnameToConfigCacheMap.values()) {
            for(Handler h : c.logging.filelogger.getHandlers()) {
                c.logging.filelogger.removeHandler(h);
            }
        }

        // Close all file handlers
        for(FileHandler fh : fileToFileHandlerMap.values()) {
            fh.flush();
            fh.close();
        }
    }

    /**
     * Write configuration to specific file
     * 
     * @param f
     */
    public static void writeConfigFile(File f, ConfigurationTree configuration) {
        try {
            if(f.getParentFile() != null)
                f.getParentFile().mkdirs();

            f.createNewFile();
            BufferedWriter w = new BufferedWriter(new FileWriter(f));

            w.write(FlatConfigGenerator.treeToFlatFile(configuration));

            w.flush();
            w.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write a file with the descriptions of all options of a specific
     * configuration tree
     * 
     * @param f
     */
    public static void writeDescriptionFile(File f, ConfigurationTree configuration) {
        try {
            if(f.getParentFile() != null)
                f.getParentFile().mkdirs();

            f.createNewFile();
            BufferedWriter w = new BufferedWriter(new FileWriter(f));

            w.write(DescriptionGenerator.treeToDescription(configuration));

            w.flush();
            w.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the cache of the specified world, or the default cache,
     * if no cache exists for that world.
     * 
     * @param worldname
     * @return
     */
    public ConfigurationCache getConfigurationCacheForWorld(String worldname) {

        ConfigurationCache cache = worldnameToConfigCacheMap.get(worldname);

        if(cache != null) {
            return cache;
        } else {
            // Enter a reference to the cache under the new name
            // to be faster in looking it up later
            cache = worldnameToConfigCacheMap.get(null);
            worldnameToConfigCacheMap.put(worldname, cache);

            return cache;
        }
    }

    public static File getDescriptionFile(String rootConfigFolder) {

        return new File(rootConfigFolder, descriptionsFileName);
    }

    public static File getDefaultActionFile(String rootConfigFolder) {
        return new File(rootConfigFolder, defaultActionFileName);
    }

    public static File getActionFile(String rootConfigFolder) {
        return new File(rootConfigFolder, actionFileName);
    }
}
