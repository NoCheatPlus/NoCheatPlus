package cc.co.evenprime.bukkit.nocheat.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

/**
 * Only place that listens to Player-teleport related events and dispatches them
 * to relevant checks
 * 
 */
public class PlayerTeleportEventManager extends PlayerListener implements EventManager {

    private final NoCheat plugin;

    public PlayerTeleportEventManager(NoCheat p) {

        this.plugin = p;

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_MOVE, this, Priority.Monitor, plugin);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, this, Priority.Monitor, plugin);
        pm.registerEvent(Event.Type.PLAYER_PORTAL, this, Priority.Monitor, plugin);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, this, Priority.Monitor, plugin);

        // This belongs to the move-check
        // Override decision to cancel teleports initialized by NoCheat by
        // uncancelling them, if possible
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, new PlayerListener() {

            @Override
            public void onPlayerTeleport(PlayerTeleportEvent event) {
                if(!event.isCancelled()) {
                    return;
                }

                final BaseData data = plugin.getData(event.getPlayer().getName());

                if(data.moving.teleportTo.isSet() && data.moving.teleportTo.equals(event.getTo())) {
                    event.setCancelled(false);
                }
            }
        }, Priority.Highest, plugin);
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.isCancelled())
            return;

        handleTeleportation(event.getPlayer().getName());
    }

    public void onPlayerPortal(PlayerPortalEvent event) {
        if(event.isCancelled())
            return;

        handleTeleportation(event.getPlayer().getName());
    }

    public void onPlayerRespawn(PlayerRespawnEvent event) {
        handleTeleportation(event.getPlayer().getName());
    }

    // Workaround for buggy Playermove cancelling
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!event.isCancelled()) {
            return;
        }

        handleTeleportation(event.getPlayer().getName());
    }

    private void handleTeleportation(String playerName) {

        plugin.clearCriticalData(playerName);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        return Collections.emptyList();
    }
}
