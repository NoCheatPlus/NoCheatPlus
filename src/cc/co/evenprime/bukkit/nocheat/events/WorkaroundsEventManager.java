package cc.co.evenprime.bukkit.nocheat.events;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;

/**
 * Only place that listens to Player-teleport related events and dispatches them
 * to relevant checks
 * 
 */
public class WorkaroundsEventManager extends EventManagerImpl {

    public WorkaroundsEventManager(NoCheat plugin) {

        super(plugin);

        registerListener(Event.Type.PLAYER_MOVE, Priority.Highest, false, null);
        registerListener(Event.Type.PLAYER_TOGGLE_SPRINT, Priority.Highest, false, null);
        registerListener(Event.Type.PLAYER_TELEPORT, Priority.Monitor, true, null);
        registerListener(Event.Type.PLAYER_PORTAL, Priority.Monitor, true, null);
        registerListener(Event.Type.PLAYER_RESPAWN, Priority.Monitor, true, null);
    }

    @Override
    protected void handlePlayerTeleportEvent(final PlayerTeleportEvent event, final Priority priority) {
        handleTeleportation(event.getPlayer());
    }

    @Override
    protected void handlePlayerPortalEvent(final PlayerPortalEvent event, final Priority priority) {
        handleTeleportation(event.getPlayer());
    }

    @Override
    protected void handlePlayerRespawnEvent(final PlayerRespawnEvent event, final Priority priority) {
        handleTeleportation(event.getPlayer());
    }

    @Override
    protected void handlePlayerMoveEvent(final PlayerMoveEvent event, final Priority priority) {
        // No typo here. I really only handle cancelled events and ignore others
        if(!event.isCancelled())
            return;

        handleTeleportation(event.getPlayer());

        // Fix a common mistake that other developers make (cancelling move
        // events is crazy, rather set the target location to the from location
        if(plugin.getPlayer(event.getPlayer()).getConfiguration().debug.overrideIdiocy) {
            event.setCancelled(false);
            event.setTo(event.getFrom().clone());
        }
    }

    @Override
    protected void handlePlayerToggleSprintEvent(final PlayerToggleSprintEvent event, final Priority priority) {
        if(event.isCancelled() && event.isSprinting()) {
            if(plugin.getPlayer(event.getPlayer()).getConfiguration().debug.overrideIdiocy)
                event.setCancelled(false);
        }
    }

    private void handleTeleportation(final Player player) {
        if(plugin.getPlayer(player).getConfiguration().inventory.closebeforeteleports && player instanceof CraftPlayer) {
            ((CraftPlayer) player).getHandle().closeInventory();
        }
        plugin.clearCriticalData(player.getName());
    }
}
