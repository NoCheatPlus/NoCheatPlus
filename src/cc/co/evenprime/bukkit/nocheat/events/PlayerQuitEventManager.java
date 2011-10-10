package cc.co.evenprime.bukkit.nocheat.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

public class PlayerQuitEventManager extends PlayerListener implements EventManager {

    private final NoCheat plugin;
    public PlayerQuitEventManager(NoCheat plugin) {
        this.plugin = plugin;
        
        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_QUIT, this, Priority.Monitor, plugin);
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Get rid of the players stored data when disconnecting him
        plugin.getDataManager().removeDataForPlayer(event.getPlayer());
    }

    
    public List<String> getActiveChecks(ConfigurationCache cc) {
        return Collections.emptyList();
    }

    public List<String> getInactiveChecks(ConfigurationCache cc) {
        return Collections.emptyList();
    }

}
