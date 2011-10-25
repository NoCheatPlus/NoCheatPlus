package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.timed.TimedCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

public class TimedEventManager implements EventManager {

    private final NoCheat     plugin;

    private final TimedCheck  check;

    private final Performance timedPerformance;

    public TimedEventManager(final NoCheat plugin) {
        this.plugin = plugin;

        check = new TimedCheck(plugin);

        this.timedPerformance = plugin.getPerformance(Type.TIMED);

        // "register a listener" for passed time
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            private int executions = 0;
            private int loopsize   = 10;

            public void run() {

                executions++;

                if(executions >= loopsize) {
                    executions = 0;
                }

                for(Player p : plugin.getServer().getOnlinePlayers()) {
                    if((p.hashCode() & 0x7FFFFFFF) % loopsize == executions) {
                        onTimedEvent(p, loopsize);
                    }
                }
            }
        }, 0, 1);
    }

    public void onTimedEvent(Player player, int elapsedTicks) {

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = timedPerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        ConfigurationCache cc = plugin.getConfig(player);

        if(cc.timed.check && !player.hasPermission(Permissions.TIMED)) {
            check.check(player, elapsedTicks, cc);
        }

        // store performance time
        if(performanceCheck)
            timedPerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.timed.check && cc.timed.godmodeCheck)
            s.add("timed.godmode");
        return s;
    }

}
