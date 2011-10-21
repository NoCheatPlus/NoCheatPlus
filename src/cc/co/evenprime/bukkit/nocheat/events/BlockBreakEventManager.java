package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.BlockBreakCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * Central location to listen to player-interact events and dispatch them to
 * relevant checks
 * 
 */
public class BlockBreakEventManager extends BlockListener implements EventManager {

    private final BlockBreakCheck blockBreakCheck;
    private final NoCheat         plugin;
    private final Performance     blockBreakPerformance;
    private final Performance     blockDamagePerformance;

    public BlockBreakEventManager(NoCheat plugin) {

        this.plugin = plugin;
        this.blockBreakCheck = new BlockBreakCheck(plugin);
        this.blockBreakPerformance = plugin.getPerformance(Type.BLOCKBREAK);
        this.blockDamagePerformance = plugin.getPerformance(Type.BLOCKDAMAGE);

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.Lowest, plugin);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this, Priority.Monitor, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.isCancelled()) {
            return;
        }

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = blockBreakPerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        final Player player = event.getPlayer();
        final ConfigurationCache cc = plugin.getConfig(player);

        // Find out if checks need to be done for that player
        if(cc.blockbreak.check && !player.hasPermission(Permissions.BLOCKBREAK)) {

            boolean cancel = false;

            cancel = blockBreakCheck.check(player, event.getBlock(), cc);

            if(cancel) {
                event.setCancelled(true);
            }
        }

        // store performance time
        if(performanceCheck)
            blockBreakPerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {

        // Only interested in insta-break events
        if(!event.isCancelled() && !event.getInstaBreak()) {
            return;
        }

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = blockDamagePerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        // Get the player-specific stored data that applies here
        final BaseData data = plugin.getData(event.getPlayer().getName());

        // Remember this location. We ignore block breaks in the block-break
        // direction check that are insta-breaks
        data.blockbreak.instaBrokeBlockLocation.set(event.getBlock());

        // store performance time
        if(performanceCheck)
            blockDamagePerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.blockbreak.check && cc.blockbreak.directionCheck)
            s.add("blockbreak.direction");
        if(cc.blockbreak.check && cc.blockbreak.reachCheck)
            s.add("blockbreak.reach");

        return s;
    }
}
