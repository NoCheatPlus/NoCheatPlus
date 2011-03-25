package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.checks.AirbuildCheck;

/**
 * Handle events for all Block related events
 * 
 * @author Evenprime
 *
 */
public class AirbuildListener extends BlockListener {

	private AirbuildCheck check;
	public AirbuildListener(AirbuildCheck check) {
		this.check = check;
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {

		if(!event.isCancelled() && check.isActive())
			check.check(event);
	}
}
