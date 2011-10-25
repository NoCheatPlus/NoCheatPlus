package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
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

    public int                taskId = -1;

    public TimedEventManager(final NoCheat plugin) {
        this.plugin = plugin;

        check = new TimedCheck(plugin);

        this.timedPerformance = plugin.getPerformance(Type.TIMED);

        // "register a listener" for passed time
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            private int                      executions = 0;
            private int                      loopsize   = 10;

            private final List<EntityPlayer> entities   = new ArrayList<EntityPlayer>(20);

            @SuppressWarnings("unchecked")
            public void run() {

                executions++;

                if(executions >= loopsize) {
                    executions = 0;
                }

                // For performance reasons, we take some shortcuts here
                CraftServer server = (CraftServer) plugin.getServer();

                try {
                    // Only collect the entities that we want to check this time
                    for(EntityPlayer p : (List<EntityPlayer>) server.getHandle().players) {
                        if(p.id % loopsize == executions) {
                            entities.add(p);
                        }
                    }
                } catch(ConcurrentModificationException e) {
                    // Bad luck, better luck next time
                } catch(Exception e) {
                    e.printStackTrace();
                }

                // Now initialize the checks one by one
                for(EntityPlayer p : entities) {
                    try {
                        onTimedEvent((Player) CraftEntity.getEntity(server, p), loopsize);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                // Clear the list for next time
                entities.clear();
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
