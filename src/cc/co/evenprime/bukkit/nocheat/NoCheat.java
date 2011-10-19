package cc.co.evenprime.bukkit.nocheat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import cc.co.evenprime.bukkit.nocheat.actions.ActionManager;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.debug.LagMeasureTask;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

import cc.co.evenprime.bukkit.nocheat.events.BlockPlaceEventManager;
import cc.co.evenprime.bukkit.nocheat.events.BlockBreakEventManager;
import cc.co.evenprime.bukkit.nocheat.events.EntityDamageEventManager;
import cc.co.evenprime.bukkit.nocheat.events.EventManager;
import cc.co.evenprime.bukkit.nocheat.events.PlayerChatEventManager;
import cc.co.evenprime.bukkit.nocheat.events.PlayerQuitEventManager;
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
 */
public class NoCheat extends JavaPlugin {

    private ConfigurationManager conf;
    private LogManager           log;
    private DataManager          data;
    private PerformanceManager   performance;
    private ActionManager        action;

    private List<EventManager>   eventManagers;

    private LagMeasureTask       lagMeasureTask;

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

        log.logToConsole(LogLevel.LOW, "[NoCheat] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    public void onEnable() {

        // First set up logging
        this.log = new LogManager();

        log.logToConsole(LogLevel.LOW, "[NoCheat] This version is for CB #1240 and CB #1317. It may break at any time and for any other version.");

        // Then set up in memory per player data storage
        this.data = new DataManager();

        // Then read the configuration files
        this.conf = new ConfigurationManager(this.getDataFolder().getPath());

        // Then set up the performance counters
        this.performance = new PerformanceManager();

        // Then set up the Action Manager
        this.action = new ActionManager(this);

        eventManagers = new ArrayList<EventManager>(8); // Big enough
        // Then set up the event listeners
        eventManagers.add(new PlayerMoveEventManager(this));
        eventManagers.add(new PlayerTeleportEventManager(this));
        eventManagers.add(new PlayerChatEventManager(this));
        eventManagers.add(new BlockBreakEventManager(this));
        eventManagers.add(new BlockPlaceEventManager(this));
        eventManagers.add(new PlayerQuitEventManager(this));
        eventManagers.add(new EntityDamageEventManager(this));

        // Then set up a task to monitor server lag
        if(lagMeasureTask == null) {
            lagMeasureTask = new LagMeasureTask(this);
            lagMeasureTask.start();
        }

        // Then print a list of active checks per world
        printActiveChecks();

        // Tell the server admin that we finished loading NoCheat now
        log.logToConsole(LogLevel.LOW, "[NoCheat] version [" + this.getDescription().getVersion() + "] is enabled.");
    }

    public ConfigurationCache getConfig(Player player) {
        return conf.getConfigurationCacheForWorld(player.getWorld().getName());
    }

    public void log(LogLevel level, String message, ConfigurationCache cc) {
        log.log(level, message, cc);
    }

    public BaseData getData(Player player) {
        return data.getData(player);
    }

    public void clearCriticalData(Player player) {
        data.clearCriticalData(player);
    }

    public void playerLeft(Player player) {
        // Get rid of the critical data that's stored for player immediately
        clearCriticalData(player);

        data.queueForRemoval(player);
    }

    public void playerJoined(Player player) {
        data.unqueueForRemoval(player);
    }

    public Performance getPerformance(Type type) {
        return performance.get(type);
    }

    public void cleanDataMap() {
        if(data != null)
            data.cleanDataMap();
    }

    /**
     * Print the list of active checks to the console, on a per world basis
     */
    private void printActiveChecks() {

        boolean introPrinted = false;
        String intro = "[NoCheat] Active Checks: ";

        // Print active checks for NoCheat, if needed.
        for(World world : this.getServer().getWorlds()) {

            StringBuilder line = new StringBuilder("  ").append(world.getName()).append(": ");

            int length = line.length();

            ConfigurationCache cc = this.conf.getConfigurationCacheForWorld(world.getName());

            if(!cc.debug.showchecks)
                continue;

            for(EventManager em : eventManagers) {
                if(em.getActiveChecks(cc).size() == 0)
                    continue;

                for(String active : em.getActiveChecks(cc)) {
                    line.append(active).append(' ');
                }

                if(!introPrinted) {
                    log.logToConsole(LogLevel.LOW, intro);
                    introPrinted = true;
                }

                log.logToConsole(LogLevel.LOW, line.toString());

                line = new StringBuilder(length);

                for(int i = 0; i < length; i++) {
                    line.append(' ');
                }
            }

        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Not our command
        if(!command.getName().equalsIgnoreCase("nocheat") || args.length == 0)
            return false;

        if(args[0].equalsIgnoreCase("permlist") && args.length >= 2) {
            // permlist command was used
            return handlePermlistCommand(sender, args);

        } else if(args[0].equalsIgnoreCase("reload")) {
            // reload command was used
            return handleReloadCommand(sender);
        }

        else if(args[0].equalsIgnoreCase("performance")) {
            // performance command was used
            return handlePerformanceCommand(sender);
        }

        return false;
    }

    private boolean handlePermlistCommand(CommandSender sender, String[] args) {
        // Does the sender have permission to use it?
        if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_PERMLIST)) {
            return false;
        }

        // Get the player by name
        Player player = this.getServer().getPlayerExact(args[1]);
        if(player == null) {
            sender.sendMessage("Unknown player: " + args[1]);
            return true;
        }

        // Should permissions be filtered by prefix?
        String prefix = "";
        if(args.length == 3) {
            prefix = args[2];
        }

        // Make a copy to allow sorting
        List<Permission> perms = new LinkedList<Permission>(this.getDescription().getPermissions());
        Collections.reverse(perms);

        sender.sendMessage("Player " + player.getName() + " has the permission(s):");

        for(Permission permission : perms) {
            if(permission.getName().startsWith(prefix)) {
                sender.sendMessage(permission.getName() + ": " + player.hasPermission(permission));
            }
        }
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        // Does the sender have permission?
        if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_RELOAD)) {
            return false;
        }

        sender.sendMessage("[NoCheat] Reloading configuration");

        this.conf.cleanup();
        this.conf = new ConfigurationManager(this.getDataFolder().getPath());
        this.data.clearCriticalData();

        sender.sendMessage("[NoCheat] Configuration reloaded");

        return true;
    }

    private boolean handlePerformanceCommand(CommandSender sender) {
        // Does the sender have permission?
        if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_PERFORMANCE)) {
            return false;
        }

        sender.sendMessage("[NoCheat] Retrieving performance statistics");

        long totalTime = 0;

        for(Type type : Type.values()) {
            Performance p = this.getPerformance(type);

            long total = p.getTotalTime();
            totalTime += total;

            StringBuilder string = new StringBuilder("").append(type.toString());
            string.append(": total ").append(Performance.toString(total));
            string.append(", relative ").append(Performance.toString(p.getRelativeTime()));
            string.append(" over ").append(p.getCounter()).append(" events.");

            sender.sendMessage(string.toString());
        }

        sender.sendMessage("Total time spent: " + Performance.toString(totalTime) + " " + Performance.toString(totalTime));

        return true;
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

    public boolean execute(Player player, ActionList actions, int violationLevel, ExecutionHistory history, ConfigurationCache cc) {
        if(action != null) {
            return action.executeActions(player, actions, violationLevel, history, cc);
        }
        return false;
    }

}
