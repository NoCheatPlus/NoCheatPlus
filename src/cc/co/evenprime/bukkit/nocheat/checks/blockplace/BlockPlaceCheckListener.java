package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockPlaceCheckListener implements Listener, EventManager {

    private final ReachCheck     reachCheck;
    private final DirectionCheck directionCheck;
    private final NoCheat        plugin;

    public BlockPlaceCheckListener(NoCheat plugin) {

        this.plugin = plugin;

        reachCheck = new ReachCheck(plugin);
        directionCheck = new DirectionCheck(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    protected void handleBlockPlaceEvent(BlockPlaceEvent event) {

        if(event.isCancelled())
            return;

        if(event.getBlock() == null || event.getBlockAgainst() == null)
            return;

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final BlockPlaceConfig cc = BlockPlaceCheck.getConfig(player.getConfigurationStore());

        final BlockPlaceData data = BlockPlaceCheck.getData(player.getDataStore());

        data.blockPlaced.set(event.getBlock());
        data.blockPlacedAgainst.set(event.getBlockAgainst());

        // Now do the actual checks
        if(cc.reachCheck && !player.hasPermission(Permissions.BLOCKPLACE_REACH)) {
            cancelled = reachCheck.check(player, data, cc);
        }
        if(!cancelled && cc.directionCheck && !player.hasPermission(Permissions.BLOCKPLACE_DIRECTION)) {
            cancelled = directionCheck.check(player, data, cc);
        }

        if(cancelled)
            event.setCancelled(cancelled);
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        BlockPlaceConfig bp = BlockPlaceCheck.getConfig(cc);

        if(bp.reachCheck)
            s.add("blockplace.reach");
        if(bp.directionCheck)
            s.add("blockplace.direction");

        return s;
    }
}
