package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.logging.Level;

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
		if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(event.getPlayer(), "nocheat.speedhack")) {
			return;
		}
		else if(NoCheatPlugin.Permissions == null && event.getPlayer().isOp() ) {
			return;
		}
		
		// Get the time of the server
		long time = System.currentTimeMillis();
		
		Level vl = null;

		// Is it time for a speedhack check now?
		if(time > interval + data.speedhackLastCheck ) {
			// Yes
			// TODO: Needs some better handling for server lag

			int limitLow = (int)((NoCheatConfiguration.speedhackLimitLow * (time - data.speedhackLastCheck)) / interval);
			int limitMed = (int)((NoCheatConfiguration.speedhackLimitMed * (time - data.speedhackLastCheck)) / interval);
			int limitHigh = (int)((NoCheatConfiguration.speedhackLimitHigh * (time - data.speedhackLastCheck)) / interval);

			
			if(data.speedhackEventsSinceLastCheck > limitHigh) vl = Level.SEVERE;
			else if(data.speedhackEventsSinceLastCheck > limitMed) vl = Level.WARNING;
			else if(data.speedhackEventsSinceLastCheck > limitLow) vl = Level.INFO;

		
			
			if(vl != null) data.speedhackViolationsInARow++;
			else data.speedhackViolationsInARow = 0;
			
			if(data.speedhackViolationsInARow >= violationsLimit) {
				String message = "NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.speedhackEventsSinceLastCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?";

				NoCheatPlugin.log(vl, message);
			}
			
			// Reset values for next check
			data.speedhackEventsSinceLastCheck = 0;
			data.speedhackLastCheck = time;
			
		}
		data.speedhackEventsSinceLastCheck++;
	}
}
