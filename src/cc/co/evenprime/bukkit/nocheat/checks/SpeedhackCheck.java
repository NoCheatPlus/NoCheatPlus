package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;

/**
 * Log if a player sends to many move events in a specific time frame, usually the result of tinkering with the system clock
 * 
 * @author Evenprime
 *
 */
public class SpeedhackCheck {

   
    private static final long interval = 1000;
    private static final int violationsLimit = 3;
    
	public static void check(NoCheatData data, PlayerMoveEvent event) {
		
		// Should we check at all?
		if(NoCheatPlugin.hasPermission(event.getPlayer(), "nocheat.speedhack")) 
			return;
		
		// Get the time of the server
		long time = System.currentTimeMillis();
		
		// Is it time for a speedhack check now?
		if(time > interval + data.speedhackLastCheck ) {
			// Yes
			// TODO: Needs some better handling for server lag
			String action = null;

			int limitLow = (int)((NoCheatConfiguration.speedhackLimitLow * (time - data.speedhackLastCheck)) / interval);
			int limitMed = (int)((NoCheatConfiguration.speedhackLimitMed * (time - data.speedhackLastCheck)) / interval);
			int limitHigh = (int)((NoCheatConfiguration.speedhackLimitHigh * (time - data.speedhackLastCheck)) / interval);

			
			if(data.speedhackEventsSinceLastCheck > limitHigh) action = NoCheatConfiguration.speedhackActionHeavy;
			else if(data.speedhackEventsSinceLastCheck > limitMed) action = NoCheatConfiguration.speedhackActionNormal;
			else if(data.speedhackEventsSinceLastCheck > limitLow) action = NoCheatConfiguration.speedhackActionMinor;
			
			if(action == null) {
				data.speedhackSetBackPoint = event.getFrom().clone();
				data.speedhackViolationsInARow = 0;
			}
			else {
				data.speedhackViolationsInARow++;
			}
			
			if(data.speedhackViolationsInARow >= violationsLimit) {
				action(action, event, data);
			}
			
			// Reset values for next check
			data.speedhackEventsSinceLastCheck = 0;
			data.speedhackLastCheck = time;
			
		}
		
		data.speedhackEventsSinceLastCheck++;
	}
	
	private static void action(String actions, PlayerMoveEvent event, NoCheatData data) {
		
		if(actions == null) return;
		// LOGGING IF NEEDED
		if(actions.contains("log")) {
			NoCheatPlugin.logAction(actions, event.getPlayer().getName()+" sent "+ data.speedhackEventsSinceLastCheck + " move events, but only "+NoCheatConfiguration.speedhackLimitLow+ " were allowed. Speedhack?");
		}
		// RESET IF NEEDED
		if(actions.contains("reset")) {
			resetPlayer(event, data);
		}
	}
	
	private static void resetPlayer(PlayerMoveEvent event, NoCheatData data) {
		
		// If we have stored a location for the player, we put him back there
		if(data.speedhackSetBackPoint != null) {
			
			// Lets try it that way. Maybe now people don't "disappear" any longer
			event.setFrom(data.speedhackSetBackPoint);
			event.setTo(data.speedhackSetBackPoint);
			event.getPlayer().teleportTo(data.speedhackSetBackPoint);
		}
		else {
			// Lets try it that way. Maybe now people don't "disappear" any longer
			event.setFrom(event.getFrom());
			event.setTo(event.getFrom().clone());
			event.getPlayer().teleportTo(event.getFrom());
		}
	}
}
