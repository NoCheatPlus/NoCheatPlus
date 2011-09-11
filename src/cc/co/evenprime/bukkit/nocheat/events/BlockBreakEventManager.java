package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.BlockBreakCheck;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;

/**
 * Central location to listen to player-interact events and dispatch them to
 * relevant checks
 * 
 * @author Evenprime
 * 
 */
public class BlockBreakEventManager extends BlockListener implements EventManager {

    private final BlockBreakCheck      blockBreakCheck;
    private final DataManager          data;
    private final ConfigurationManager config;

    public BlockBreakEventManager(NoCheat plugin) {

        this.data = plugin.getDataManager();
        this.config = plugin.getConfigurationManager();

        this.blockBreakCheck = new BlockBreakCheck(plugin);

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.Lowest, plugin);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this, Priority.Monitor, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ConfigurationCache cc = config.getConfigurationCacheForWorld(player.getWorld().getName());

        // Find out if checks need to be done for that player
        if(cc.blockbreak.check && !player.hasPermission(Permissions.BLOCKBREAK)) {

            boolean cancel = false;

            // Get the player-specific stored data that applies here
            final BlockBreakData data = this.data.getBlockBreakData(player);

            cancel = blockBreakCheck.check(player, event.getBlock(), data, cc);

            if(cancel) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {

        // Only interested in insta-break events
        if(!event.isCancelled() && !event.getInstaBreak()) {
            return;
        }

        final Player player = event.getPlayer();
        // Get the player-specific stored data that applies here
        final BlockBreakData data = this.data.getBlockBreakData(player);

        // Remember this location. We ignore block breaks in the block-break
        // direction check that are insta-breaks
        data.instaBrokeBlockLocation = event.getBlock().getLocation();
    }

    @Override
    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.blockbreak.check && cc.blockbreak.directionCheck)
            s.add("blockbreak.direction");
        if(cc.blockbreak.check && cc.blockbreak.reachCheck)
            s.add("blockbreak.reach");

        return s;
    }

    @Override
    public List<String> getInactiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();
        
        if(!(cc.blockbreak.check && cc.blockbreak.directionCheck))
            s.add("blockbreak.direction");
        if(!(cc.blockbreak.check && cc.blockbreak.reachCheck))
            s.add("blockbreak.reach");

        return s;
    }
}
