package cc.co.evenprime.bukkit.nocheat;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import cc.co.evenprime.bukkit.nocheat.actions.ActionManager;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;

import cc.co.evenprime.bukkit.nocheat.events.BlockPlaceEventManager;
import cc.co.evenprime.bukkit.nocheat.events.BlockBreakEventManager;
import cc.co.evenprime.bukkit.nocheat.events.EventManager;
import cc.co.evenprime.bukkit.nocheat.events.PlayerChatEventManager;
import cc.co.evenprime.bukkit.nocheat.events.PlayerItemDropEventManager;
import cc.co.evenprime.bukkit.nocheat.events.PlayerInteractEventManager;
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

    private ConfigurationManager conf;
    private LogManager           log;
    private DataManager          data;

    private List<EventManager>   eventManagers            = new LinkedList<EventManager>();

    private int                  taskId                   = -1;
    private int                  ingameseconds            = 0;
    private long                 lastIngamesecondTime     = 0L;
    private long                 lastIngamesecondDuration = 0L;
    private boolean              skipCheck                = false;

    private ActionManager        action;

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
        this.log = new LogManager(this);
        this.data = new DataManager();

        this.action = new ActionManager(log);

        // parse the nocheat.yml config file
        this.conf = new ConfigurationManager(this.getDataFolder().getPath(), action);

        eventManagers.add(new PlayerMoveEventManager(this));
        eventManagers.add(new PlayerTeleportEventManager(this));
        eventManagers.add(new PlayerItemDropEventManager(this));
        eventManagers.add(new PlayerInteractEventManager(this));
        eventManagers.add(new PlayerChatEventManager(this));
        eventManagers.add(new BlockBreakEventManager(this));
        eventManagers.add(new BlockPlaceEventManager(this));

        PluginDescriptionFile pdfFile = this.getDescription();

        if(taskId == -1) {
            taskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

                @Override
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
                }
            }, 0, 20);
        }

        printActiveChecks();

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
}
