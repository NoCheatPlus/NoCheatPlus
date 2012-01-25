package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

/**
 * Central location to listen to events that are 
 * relevant for the blockbreak checks
 * 
 */
public class BlockBreakCheckListener implements Listener, EventManager {

    private final List<BlockBreakCheck> checks;
    private final NoCheat plugin;

    public BlockBreakCheckListener(NoCheat plugin) {

        // Three checks exist for this event type
        this.checks = new ArrayList<BlockBreakCheck>(3);
        this.checks.add(new NoswingCheck(plugin));
        this.checks.add(new ReachCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));
        
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockBreak(final BlockBreakEvent event) {

        if(event.isCancelled()) return;

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCBlockBreak cc = BlockBreakCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.BLOCKBREAK)) {
            return;
        }

        final BlockBreakData data = BlockBreakCheck.getData(player.getDataStore());

        data.brokenBlockLocation.set(event.getBlock());

        // Only if the block got damaged before, do the check(s)
        if(!data.brokenBlockLocation.equals(data.lastDamagedBlock)) {
            // Something caused a blockbreak event that's not from the player
            // Don't check it at all
            data.lastDamagedBlock.reset();
            return;
        }

        for(BlockBreakCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                cancelled = check.check(player, data, cc);
            }
        }

        if(cancelled)
            event.setCancelled(cancelled);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockHit(final BlockDamageEvent event) {

        if(event.isCancelled()) return;
        
        NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        BlockBreakData data = BlockBreakCheck.getData(player.getDataStore());

        // Only interested in insta-break events here
        if(event.getInstaBreak()) {
            // Remember this location. We ignore block breaks in the block-break
            // direction check that are insta-breaks
            data.instaBrokenBlockLocation.set(event.getBlock());
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockInteract(final PlayerInteractEvent event) {

        if(event.getClickedBlock() == null)
            return;

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        BlockBreakData data = BlockBreakCheck.getData(player.getDataStore());
        // Remember this location. Only blockbreakevents for this specific block
        // will be handled at all
        data.lastDamagedBlock.set(event.getClickedBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void armSwing(final PlayerAnimationEvent event) {
        BlockBreakCheck.getData(plugin.getPlayer(event.getPlayer()).getDataStore()).armswung = true;
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        CCBlockBreak bb = BlockBreakCheck.getConfig(cc);

        if(bb.check && bb.directionCheck)
            s.add("blockbreak.direction");
        if(bb.check && bb.reachCheck)
            s.add("blockbreak.reach");
        if(bb.check && bb.noswingCheck)
            s.add("blockbreak.noswing");

        return s;
    }
}
