package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;

public class BedteleportCheck {


	public static void check(PlayerMoveEvent event) {
		
		// Should we check at all?
		if(NoCheatPlugin.hasPermission(event.getPlayer(), "nocheat.bedteleport")) 
			return;
    	    	
    	if(event.getFrom().getWorld().getBlockTypeIdAt(event.getFrom()) == Material.BED_BLOCK.getId()) {
    		double yRest = Math.floor(event.getFrom().getY()) - event.getFrom().getY();
    		if(yRest > 0.099 && yRest < 0.101)
        		// Don't allow the teleport
    			event.setCancelled(true);
    	}
	}
}
