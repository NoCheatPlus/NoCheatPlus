package cc.co.evenprime.bukkit.nocheat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * Only place that listens to Player-teleport related events and dispatches them
 * to relevant checks
 * 
 */
public class PlayerTeleportEventManager extends EventManager {

    public PlayerTeleportEventManager(NoCheat plugin) {

        super(plugin);

        registerListener(Event.Type.PLAYER_MOVE, Priority.Monitor, false, null);
        registerListener(Event.Type.PLAYER_TELEPORT, Priority.Monitor, true, null);
        registerListener(Event.Type.PLAYER_TELEPORT, Priority.Highest, false, null);
        registerListener(Event.Type.PLAYER_PORTAL, Priority.Monitor, true, null);
        registerListener(Event.Type.PLAYER_RESPAWN, Priority.Monitor, true, null);
    }

    @Override
    protected void handlePlayerTeleportEvent(final PlayerTeleportEvent event, final Priority priority) {
        if(priority.equals(Priority.Monitor)) {
            handleTeleportation(event.getPlayer());
        } else {
            // No typo here, I really want to only handle cancelled events
            if(!event.isCancelled())
                return;

            final MovingData data = plugin.getPlayer(event.getPlayer().getName()).getData().moving;

            if(data.teleportTo.isSet() && data.teleportTo.equals(event.getTo())) {
                event.setCancelled(false);
            }
        }
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
    }

    private void handleTeleportation(final Player player) {
        plugin.clearCriticalData(player.getName());
    }
}
