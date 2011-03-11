package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;


/**
 * Check if the player tries to place blocks in midair (which shouldn't be possible)
 * 
 * @author Evenprime
 *
 */
public class AirbuildCheck {


	public static void check(BlockPlaceEvent event) {

		// Should we check at all?
		if(NoCheatPlugin.hasPermission(event.getPlayer(), "nocheat.airbuild")) 
			return;


		// Are all 6 sides "air-blocks" -> cancel the event
		if(event.getBlockAgainst().getType() == Material.AIR)
		    action(NoCheatConfiguration.airbuildAction, event);
			
	}
	
	private static void action(String action, BlockPlaceEvent event) {
		
		// LOG IF NEEDED
		if(action.contains("log")) {
			Location l = event.getBlockPlaced().getLocation();
			NoCheatPlugin.logAction(action, "NoCheatPlugin: Airbuild violation: "+event.getPlayer().getName()+" tried to place block " + event.getBlockPlaced().getType() + " in the air at " + l.getBlockX() + "," + l.getBlockY() +"," + l.getBlockZ());
		}
		
		// DENY IF NEEDED
		if(action.contains("deny")) {
			event.setCancelled(true);
		}
	}
}
