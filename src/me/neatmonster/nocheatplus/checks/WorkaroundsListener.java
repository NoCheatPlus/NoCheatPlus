package me.neatmonster.nocheatplus.checks;

import java.util.Collections;
import java.util.List;

import me.neatmonster.nocheatplus.EventManager;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

/**
 * Only place that listens to Player-teleport related events and dispatches them
 * to relevant checks
 * 
 */
public class WorkaroundsListener implements Listener, EventManager {

    public WorkaroundsListener() {}

    @Override
    public List<String> getActiveChecks(final ConfigurationCacheStore cc) {
        return Collections.emptyList();
    }

    @EventHandler(
            priority = EventPriority.HIGHEST)
    public void playerMove(final PlayerMoveEvent event) {
        // No typo here. I really only handle cancelled events and ignore others
        if (!event.isCancelled())
            return;

        // Fix a common mistake that other developers make (cancelling move
        // events is crazy, rather set the target location to the from location)
        event.setCancelled(false);
        event.setTo(event.getFrom().clone());
    }

    @EventHandler(
            priority = EventPriority.HIGHEST)
    public void toggleSprint(final PlayerToggleSprintEvent event) {
        // Some plugins cancel "sprinting", which makes no sense at all because
        // it doesn't stop people from sprinting and rewards them by reducing
        // their hunger bar as if they were walking instead of sprinting
        if (event.isCancelled() && event.isSprinting())
            event.setCancelled(false);
    }
}
