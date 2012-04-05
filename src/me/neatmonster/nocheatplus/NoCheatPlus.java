package me.neatmonster.nocheatplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import me.neatmonster.nocheatplus.checks.WorkaroundsListener;
import me.neatmonster.nocheatplus.checks.blockbreak.BlockBreakCheckListener;
import me.neatmonster.nocheatplus.checks.blockplace.BlockPlaceCheckListener;
import me.neatmonster.nocheatplus.checks.chat.ChatCheckListener;
import me.neatmonster.nocheatplus.checks.fight.FightCheckListener;
import me.neatmonster.nocheatplus.checks.inventory.InventoryCheckListener;
import me.neatmonster.nocheatplus.checks.moving.MovingCheckListener;
import me.neatmonster.nocheatplus.command.CommandHandler;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.config.ConfigurationManager;
import me.neatmonster.nocheatplus.config.NoCheatPlusConfiguration;
import me.neatmonster.nocheatplus.config.Permissions;
import me.neatmonster.nocheatplus.data.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * NoCheatPlus
 * 
 * Check various player events for their plausibility and log/deny them/react to
 * them based on configuration
 */
public class NoCheatPlus extends JavaPlugin implements Listener {

    private ConfigurationManager conf;
    private CommandHandler       commandHandler;
    private PlayerManager        players;

    private List<EventManager>   eventManagers;

    private LagMeasureTask       lagMeasureTask;
    private Logger               fileLogger;

    public NoCheatPlus() {

    }

    /**
     * Call this periodically to walk over the stored data map and remove
     * old/unused entries
     * 
     */
    public void cleanDataMap() {
        players.cleanDataMap();
    }

    public ConfigurationCacheStore getConfig(final Player player) {
        if (player != null)
            return getConfig(player.getWorld());
        else
            return conf.getConfigurationCacheForWorld(null);
    }

    public ConfigurationCacheStore getConfig(final World world) {
        if (world != null)
            return conf.getConfigurationCacheForWorld(world.getName());
        else
            return conf.getConfigurationCacheForWorld(null);
    }

    public NoCheatPlusPlayer getPlayer(final Player player) {
        return players.getPlayer(player);
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
    public Map<String, Object> getPlayerData(final String playerName) {

        final Map<String, Object> map = players.getPlayerData(playerName);

        map.put("nocheatplus.version", getDescription().getVersion());

        return map;
    }

    @EventHandler(
            priority = EventPriority.MONITOR)
    public void logEvent(final NoCheatPlusLogEvent event) {
        if (event.toConsole())
            // Console logs are not colored
            System.out.println(Colors.removeColors(event.getPrefix() + event.getMessage()));
        if (event.toChat())
            for (final Player player : Bukkit.getServer().getOnlinePlayers())
                if (player.hasPermission(Permissions.ADMIN_CHATLOG))
                    // Chat logs are potentially colored
                    player.sendMessage(Colors.replaceColors(event.getPrefix() + event.getMessage()));
        if (event.toFile())
            // File logs are not colored
            fileLogger.info(Colors.removeColors(event.getMessage()));
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final boolean result = commandHandler.handleCommand(this, sender, command, label, args);

        return result;
    }

    @Override
    public void onDisable() {

        final PluginDescriptionFile pdfFile = getDescription();

        if (lagMeasureTask != null) {
            lagMeasureTask.cancel();
            lagMeasureTask = null;
        }

        if (conf != null) {
            conf.cleanup();
            conf = null;
        }

        // Just to be sure nothing gets left out
        getServer().getScheduler().cancelTasks(this);

        commandHandler = null;

        System.out.println("[NoCheatPlus] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    @Override
    public void onEnable() {

        // Then set up in memory per player data storage
        players = new PlayerManager(this);

        commandHandler = new CommandHandler(this);
        // Then read the configuration files
        conf = new ConfigurationManager(this, getDataFolder());

        eventManagers = new ArrayList<EventManager>(8); // Big enough
        // Then set up the event listeners
        eventManagers.add(new MovingCheckListener(this));
        eventManagers.add(new WorkaroundsListener());
        eventManagers.add(new ChatCheckListener(this));
        eventManagers.add(new BlockBreakCheckListener(this));
        eventManagers.add(new BlockPlaceCheckListener(this));
        eventManagers.add(new FightCheckListener(this));
        eventManagers.add(new InventoryCheckListener(this));

        // Then set up a task to monitor server lag
        if (lagMeasureTask == null) {
            lagMeasureTask = new LagMeasureTask(this);
            lagMeasureTask.start();
        }

        // register all listeners
        for (final EventManager eventManager : eventManagers)
            Bukkit.getPluginManager().registerEvents(eventManager, this);

        Bukkit.getPluginManager().registerEvents(this, this);

        NoCheatPlusConfiguration.writeInstructions(this);

        // Tell the server admin that we finished loading NoCheatPlus now
        System.out.println("[NoCheatPlus] version [" + getDescription().getVersion() + "] is enabled.");
    }

    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        String message = "";
        // Disable Zombe's fly mod
        if (!player.hasPermission(Permissions.ZOMBE_FLY))
            message = message + "§f §f §1 §0 §2 §4";
        // Disable Zombe's cheats
        if (!player.hasPermission(Permissions.ZOMBE_CHEATS))
            message = message + "§f §f §2 §0 §4 §8";
        // Disable CJB's fly mod
        if (!player.hasPermission(Permissions.CJB_FLY))
            message = message + "§3 §9 §2 §0 §0 §1";
        // Disable CJB's XRay
        if (!player.hasPermission(Permissions.CJB_XRAY))
            message = message + "§3 §9 §2 §0 §0 §2";
        // Disable CJB's minimap
        if (!player.hasPermission(Permissions.CJB_MINIMAP))
            message = message + "§3 §9 §2 §0 §0 §3";
        player.sendMessage(message);
    }

    public void reloadConfiguration() {
        conf.cleanup();
        conf = new ConfigurationManager(this, getDataFolder());
        players.cleanDataMap();
    }

    public void setFileLogger(final Logger logger) {
        fileLogger = logger;
    }

    public boolean skipCheck() {
        if (lagMeasureTask != null)
            return lagMeasureTask.skipCheck();
        return false;
    }
}
