package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.checks.AirbuildCheck;

/**
 * Handle events for all Block related events
 * 
 * @author Evenprime
 *
 */
public class NoCheatBlockListener extends BlockListener {

	
	public NoCheatBlockListener() {

	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {

		if(!event.isCancelled() && NoCheatConfiguration.airbuildCheckActive)
			AirbuildCheck.check(event);
	}
}
