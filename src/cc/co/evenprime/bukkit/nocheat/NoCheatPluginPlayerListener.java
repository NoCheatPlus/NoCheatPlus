package cc.co.evenprime.bukkit.nocheat;


import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author Evenprime
 */

public class NoCheatPluginPlayerListener extends PlayerListener {
	
	/**
	 * Storage for data persistence between events
	 *
	 */
	public class NoCheatPluginData {
		
		/**
		 *  Don't rely on any of these yet, they are likely going to 
		 * change their name/functionality 
		 */
		int phase = 0; // current jumpingPhase
		long lastSpeedHackCheck = System.currentTimeMillis(); // timestamp of last check for speedhacks
		int eventsSinceLastSpeedHackCheck = 0; // used to identify speedhacks
		int ignoreNextXEvents = 0;
		
		int movingViolations[] = {0,0,0};
		int movingViolationsTotal[] = {0,0,0};
		boolean movingViolationsDirty = false;
		Location movingSetBackPoint = null;
		
		private NoCheatPluginData() { }
	}
	
    private final NoCheatPlugin plugin;
    
    // Store data between Events
    private static Map<Player, NoCheatPluginData> playerData = new HashMap<Player, NoCheatPluginData>();
    

    public NoCheatPluginPlayerListener(NoCheatPlugin instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerQuit(PlayerEvent event) {
    	playerData.remove(event.getPlayer());
    }
    
    
    public void ingoreNextXEvents(Entity player) {

    	NoCheatPluginData data = playerData.get(player);
    	if(data != null) {
    		data.ignoreNextXEvents = 1;
    	}
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
    	
		// Get the player-specific data
		NoCheatPluginData data = null;
		
		if((data = playerData.get(event.getPlayer())) == null ) {
			// If we have no data for the player, create some
			data = new NoCheatPluginData();
			playerData.put(event.getPlayer(), data);
		}
		
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
