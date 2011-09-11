package cc.co.evenprime.bukkit.nocheat.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * Only place that listens to Player-teleport related events and dispatches them
 * to relevant checks
 * 
 * @author Evenprime
 * 
 */
public class PlayerTeleportEventManager extends PlayerListener implements EventManager {

    private final DataManager data;

    public PlayerTeleportEventManager(NoCheat plugin) {

        this.data = plugin.getDataManager();

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_TELEPORT, this, Priority.Monitor, plugin);
        pm.registerEvent(Event.Type.PLAYER_PORTAL, this, Priority.Monitor, plugin);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, this, Priority.Monitor, plugin);

        // This belongs to the move-check
        // Override decision to cancel teleports initialized by NoCheat by
        // uncancelling them
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, new PlayerListener() {

            @Override
            public void onPlayerTeleport(PlayerTeleportEvent event) {
                final MovingData data2 = data.getMovingData(event.getPlayer());
                if(event.isCancelled()) {
                    if(data2.teleportTo != null && data2.teleportTo.equals(event.getTo())) {
                        event.setCancelled(false);
                    }
                }
            }
        }, Priority.Highest, plugin);
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(!event.isCancelled())
            handleTeleportation(event.getPlayer(), event.getTo());

    }

    public void onPlayerPortal(PlayerPortalEvent event) {
        if(!event.isCancelled())
            handleTeleportation(event.getPlayer(), event.getTo());
    }

    public void onPlayerRespawn(PlayerRespawnEvent event) {
        handleTeleportation(event.getPlayer(), event.getRespawnLocation());
    }

    private void handleTeleportation(Player player, Location newLocation) {

        /********* Moving check ************/
        final MovingData data = this.data.getMovingData(player);

        data.movingsetBackPoint = null;
        data.morePacketsCounter = 0;
        data.morePacketsSetbackPoint = null;
        data.jumpPhase = 0;
        
        if(newLocation != null) {

            data.noclipX = newLocation.getBlockX();
            data.noclipY = Location.locToBlock(newLocation.getY()+1.1D);
            data.noclipZ = newLocation.getBlockZ();
            
        }
    }

    @Override
    public List<String> getActiveChecks(ConfigurationCache cc) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getInactiveChecks(ConfigurationCache cc) {
        return Collections.emptyList();
    }
}
