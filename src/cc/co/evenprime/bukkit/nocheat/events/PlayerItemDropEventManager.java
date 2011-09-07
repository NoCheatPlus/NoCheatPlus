package cc.co.evenprime.bukkit.nocheat.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;
import cc.co.evenprime.bukkit.nocheat.log.LogManager;

/**
 * 
 * Temporary, until Bukkit implements a real fix for the problem.
 * 
 * @author Evenprime
 * 
 */
public class PlayerItemDropEventManager extends PlayerListener {

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
}
