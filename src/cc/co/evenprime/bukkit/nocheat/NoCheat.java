package cc.co.evenprime.bukkit.nocheat;

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
import cc.co.evenprime.bukkit.nocheat.data.DataManager;
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
 * 
 * @author Evenprime
 */
public class NoCheat extends JavaPlugin {

    private ConfigurationManager     conf;
    private LogManager               log;
    private DataManager              data;
    private PerformanceManager       performance;
    private ActionManager            action;

    private final List<EventManager> eventManagers            = new LinkedList<EventManager>();

    private int                      taskId                   = -1;
    private int                      ingameseconds            = 0;
    private long                     lastIngamesecondTime     = 0L;
    private long                     lastIngamesecondDuration = 0L;
    private boolean                  skipCheck                = false;

    public NoCheat() {

    }

    public void onDisable() {

        if(taskId != -1) {
            this.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        PluginDescriptionFile pdfFile = this.getDescription();

        if(conf != null)
            conf.cleanup();

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

        // Then set up the event listeners
        eventManagers.add(new PlayerMoveEventManager(this));
        eventManagers.add(new PlayerTeleportEventManager(this));
        eventManagers.add(new PlayerChatEventManager(this));
        eventManagers.add(new BlockBreakEventManager(this));
        eventManagers.add(new BlockPlaceEventManager(this));
        eventManagers.add(new PlayerQuitEventManager(this));
        eventManagers.add(new EntityDamageEventManager(this));

        // Then set up a task to monitor server lag
        if(taskId == -1) {
            taskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

                public void run() {

                    // If the previous second took to long, skip checks during
                    // this second
                    skipCheck = lastIngamesecondDuration > 1500;

                    long time = System.currentTimeMillis();
                    lastIngamesecondDuration = time - lastIngamesecondTime;
                    if(lastIngamesecondDuration < 1000)
                        lastIngamesecondDuration = 1000;
                    lastIngamesecondTime = time;
                    ingameseconds++;

                    // Check if some data is outdated now and let it be removed
                    getDataManager().cleanDataMap();
                }
            }, 0, 20);
        }

        // Then print a list of active checks per world
        printActiveChecks();

        // Tell the server admin that we finished loading NoCheat now
        log.logToConsole(LogLevel.LOW, "[NoCheat] version [" + this.getDescription().getVersion() + "] is enabled.");
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

    public PerformanceManager getPerformanceManager() {
        return performance;
    }

    public ActionManager getActionManager() {
        return action;
    }

    public int getIngameSeconds() {
        return ingameseconds;
    }

    public long getIngameSecondDuration() {
        return lastIngamesecondDuration;
    }

    public boolean skipCheck() {
        return skipCheck;
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

            if(cc.debug.showchecks) {
                for(EventManager em : eventManagers) {
                    List<String> checks = em.getActiveChecks(cc);
                    if(checks.size() > 0) {
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
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getName().equalsIgnoreCase("nocheat") && args.length > 0) {
            if(args[0].equalsIgnoreCase("permlist") && args.length >= 2) {
                // permlist command was used CORRECTLY

                // Does the sender have permission?
                if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_PERMLIST)) {
                    return false;
                }

                // Get the player names
                Player player = this.getServer().getPlayerExact(args[1]);
                if(player == null) {
                    sender.sendMessage("Unknown player: " + args[1]);
                    return true;
                } else {
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
            } else if(args[0].equalsIgnoreCase("reload")) {
                // reload command was used

                // Does the sender have permission?
                if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_RELOAD)) {
                    return false;
                }

                sender.sendMessage("[NoCheat] Reloading configuration");

                this.conf.cleanup();
                this.conf = new ConfigurationManager(this.getDataFolder().getPath());
                this.data.resetAllCriticalData();

                sender.sendMessage("[NoCheat] Configuration reloaded");

                return true;
            }

            else if(args[0].equalsIgnoreCase("performance")) {
                // performance command was used

                // Does the sender have permission?
                if(sender instanceof Player && !sender.hasPermission(Permissions.ADMIN_PERFORMANCE)) {
                    return false;
                }

                sender.sendMessage("[NoCheat] Retrieving performance statistics");

                PerformanceManager pm = this.getPerformanceManager();
                long totalTime = 0;

                for(Type type : Type.values()) {
                    Performance p = pm.get(type);

                    long total = p.getTotalTime();
                    totalTime += total;
                    long relative = p.getRelativeTime();
                    long events = p.getCounter();

                    StringBuilder string = new StringBuilder("").append(type.toString());
                    string.append(": total ").append(pm.convertToAppropriateUnit(total)).append(" ").append(pm.getAppropriateUnit(total));
                    string.append(", relative ").append(pm.convertToAppropriateUnit(relative)).append(" ").append(pm.getAppropriateUnit(relative));
                    string.append(" over ").append(events).append(" events.");

                    sender.sendMessage(string.toString());
                }

                sender.sendMessage("Total time spent: " + pm.convertToAppropriateUnit(totalTime) + " " + pm.getAppropriateUnit(totalTime));

                return true;
            }
        }
        return false;
    }

}
