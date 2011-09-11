package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.BlockPlaceCheck;
import cc.co.evenprime.bukkit.nocheat.checks.moving.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 * @author Evenprime
 * 
 */
public class BlockPlaceEventManager extends BlockListener implements EventManager {

    private final MovingCheck          movingCheck;
    private final BlockPlaceCheck      blockPlaceCheck;

    private final DataManager          data;
    private final ConfigurationManager config;

    public BlockPlaceEventManager(NoCheat plugin) {

        this.data = plugin.getDataManager();
        this.config = plugin.getConfigurationManager();

        this.movingCheck = new MovingCheck(plugin);
        this.blockPlaceCheck = new BlockPlaceCheck(plugin);

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_PLACE, this, Priority.Lowest, plugin);

        // This is part of a workaround for the moving check
        pm.registerEvent(Event.Type.BLOCK_PLACE, new BlockListener() {

            @Override
            public void onBlockPlace(BlockPlaceEvent event) {
                if(!event.isCancelled()) {
                    final Player player = event.getPlayer();
                    // Get the player-specific stored data that applies here
                    movingCheck.blockPlaced(player, data.getMovingData(player), event.getBlockPlaced());
                }
            }
        }, Priority.Monitor, plugin);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!event.isCancelled()) {

            boolean cancel = false;

            final Player player = event.getPlayer();
            final ConfigurationCache cc = config.getConfigurationCacheForWorld(player.getWorld().getName());

            // Find out if checks need to be done for that player
            if(cc.blockplace.check && !player.hasPermission(Permissions.BLOCKPLACE)) {

                cancel = blockPlaceCheck.check(player, event.getBlockPlaced(), event.getBlockAgainst(), data.getBlockPlaceData(player), cc);
            }

            if(cancel) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.blockplace.check && cc.blockplace.onliquidCheck)
            s.add("blockplace.onliquid");
        if(cc.blockplace.check && cc.blockplace.reachCheck)
            s.add("blockplace.reach");

        return s;
    }

    @Override
    public List<String> getInactiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(!(cc.blockplace.check && cc.blockplace.onliquidCheck))
            s.add("blockplace.onliquid");
        if(!(cc.blockplace.check && cc.blockplace.reachCheck))
            s.add("blockplace.reach");

        return s;
    }
}
