package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
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

    private final List<BlockPlaceCheck> checks;
    private final NoCheat               plugin;

    public BlockPlaceCheckListener(NoCheat plugin) {

        this.plugin = plugin;

        this.checks = new ArrayList<BlockPlaceCheck>(2);
        this.checks.add(new ReachCheck(plugin));
        this.checks.add(new DirectionCheck(plugin));
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    protected void handleBlockPlaceEvent(BlockPlaceEvent event) {

        if(event.isCancelled())
            return;

        if(event.getBlock() == null || event.getBlockAgainst() == null)
            return;

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCBlockPlace cc = BlockPlaceCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.BLOCKPLACE)) {
            return;
        }

        final BlockPlaceData data = BlockPlaceCheck.getData(player.getDataStore());

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

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        CCBlockPlace bp = BlockPlaceCheck.getConfig(cc);

        if(bp.check && bp.reachCheck)
            s.add("blockplace.reach");

        return s;
    }
}
