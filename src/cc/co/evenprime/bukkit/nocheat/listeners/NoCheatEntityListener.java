package cc.co.evenprime.bukkit.nocheat.listeners;


import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;


public class NoCheatEntityListener extends EntityListener {

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		
		
		if(event.getEntity() instanceof Player) {
			
			Player p = (Player)event.getEntity();
			
			NoCheatPlugin.getPlayerData(p).movingJumpPhase = 0;
		}
		
		
	}
	                                                      
	                                                   
}
