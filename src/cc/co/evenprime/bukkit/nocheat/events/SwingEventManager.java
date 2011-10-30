package cc.co.evenprime.bukkit.nocheat.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

/**
 * The only place that listens to and modifies player_move events if necessary
 * 
 * Get the event, decide which checks should work on it and in what order,
 * evaluate the check results and decide what to
 * 
 */
public class SwingEventManager extends PlayerListener implements EventManager {

    private final NoCheat plugin;

    public SwingEventManager(NoCheat plugin) {

        this.plugin = plugin;

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_ANIMATION, this, Priority.Lowest, plugin);
    }

    @Override
    public void onPlayerAnimation(final PlayerAnimationEvent event) {
        plugin.getData(event.getPlayer().getName()).armswung = true;
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        return Collections.emptyList();
    }
}
