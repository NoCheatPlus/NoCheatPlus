package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.BlockPlaceCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.DirectionCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.ReachCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockPlace;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockPlaceEventManager extends EventManagerImpl {

    private final List<BlockPlaceCheck> checks;

    public BlockPlaceEventManager(NoCheat plugin) {

        super(plugin);

        this.checks = new ArrayList<BlockPlaceCheck>(2);
        this.checks.add(new ReachCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));

        registerListener(Event.Type.BLOCK_PLACE, Priority.Lowest, true, plugin.getPerformance(Type.BLOCKPLACE));
    }

    @Override
    protected void handleBlockPlaceEvent(BlockPlaceEvent event, Priority priority) {

        if(event.getBlock() == null || event.getBlockAgainst() == null)
            return;

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCBlockPlace cc = player.getConfiguration().blockplace;

        if(!cc.check || player.hasPermission(Permissions.BLOCKPLACE)) {
            return;
        }

        final BlockPlaceData data = player.getData().blockplace;

        data.blockPlaced.set(event.getBlock());
        data.blockPlacedAgainst.set(event.getBlockAgainst());

        for(BlockPlaceCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                cancelled = check.check(player, data, cc);
            }
        }

        if(cancelled)
            event.setCancelled(cancelled);
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.blockplace.check && cc.blockplace.reachCheck)
            s.add("blockplace.reach");

        return s;
    }
}
