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
public class AirbuildBlockListener extends BlockListener {

	private final AirbuildCheck check;

	public AirbuildBlockListener(AirbuildCheck check) {
		this.check = check;
	}

	@Override
	public void onBlockPlace(BlockPlaceEvent event) {

		if(!event.isCancelled()) check.check(event);
	}
}
