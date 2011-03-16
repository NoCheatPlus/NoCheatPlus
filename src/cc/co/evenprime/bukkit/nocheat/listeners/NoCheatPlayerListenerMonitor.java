package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;

public class NoCheatPlayerListenerMonitor extends PlayerListener {

    @Override
    public void onPlayerTeleport(PlayerMoveEvent event) {
    	
     	NoCheatData data = NoCheatPlugin.getPlayerData(event.getPlayer());

    	if(data.reset) { // My plugin requested this teleport, so we allow it
    		data.reset = false;
    	}
    	else if(!event.isCancelled()) {
        	// If it wasn't our plugin that ordered the teleport, forget all our information and start from scratch at the new location
        	data.speedhackSetBackPoint = event.getTo().clone();
        	data.movingSetBackPoint = event.getTo().clone();
        	data.movingJumpPhase = 0;
    	}
    }
}
