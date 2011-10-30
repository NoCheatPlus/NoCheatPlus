package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.BlockPlaceCheck;
import cc.co.evenprime.bukkit.nocheat.checks.moving.RunFlyCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockPlaceEventManager extends BlockListener implements EventManager {

    private final NoCheat         plugin;
    private final RunFlyCheck     movingCheck;
    private final BlockPlaceCheck blockPlaceCheck;

    private final Performance     blockPlacePerformance;

    public BlockPlaceEventManager(NoCheat p) {

        this.plugin = p;

        this.movingCheck = new RunFlyCheck(plugin);
        this.blockPlaceCheck = new BlockPlaceCheck(plugin);

        this.blockPlacePerformance = p.getPerformance(Type.BLOCKPLACE);

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_PLACE, this, Priority.Lowest, plugin);

        // This is part of a workaround for the moving check
        pm.registerEvent(Event.Type.BLOCK_PLACE, new BlockListener() {

            @Override
            public void onBlockPlace(BlockPlaceEvent event) {
                if(event.isCancelled())
                    return;

                final Player player = event.getPlayer();
                // Get the player-specific stored data that applies here
                movingCheck.blockPlaced(player, event.getBlockPlaced());

            }
        }, Priority.Monitor, plugin);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {

        if(event.isCancelled())
            return;

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = blockPlacePerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        boolean cancel = false;

        final Player player = event.getPlayer();
        final ConfigurationCache cc = plugin.getConfig(player);

        // Find out if checks need to be done for that player
        if(cc.blockplace.check && !player.hasPermission(Permissions.BLOCKPLACE)) {
            cancel = blockPlaceCheck.check(player, event.getBlockPlaced(), event.getBlockAgainst(), cc);
        }

        if(cancel) {
            event.setCancelled(true);
        }

        // store performance time
        if(performanceCheck)
            blockPlacePerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.blockplace.check && cc.blockplace.onliquidCheck)
            s.add("blockplace.onliquid");
        if(cc.blockplace.check && cc.blockplace.reachCheck)
            s.add("blockplace.reach");

        return s;
    }
}
