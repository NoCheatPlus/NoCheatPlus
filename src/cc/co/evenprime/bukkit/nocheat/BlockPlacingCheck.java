package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlacingCheck {

	public static void check(BlockPlaceEvent event) {
		

    	if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(event.getPlayer(), "nocheat.airbuild")) {
			return;
		}
		else if(NoCheatPlugin.Permissions == null && event.getPlayer().isOp() ) {
			return;
		}
    	
    	Location l = event.getBlockPlaced().getLocation(); 
    	World w = event.getBlock().getWorld();
    	int airId = Material.AIR.getId();
    	
    	if(w.getBlockTypeIdAt(l.getBlockX()-1, l.getBlockY(), l.getBlockZ()) == airId &&
    	   w.getBlockTypeIdAt(l.getBlockX()+1, l.getBlockY(), l.getBlockZ()) == airId &&
    	   w.getBlockTypeIdAt(l.getBlockX(), l.getBlockY()-1, l.getBlockZ()) == airId &&
    	   w.getBlockTypeIdAt(l.getBlockX(), l.getBlockY()+1, l.getBlockZ()) == airId &&
    	   w.getBlockTypeIdAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()-1) == airId &&
    	   w.getBlockTypeIdAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()+1) == airId) {
    		event.setCancelled(true);
    		NoCheatPlugin.log.warning("NoCheatPlugin: Airbuild violation: "+event.getPlayer().getName()+" tried to place block " + event.getBlockPlaced().getType() + " in the air");
    	}
	}
}
