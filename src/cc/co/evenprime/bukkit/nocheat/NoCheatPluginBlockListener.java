package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Handle events for all Block related events
 * 
 * @author Evenprime
 *
 */
public class NoCheatPluginBlockListener extends BlockListener {

	
	public NoCheatPluginBlockListener() {

	}
	
	@Override
	public void onBlockPlace(BlockPlaceEvent event) {

		if(!event.isCancelled() && NoCheatConfiguration.airbuildCheckActive)
			BlockPlacingCheck.check(event);
	}
}
