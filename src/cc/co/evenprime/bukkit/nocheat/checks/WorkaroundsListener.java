package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.Collections;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;

/**
 * Only place that listens to Player-teleport related events and dispatches them
 * to relevant checks
 * 
 */
public class WorkaroundsListener implements Listener, EventManager {

    //private final NoCheat plugin;

    public WorkaroundsListener(NoCheat plugin) {

        //this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerMove(final PlayerMoveEvent event) {
        // No typo here. I really only handle cancelled events and ignore others
        if(!event.isCancelled())
            return;

        // Fix a common mistake that other developers make (cancelling move
        // events is crazy, rather set the target location to the from location)
        event.setCancelled(false);
        event.setTo(event.getFrom().clone());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void toggleSprint(final PlayerToggleSprintEvent event) {
        if(event.isCancelled() && event.isSprinting()) {
            event.setCancelled(false);
        }
    }

    @Override
    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        return Collections.emptyList();
    }
}
