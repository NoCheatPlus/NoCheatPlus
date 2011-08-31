package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import cc.co.evenprime.bukkit.nocheat.actions.ActionManager;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;

import cc.co.evenprime.bukkit.nocheat.events.BlockPlaceEventManager;
import cc.co.evenprime.bukkit.nocheat.events.BlockBreakEventManager;
import cc.co.evenprime.bukkit.nocheat.events.PlayerMoveEventManager;
import cc.co.evenprime.bukkit.nocheat.events.PlayerTeleportEventManager;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;
import cc.co.evenprime.bukkit.nocheat.log.LogManager;

/**
 * 
 * NoCheat
 * 
 * Check various player events for their plausibility and log/deny them/react to
 * them based on configuration
 * 
 * @author Evenprime
 */
public class NoCheat extends JavaPlugin {

    private ConfigurationManager       conf;
    private LogManager                 log;
    private DataManager                data;

    private PlayerMoveEventManager     eventPlayerMoveManager;
    private PlayerTeleportEventManager eventPlayerTeleportManager;
    private BlockBreakEventManager     eventBlockBreakManager;

    private BlockPlaceEventManager     eventBlockPlaceManager;

    private ActionManager              action;

    public NoCheat() {

    }

    public void onDisable() {

        PluginDescriptionFile pdfFile = this.getDescription();

        if(conf != null)
            conf.cleanup();

        log.logToConsole(LogLevel.LOW, "[NoCheat] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    public void onEnable() {

        // First set up logging
        this.log = new LogManager(this);
        this.data = new DataManager();

        this.action = new ActionManager(log);
        // parse the nocheat.yml config file
        this.conf = new ConfigurationManager(ConfigurationManager.rootConfigFolder, action);

        eventPlayerMoveManager = new PlayerMoveEventManager(this);
        eventPlayerTeleportManager = new PlayerTeleportEventManager(this);
        eventBlockBreakManager = new BlockBreakEventManager(this);

        eventBlockPlaceManager = new BlockPlaceEventManager(this);

        PluginDescriptionFile pdfFile = this.getDescription();

        log.logToConsole(LogLevel.LOW, "[NoCheat] version [" + pdfFile.getVersion() + "] is enabled.");
    }

    public ConfigurationManager getConfigurationManager() {
        return conf;
    }

    public LogManager getLogManager() {
        return log;
    }

    public DataManager getDataManager() {
        return data;
    }

    public ActionManager getActionManager() {
        return action;
    }
}
