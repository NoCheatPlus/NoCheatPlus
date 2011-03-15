package cc.co.evenprime.bukkit.nocheat.listeners;


import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;
import cc.co.evenprime.bukkit.nocheat.checks.BedteleportCheck;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;
import cc.co.evenprime.bukkit.nocheat.checks.SpeedhackCheck;

/**
 * Handle events for all Player related events
 * 
 * @author Evenprime
 */

public class NoCheatPlayerListener extends PlayerListener {
	
 
 
    public NoCheatPlayerListener() {  }
    
    @Override
    public void onPlayerQuit(PlayerEvent event) {
    	NoCheatPlugin.playerData.remove(event.getPlayer());
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
    	

		if(!event.isCancelled() && NoCheatConfiguration.speedhackCheckActive)
    		SpeedhackCheck.check(event);
    	    	
		if(!event.isCancelled() && NoCheatConfiguration.movingCheckActive)
			MovingCheck.check(event);
		
    }
    @Override
    public void onPlayerTeleport(PlayerMoveEvent event) {

		if(!event.isCancelled() && NoCheatConfiguration.bedteleportCheckActive) {
			BedteleportCheck.check(event);
		}
		
		if(!event.isCancelled()) {
			NoCheatData data = NoCheatPlugin.getPlayerData(event.getPlayer());
			// If it wasn't our plugin that ordered the teleport, forget all our information to start from scratch at the new location
			if(!event.getTo().equals(data.movingSetBackPoint) && !event.getTo().equals(data.speedhackSetBackPoint)) {
				data.speedhackSetBackPoint = null;
				data.movingSetBackPoint = null;
				data.movingJumpPhase = 0;
			}
		}
    }
}
