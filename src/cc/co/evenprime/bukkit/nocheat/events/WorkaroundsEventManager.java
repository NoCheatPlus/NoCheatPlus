package cc.co.evenprime.bukkit.nocheat.events;

import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;

/**
 * Only place that listens to Player-teleport related events and dispatches them
 * to relevant checks
 * 
 */
public class WorkaroundsEventManager implements Listener, EventManager {

    private final NoCheat plugin;

    public WorkaroundsEventManager(NoCheat plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void teleport(final PlayerTeleportEvent event) {
        if(event.isCancelled())
            return;
        handleTeleportation(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void portal(final PlayerPortalEvent event) {
        if(event.isCancelled())
            return;
        handleTeleportation(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void respawn(final PlayerRespawnEvent event) {
        handleTeleportation(event.getPlayer(), event.getRespawnLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerMove(final PlayerMoveEvent event) {
        // No typo here. I really only handle cancelled events and ignore others
        if(!event.isCancelled())
            return;

        handleTeleportation(event.getPlayer(), event.getTo());

        // Fix a common mistake that other developers make (cancelling move
        // events is crazy, rather set the target location to the from location)
        if(plugin.getPlayer(event.getPlayer()).getConfigurationStore().debug.overrideIdiocy) {
            event.setCancelled(false);
            event.setTo(event.getFrom().clone());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void toggleSprint(final PlayerToggleSprintEvent event) {
        if(event.isCancelled() && event.isSprinting()) {
            if(plugin.getPlayer(event.getPlayer()).getConfigurationStore().debug.overrideIdiocy)
                event.setCancelled(false);
        }
    }

    private void handleTeleportation(final Player player, final Location to) {
        plugin.clearCriticalData(player.getName());
    }

    @Override
    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        return Collections.emptyList();
    }
}
