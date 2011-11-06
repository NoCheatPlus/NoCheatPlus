package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.BlockBreakCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.DirectionCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.NoswingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockbreak.ReachCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockBreak;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockBreakData;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * Central location to listen to player-interact events and dispatch them to
 * relevant checks
 * 
 */
public class BlockBreakEventManager extends BlockListener implements EventManager {

    private final List<BlockBreakCheck> checks;
    private final NoCheat               plugin;
    private final Performance           blockBreakPerformance;
    private final Performance           blockDamagePerformance;

    public BlockBreakEventManager(NoCheat plugin) {

        this.plugin = plugin;

        // Three checks exist for this event type
        this.checks = new ArrayList<BlockBreakCheck>(3);
        this.checks.add(new DirectionCheck(plugin));
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new ReachCheck(plugin));

        this.blockBreakPerformance = plugin.getPerformance(Type.BLOCKBREAK);
        this.blockDamagePerformance = plugin.getPerformance(Type.BLOCKDAMAGE);

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_BREAK, this, Priority.Lowest, plugin);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, this, Priority.Monitor, plugin);
    }

    private void handleEvent(BlockBreakEvent event) {

        boolean cancelled = false;

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer().getName());

        ConfigurationCache c = player.getConfiguration();

        if(!c.blockbreak.check || player.hasPermission(Permissions.BLOCKBREAK)) {
            return;
        }

        CCBlockBreak cc = player.getConfiguration().blockbreak;
        BlockBreakData data = player.getData().blockbreak;

        data.brokenBlockLocation.set(event.getBlock());
        
        for(BlockBreakCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                check.check(player, data, cc);
            }
        }

        if(cancelled) {
            event.setCancelled(cancelled);
        }

    }

    private void handleEvent(BlockDamageEvent event) {
        // Get the player-specific stored data that applies here
        final BlockBreakData data = plugin.getPlayer(event.getPlayer().getName()).getData().blockbreak;

        // Remember this location. We ignore block breaks in the block-break
        // direction check that are insta-breaks
        data.instaBrokeBlockLocation.set(event.getBlock());
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

        handleEvent(event);

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

        handleEvent(event);

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
        if(cc.blockbreak.check && cc.blockbreak.noswingCheck)
            s.add("blockbreak.noswing");

        return s;
    }
}
