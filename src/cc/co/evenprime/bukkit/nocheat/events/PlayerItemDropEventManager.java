package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

/**
 * 
 * Temporary, until Bukkit implements a real fix for the problem.
 * 
 * @author Evenprime
 * 
 */
public class PlayerItemDropEventManager extends PlayerListener implements EventManager {

    public PlayerItemDropEventManager(NoCheat plugin) {

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Priority.Lowest, plugin);

    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        if(!event.getPlayer().isOnline()) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<String> getActiveChecks(ConfigurationCache cc) {
        return new LinkedList<String>();
    }

    @Override
    public List<String> getInactiveChecks(ConfigurationCache cc) {
        return new LinkedList<String>();
    }
}
