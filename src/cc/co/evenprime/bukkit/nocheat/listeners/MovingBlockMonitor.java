package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;

/**
 * Handle events for all Block related events
 * 
 * @author Evenprime
 * 
 */
public class MovingBlockMonitor extends BlockListener {

    private final MovingCheck check;

    public MovingBlockMonitor(MovingCheck check) {
        this.check = check;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {

        if(!event.isCancelled())
            check.blockPlaced(event);
    }
}
