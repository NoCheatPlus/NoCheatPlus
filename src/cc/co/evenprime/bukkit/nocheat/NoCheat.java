package cc.co.evenprime.bukkit.nocheat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import cc.co.evenprime.bukkit.nocheat.checks.WorkaroundsListener;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.BlockBreakCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.BlockPlaceCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.chat.ChatCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.fight.FightCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.inventory.InventoryCheckListener;
import cc.co.evenprime.bukkit.nocheat.checks.moving.MovingCheckListener;
import cc.co.evenprime.bukkit.nocheat.command.CommandHandler;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.PlayerManager;
import cc.co.evenprime.bukkit.nocheat.debug.ActiveCheckPrinter;
import cc.co.evenprime.bukkit.nocheat.debug.LagMeasureTask;

/**
 * 
 * NoCheat
 * 
 * Check various player events for their plausibility and log/deny them/react to
 * them based on configuration
 */
public class NoCheat extends JavaPlugin implements Listener {

    private ConfigurationManager conf;
    private PlayerManager        players;

    private List<EventManager>   eventManagers;

    private LagMeasureTask       lagMeasureTask;
    private Logger               fileLogger;

    public NoCheat() {

    }

    public void onDisable() {

        PluginDescriptionFile pdfFile = this.getDescription();

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

        System.out.println("[NoCheat] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    public void onEnable() {

        // Then set up in memory per player data storage
        this.players = new PlayerManager(this);

        // Then read the configuration files
        this.conf = new ConfigurationManager(this, this.getDataFolder());

        eventManagers = new ArrayList<EventManager>(8); // Big enough
        // Then set up the event listeners
        eventManagers.add(new MovingCheckListener(this));
        eventManagers.add(new WorkaroundsListener(this));
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

        // register all listeners
        for(EventManager eventManager : eventManagers) {
            Bukkit.getPluginManager().registerEvents(eventManager, this);
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        NoCheatConfiguration.writeInstructions(this.getDataFolder());

        // Tell the server admin that we finished loading NoCheat now
        System.out.println("[NoCheat] version [" + this.getDescription().getVersion() + "] is enabled.");
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean result = CommandHandler.handleCommand(this, sender, command, label, args);

        return result;
    }

    public boolean skipCheck() {
        if(lagMeasureTask != null)
            return lagMeasureTask.skipCheck();
        return false;
    }

    public void reloadConfiguration() {
        conf.cleanup();
        this.conf = new ConfigurationManager(this, this.getDataFolder());
        players.cleanDataMap();
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void logEvent(NoCheatLogEvent event) {
        if(event.toConsole()) {
            // Console logs are not colored
            System.out.println(Colors.removeColors(event.getPrefix() + event.getMessage()));
        }
        if(event.toChat()) {
            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                if(player.hasPermission(Permissions.ADMIN_CHATLOG)) {
                    // Chat logs are potentially colored
                    player.sendMessage(Colors.replaceColors(event.getPrefix() + event.getMessage()));
                }
            }
        }
        if(event.toFile()) {
            // File logs are not colored
            fileLogger.info(Colors.removeColors(event.getMessage()));
        }
    }

    public void setFileLogger(Logger logger) {
        this.fileLogger = logger;
    }
}
