package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

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

    private final NoswingCheck   noswingCheck;
    private final ReachCheck     reachCheck;
    private final DirectionCheck directionCheck;
    private final NoCheat        plugin;

    public BlockBreakCheckListener(NoCheat plugin) {

        noswingCheck = new NoswingCheck(plugin);
        reachCheck = new ReachCheck(plugin);
        directionCheck = new DirectionCheck(plugin);

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockBreak(final BlockBreakEvent event) {

        if(event.isCancelled())
            return;

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockBreakConfig cc = BlockBreakCheck.getConfig(player.getConfigurationStore());

        final BlockBreakData data = BlockBreakCheck.getData(player.getDataStore());

        data.brokenBlockLocation.set(event.getBlock());

        // Only if the block got damaged before, do the check(s)
        if(!data.brokenBlockLocation.equals(data.lastDamagedBlock)) {
            // Something caused a blockbreak event that's not from the player
            // Don't check it at all
            data.lastDamagedBlock.reset();
            return;
        }

        // Now do the actual checks, if still needed
        if(cc.noswingCheck && !player.hasPermission(Permissions.BLOCKBREAK_NOSWING)) {
            cancelled = noswingCheck.check(player, data, cc);
        }
        if(!cancelled && cc.reachCheck && !player.hasPermission(Permissions.BLOCKBREAK_REACH)) {
            cancelled = reachCheck.check(player, data, cc);
        }
        if(!cancelled && cc.directionCheck && !player.hasPermission(Permissions.BLOCKBREAK_DIRECTION)) {
            cancelled = directionCheck.check(player, data, cc);
        }

        if(cancelled)
            event.setCancelled(cancelled);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockHit(final BlockDamageEvent event) {

        if(event.isCancelled())
            return;

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

        BlockBreakConfig bb = BlockBreakCheck.getConfig(cc);

        if(bb.directionCheck)
            s.add("blockbreak.direction");
        if(bb.reachCheck)
            s.add("blockbreak.reach");
        if(bb.noswingCheck)
            s.add("blockbreak.noswing");

        return s;
    }
}
