package cc.co.evenprime.bukkit.nocheat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.BlockBreakCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.BlockPlaceCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.chat.ChatCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.fight.FightCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.inventory.InventoryCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.moving.MovingCheckListener;
import cc.co.evenprime.bukkit.nocheat.command.CommandHandler;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.data.PlayerManager;
import cc.co.evenprime.bukkit.nocheat.debug.ActiveCheckPrinter;
import cc.co.evenprime.bukkit.nocheat.debug.LagMeasureTask;
import cc.co.evenprime.bukkit.nocheat.events.WorkaroundsEventManager;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;
import cc.co.evenprime.bukkit.nocheat.log.LogManager;

/**
 * 
 * NoCheat
 * 
 * Check various player events for their plausibility and log/deny them/react to
 * them based on configuration
 */
public class NoCheat extends JavaPlugin {

    private ConfigurationManager   conf;
    private LogManager             log;
    private PlayerManager          players;

    private List<EventManager> eventManagers;

    private LagMeasureTask         lagMeasureTask;

    private int                    taskId = -1;

    public NoCheat() {

    }

    public void onDisable() {

        PluginDescriptionFile pdfFile = this.getDescription();

        if(taskId != -1) {
            getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        if(lagMeasureTask != null) {
            lagMeasureTask.cancel();
            lagMeasureTask = null;
        }

        if(conf != null) {
            conf.cleanup();
            conf = null;
        }

        // Just to be sure nothing gets left out
        getServer().getScheduler().cancelTasks(this);

        log.logToConsole(LogLevel.LOW, "[NoCheat] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    public void onEnable() {

        // First set up logging
        this.log = new LogManager();

        // Then set up in memory per player data storage
        this.players = new PlayerManager(this);

        // Then read the configuration files
        this.conf = new ConfigurationManager(this.getDataFolder());

        eventManagers = new ArrayList<EventManager>(8); // Big enough
        // Then set up the event listeners
        eventManagers.add(new MovingCheckListener(this));
        eventManagers.add(new WorkaroundsEventManager(this));
        eventManagers.add(new ChatCheckListener(this));
        eventManagers.add(new BlockBreakCheckListener(this));
        eventManagers.add(new BlockPlaceCheckListener(this));
        eventManagers.add(new FightCheckListener(this));
        eventManagers.add(new InventoryCheckListener(this));

        // Then set up a task to monitor server lag
        if(lagMeasureTask == null) {
            lagMeasureTask = new LagMeasureTask(this);
            lagMeasureTask.start();
        }

        // Then print a list of active checks per world
        ActiveCheckPrinter.printActiveChecks(this, eventManagers);

        // Tell the server admin that we finished loading NoCheat now
        log.logToConsole(LogLevel.LOW, "[NoCheat] version [" + this.getDescription().getVersion() + "] is enabled.");
    }

    public ConfigurationCacheStore getConfig(Player player) {
        if(player != null)
            return getConfig(player.getWorld());
        else
            return conf.getConfigurationCacheForWorld(null);
    }

    public ConfigurationCacheStore getConfig(World world) {
        if(world != null)
            return conf.getConfigurationCacheForWorld(world.getName());
        else
            return conf.getConfigurationCacheForWorld(null);
    }

    public void log(LogLevel level, String message, ConfigurationCacheStore cc) {
        log.log(level, message, cc);
    }

    public void clearCriticalData(String playerName) {
        players.clearCriticalData(playerName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean result = CommandHandler.handleCommand(this, sender, command, label, args);

        return result;
    }

    public int getIngameSeconds() {
        if(lagMeasureTask != null)
            return lagMeasureTask.getIngameSeconds();
        return 0;
    }

    public boolean skipCheck() {
        if(lagMeasureTask != null)
            return lagMeasureTask.skipCheck();
        return false;
    }

    public long getIngameSecondDuration() {
        if(lagMeasureTask != null)
            return lagMeasureTask.getIngameSecondDuration();
        return 1000L;
    }

    public void logToConsole(LogLevel low, String message) {
        if(log != null) {
            log.logToConsole(low, message);
        }
    }

    public void reloadConfiguration() {
        conf.cleanup();
        this.conf = new ConfigurationManager(this.getDataFolder());
        players.cleanDataMap();
        players.clearCriticalData();
    }

    /**
     * Call this periodically to walk over the stored data map and remove
     * old/unused entries
     * 
     */
    public void cleanDataMap() {
        players.cleanDataMap();
    }

    /**
     * An interface method usable by other plugins to collect information about
     * a player. It will include the plugin version, two timestamps (beginning
     * and end of data collection for that player), and various data from
     * checks)
     * 
     * @param playerName
     *            a player name
     * @return A newly created map of identifiers and corresponding values
     */
    public Map<String, Object> getPlayerData(String playerName) {

        Map<String, Object> map = new TreeMap<String, Object>();

        players.getPlayerData(playerName, map);

        map.put("nocheat.version", this.getDescription().getVersion());

        return map;
    }

    public NoCheatPlayer getPlayer(Player player) {
        return players.getPlayer(player);
    }
}
