package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.checks.interact.InteractCheck;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;
import cc.co.evenprime.bukkit.nocheat.data.InteractData;

/**
 * 
 * @author Evenprime
 * 
 */
public class PlayerInteractEventManager extends PlayerListener  implements EventManager {

    private final InteractCheck        interactCheck;
    private final DataManager          data;
    private final ConfigurationManager config;

    public PlayerInteractEventManager(NoCheat plugin) {

        this.data = plugin.getDataManager();
        this.config = plugin.getConfigurationManager();
        this.interactCheck = new InteractCheck(plugin);

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Priority.Lowest, plugin);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {

        if(event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ConfigurationCache cc = config.getConfigurationCacheForWorld(player.getWorld().getName());

        // Find out if checks need to be done for that player
        if(cc.interact.check && !player.hasPermission(Permissions.INTERACT)) {

            boolean cancel = false;

            // Get the player-specific stored data that applies here
            final InteractData data = this.data.getInteractData(player);

            cancel = interactCheck.check(player, data, cc);

            if(cancel) {
                event.setCancelled(true);
            }
        }

    }

    @Override
    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.interact.check && cc.interact.durabilityCheck)
            s.add("interact.durability");
        return s;
    }

    @Override
    public List<String> getInactiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(!(cc.interact.check && cc.interact.durabilityCheck))
            s.add("interact.durability");

        return s;
    }

}
