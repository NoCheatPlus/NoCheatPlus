package cc.co.evenprime.bukkit.nocheat.events;

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

        registerListener(Event.Type.PLAYER_MOVE, Priority.Monitor, false);
        registerListener(Event.Type.PLAYER_TELEPORT, Priority.Monitor, true);
        registerListener(Event.Type.PLAYER_TELEPORT, Priority.Highest, false);
        registerListener(Event.Type.PLAYER_PORTAL, Priority.Monitor, true);
        registerListener(Event.Type.PLAYER_RESPAWN, Priority.Monitor, true);
    }

    @Override
    protected void handlePlayerTeleportEvent(PlayerTeleportEvent event, Priority priority) {
        if(priority.equals(Priority.Monitor)) {
            handleTeleportation(event.getPlayer().getName());
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
    protected void handlePlayerPortalEvent(PlayerPortalEvent event, Priority priority) {
        handleTeleportation(event.getPlayer().getName());
    }

    @Override
    protected void handlePlayerRespawnEvent(PlayerRespawnEvent event, Priority priority) {
        handleTeleportation(event.getPlayer().getName());
    }

    @Override
    protected void handlePlayerMoveEvent(PlayerMoveEvent event, Priority priority) {
        // No typo here. I really only handle cancelled events and ignore others
        if(!event.isCancelled())
            return;

        handleTeleportation(event.getPlayer().getName());
    }

    private void handleTeleportation(String playerName) {

        plugin.clearCriticalData(playerName);
    }
}
