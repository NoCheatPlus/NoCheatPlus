package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.CraftServer;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.TimedCheck;
import cc.co.evenprime.bukkit.nocheat.checks.timed.GodmodeCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCTimed;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.TimedData;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

public class TimedEventManager implements EventManager {

    private final List<TimedCheck> checks;
    private final Performance      timedPerformance;
    public final int               taskId;

    public TimedEventManager(final NoCheat plugin) {

        checks = new ArrayList<TimedCheck>(1);
        checks.add(new GodmodeCheck(plugin));

        this.timedPerformance = plugin.getPerformance(Type.TIMED);

        // "register a listener" for passed time
        this.taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

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

                        // Performance counter setup
                        long nanoTimeStart = 0;
                        final boolean performanceCheck = timedPerformance.isEnabled();

                        if(performanceCheck)
                            nanoTimeStart = System.nanoTime();

                        handleEvent(plugin.getPlayer(p.name));

                        // store performance time
                        if(performanceCheck)
                            timedPerformance.addTime(System.nanoTime() - nanoTimeStart);

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                // Clear the list for next time
                entities.clear();
            }
        }, 0, 1);
    }

    private void handleEvent(NoCheatPlayer player) {

        TimedData data = player.getData().timed;
        CCTimed cc = player.getConfiguration().timed;

        if(!cc.check || player.hasPermission(Permissions.TIMED)) {
            return;
        }

        for(TimedCheck check : checks) {
            if(cc.check && !player.hasPermission(Permissions.TIMED)) {
                check.check(player, data, cc);
            }
        }
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.timed.check && cc.timed.godmodeCheck)
            s.add("timed.godmode");
        return s;
    }

}
