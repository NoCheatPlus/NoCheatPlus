package fr.neatmonster.nocheatplus.actions.types;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.StreamID;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

/**
 * Generic log action, capable of logging to any stream of LogManager with any
 * log level. Custom actions might need an altered action factory or override
 * toString, due to action prefixes/suffixes.
 * 
 * @author asofold
 *
 */
public class GenericLogAction extends ActionWithParameters<ViolationData, ActionList> {

    public static class GenericLogActionConfig {
        /**
         * Config path to check for optimized actions or during runtime. Set to
         * null, to always log
         */
        public final String configPathActive;
        public final boolean chatColor;
        public final StreamID streamID;
        public final Level level;
        /**
         * Suffix for log actions, such as i for ingame, f for file and c for
         * console. Band-aid for writing log actions back to config.
         */
        public final String actionConfigSuffix;

        public GenericLogActionConfig(String configPathActive, StreamID streamID, boolean chatColor, Level level, String actionSuffix) {
            this.configPathActive = configPathActive;
            this.streamID = streamID;
            this.chatColor = chatColor;
            this.level = level;
            this.actionConfigSuffix = actionSuffix;
        }
    }

    private final GenericLogActionConfig[] configs;

    /** Set if to check the configuration flag on execute being called. */
    private final boolean checkActive;
    /** Set if any config demands replacing color. */
    private final boolean replaceColor;
    /** Set if any config demands stripping color. */
    private final boolean stripColor;

    public GenericLogAction(final String name, final int delay, final int repeat, final String message, 
            final boolean checkActive, final GenericLogActionConfig... configs) {
        super(name, delay, repeat, message);
        final List<GenericLogActionConfig> temp = new ArrayList<GenericLogAction.GenericLogActionConfig>(configs.length);
        boolean replaceColor = false;
        boolean stripColor = false;
        boolean checkActiveUseful = false;
        for (int i = 0; i < configs.length; i++) {
            GenericLogActionConfig config = configs[i];
            if (config == null) {
                continue;
            }
            temp.add(config);
            if (config.chatColor) {
                replaceColor = true;
            } else {
                stripColor = true;
            }
            if (config.configPathActive != null) {
                checkActiveUseful = true;
            }
        }
        this.configs = temp.toArray(new GenericLogActionConfig[temp.size()]);
        this.checkActive = checkActive ? checkActiveUseful : false;
        this.replaceColor = replaceColor;
        this.stripColor = stripColor;
    }

    @Override
    public Action<ViolationData, ActionList> getOptimizedCopy(final ConfigFileWithActions<ViolationData, ActionList> config, final Integer threshold) {
        if (!config.getBoolean(ConfPaths.LOGGING_ACTIVE) || configs.length == 0) {
            return null;
        }
        final List<GenericLogActionConfig> temp = new ArrayList<GenericLogAction.GenericLogActionConfig>(configs.length);
        for (int i = 0; i < configs.length; i ++) {
            final GenericLogActionConfig logConfig = configs[i];
            if (checkActive && logConfig.configPathActive != null && !config.getBoolean(logConfig.configPathActive)) {
                continue;
            }
            temp.add(logConfig);
        }
        if (temp.isEmpty()) {
            return null;
        }
        final GenericLogActionConfig[] logConfigs = temp.toArray(new GenericLogActionConfig[temp.size()]);
        return new GenericLogAction(name, delay, repeat, message, false, logConfigs);
    }

    @Override
    public boolean execute(final ViolationData violationData) {
        // TODO: Consider permission caching or removing the feature? [Besides, check earlier?]
        if (violationData.player.hasPermission(violationData.getPermissionSilent())) {
            return false;
        }
        final LogManager logManager = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
        final String message = super.getMessage(violationData);
        final String messageNoColor = stripColor ? ColorUtil.removeColors(message) : null;
        final String messageWithColor = replaceColor ? ColorUtil.replaceColors(message) : null;
        final ConfigFile configFile = checkActive ? ConfigManager.getConfigFile() : null;
        for (int i = 0; i < configs.length; i ++) {
            final GenericLogActionConfig config = configs[i];
            if (checkActive && config.configPathActive != null && !configFile.getBoolean(config.configPathActive)) {
                continue;
            }
            logManager.log(config.streamID, config.level, config.chatColor ? messageWithColor : messageNoColor);
        }
        return false;
    }

    /**
     * Create the string that's used to define the action in the configuration file.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32 + 2 * configs.length);
        builder.append("log:" + name + ":" + delay + ":" + repeat + ":");
        for (int i = 0; i < configs.length; i ++) {
            builder.append(configs[i].actionConfigSuffix);
        }
        return builder.toString();
    }

    @Override
    public boolean isOptimized() {
        return !checkActive;
    }

}
