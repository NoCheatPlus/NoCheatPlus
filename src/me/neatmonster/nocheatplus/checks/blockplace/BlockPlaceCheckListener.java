package me.neatmonster.nocheatplus.checks.blockplace;

import java.util.LinkedList;
import java.util.List;

import me.neatmonster.nocheatplus.EventManager;
import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.config.Permissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockPlaceCheckListener implements Listener, EventManager {

    private final ReachCheck     reachCheck;
    private final DirectionCheck directionCheck;
    private final NoCheatPlus    plugin;

    public BlockPlaceCheckListener(final NoCheatPlus plugin) {

        this.plugin = plugin;

        reachCheck = new ReachCheck(plugin);
        directionCheck = new DirectionCheck(plugin);
    }

    @Override
    public List<String> getActiveChecks(final ConfigurationCacheStore cc) {
        final LinkedList<String> s = new LinkedList<String>();

        final BlockPlaceConfig bp = BlockPlaceCheck.getConfig(cc);

        if (bp.reachCheck)
            s.add("blockplace.reach");
        if (bp.directionCheck)
            s.add("blockplace.direction");

        return s;
    }

    /**
     * We listen to BlockPlace events for obvious reasons
     * 
     * @param event
     *            the BlockPlace event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void handleBlockPlaceEvent(final BlockPlaceEvent event) {

        if (event.getBlock() == null || event.getBlockAgainst() == null)
            return;

        boolean cancelled = false;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockPlaceConfig cc = BlockPlaceCheck.getConfig(player);
        final BlockPlaceData data = BlockPlaceCheck.getData(player);

        // Remember these locations and put them in a simpler "format"
        data.blockPlaced.set(event.getBlock());
        data.blockPlacedAgainst.set(event.getBlockAgainst());

        // Now do the actual checks

        // First the reach check
        if (cc.reachCheck && !player.hasPermission(Permissions.BLOCKPLACE_REACH))
            cancelled = reachCheck.check(player, data, cc);

        // Second the direction check
        if (!cancelled && cc.directionCheck && !player.hasPermission(Permissions.BLOCKPLACE_DIRECTION))
            cancelled = directionCheck.check(player, data, cc);

        // If one of the checks requested to cancel the event, do so
        if (cancelled)
            event.setCancelled(cancelled);
    }
}
