package cc.co.evenprime.bukkit.nocheat.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.checks.moving.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 * @author Evenprime
 * 
 */
public class BlockPlaceEventManager extends BlockListener {

    private final MovingCheck movingCheck;
    private final DataManager data;

    public BlockPlaceEventManager(NoCheat plugin) {

        this.data = plugin.getDataManager();

        this.movingCheck = new MovingCheck(plugin);

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_PLACE, this, Priority.Monitor, plugin);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!event.isCancelled()) {

            final Player player = event.getPlayer();
            // Get the player-specific stored data that applies here
            final MovingData data = this.data.getMovingData(player);

            movingCheck.blockPlaced(player, data, event.getBlockPlaced());

        }
    }
}
