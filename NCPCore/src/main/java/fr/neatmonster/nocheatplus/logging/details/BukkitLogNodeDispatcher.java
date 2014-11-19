package fr.neatmonster.nocheatplus.logging.details;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.components.TickListener;
import fr.neatmonster.nocheatplus.utilities.TickTask;

public class BukkitLogNodeDispatcher extends AbstractLogNodeDispatcher { // TODO: Name.
    
    
    /**
     * Permanent TickListener for logging [TODO: on-demand scheduling, but thread-safe. With extra lock.]
     */
    private final TickListener taskPrimary = new TickListener() {
        
        @Override
        public void onTick(final int tick, final long timeLast) {
            if (runLogsPrimary()) {
                // TODO: Here or within runLogsPrimary, handle rescheduling.
            }
        }
        
    };
    
    /**
     * Needed for scheduling.
     */
    private final Plugin plugin;
    
    public BukkitLogNodeDispatcher(Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void startTasks() {
        // TODO: This is a temporary solution. Needs on-demand scheduling [or a wrapper task].
        TickTask.addTickListener(taskPrimary);
        scheduleAsynchronous(); // Just in case.
    }
    
    @Override
    protected void scheduleAsynchronous() {
        synchronized (queueAsynchronous) {
            if (taskAsynchronousID == -1) {
                // Deadlocking should not be possible.
                taskAsynchronousID = Bukkit.getScheduler().runTaskAsynchronously(plugin, taskAsynchronous).getTaskId();
                // TODO: Re-check task id here.
            }
        }
    }

    @Override
    protected final boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    protected void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

}
