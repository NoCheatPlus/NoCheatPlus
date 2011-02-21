package cc.co.evenprime.bukkit.nocheat;



import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author Evenprime
 */

public class NoCheatPluginPlayerListener extends PlayerListener {
	
 
 
    public NoCheatPluginPlayerListener() {  }
    
    @Override
    public void onPlayerQuit(PlayerEvent event) {
    	NoCheatPlugin.playerData.remove(event.getPlayer());
    }
    
    public void ingoreNextXEvents(Entity player, int count) {

    	NoCheatPluginData data = NoCheatPlugin.playerData.get(player);
    	if(data != null) {
    		data.ignoreNextXEvents = count;
    	}
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
    	
		// Get the player-specific data
		NoCheatPluginData data = NoCheatPlugin.getPlayerData(event.getPlayer());
	
		if(data.ignoreNextXEvents > 0 ) {
    		data.ignoreNextXEvents--;
    		return;
    	}
    	
		if(!event.isCancelled() && NoCheatConfiguration.speedhackCheckActive)
    		SpeedhackCheck.check(data, event);
    	    	
		if(!event.isCancelled() && NoCheatConfiguration.movingCheckActive)
			MovingCheck.check(data, event);
		
    }
}
