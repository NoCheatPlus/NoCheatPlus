package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlacingCheck {

	public static void check(BlockPlaceEvent event) {
		

    	if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(event.getPlayer(), "nocheat.airbuild")) {
			return;
		}
		else if(NoCheatPlugin.Permissions == null && event.getPlayer().isOp() ) {
			return;
		}
    	
    	if(event.getBlockAgainst().getType() == Material.AIR) {
    		event.setCancelled(true);
    		NoCheatPlugin.log.warning("NoCheatPlugin: Airbuild violation: "+event.getPlayer().getName()+" tried to place block " + event.getBlockPlaced().getType() + " against air");
    		
    	}
	}
}
