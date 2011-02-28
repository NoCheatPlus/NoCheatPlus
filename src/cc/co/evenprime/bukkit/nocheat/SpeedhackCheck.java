package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Log if a player sends to many move events in a specific time frame, usually the result of tinkering with the system clock
 * 
 * @author Evenprime
 *
 */
public class SpeedhackCheck {

    // Violation levels
    private static final int HEAVY = 3;
    private static final int NORMAL = 2;
    private static final int MINOR = 1;
    private static final int NONE = 0;
    
    private static final long interval = 1000;
    private static final int violationsLimit = 3;
    
	public static void check(NoCheatPluginData data, PlayerMoveEvent event) {
		
		// Should we check at all?
		if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(event.getPlayer(), "nocheat.speedhack")) {
			return;
		}
		else if(NoCheatPlugin.Permissions == null && event.getPlayer().isOp() ) {
			return;
		}
		
		// Get the time of the server
		long time = System.currentTimeMillis();
		
		int vl = NONE;

		// Is it time for a speedhack check now?
		if(time > interval + data.speedhackLastCheck ) {
			// Yes
			// TODO: Needs some better handling for server lag

			int limitLow = (int)((NoCheatConfiguration.speedhackLow * (time - data.speedhackLastCheck)) / interval);
			int limitMed = (int)((NoCheatConfiguration.speedhackMed * (time - data.speedhackLastCheck)) / interval);
			int limitHigh = (int)((NoCheatConfiguration.speedhackHigh * (time - data.speedhackLastCheck)) / interval);

			
			if(data.speedhackEventsSinceLastCheck > limitHigh) vl = HEAVY;
			else if(data.speedhackEventsSinceLastCheck > limitMed) vl = NORMAL;
			else if(data.speedhackEventsSinceLastCheck > limitLow) vl = MINOR;

			// Reset values for next check
			data.speedhackEventsSinceLastCheck = 0;
			data.speedhackLastCheck = time;
			
			if(vl > NONE) data.speedhackViolationsInARow++;
			else data.speedhackViolationsInARow = 0;
			
			if(data.speedhackViolationsInARow >= violationsLimit) {
				String message = "NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.speedhackEventsSinceLastCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?";
				switch(vl) {
				case HEAVY: NoCheatPlugin.logHeavy(message); break;
				case NORMAL:  NoCheatPlugin.logNormal(message); break;
				case MINOR:  NoCheatPlugin.logMinor(message); break;
				}
			}
			
		}
		data.speedhackEventsSinceLastCheck++;
	}
}
