package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Log if a player sends to many move events in a specific time frame, usually the result of tinkering with the system clock
 * 
 * @author Evenprime
 *
 */
public class SpeedhackCheck {

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

		// Is it time for a speedhack check now?
		if(time > NoCheatConfiguration.speedhackInterval + data.lastSpeedHackCheck ) {
			// Yes
			// TODO: Needs some better handling for server lag

			int limitLow = (int)((NoCheatConfiguration.speedhackLow * (time - data.lastSpeedHackCheck)) / NoCheatConfiguration.speedhackInterval);
			int limitMed = (int)((NoCheatConfiguration.speedhackMed * (time - data.lastSpeedHackCheck)) / NoCheatConfiguration.speedhackInterval);
			int limitHigh = (int)((NoCheatConfiguration.speedhackHigh * (time - data.lastSpeedHackCheck)) / NoCheatConfiguration.speedhackInterval);

			if(data.eventsSinceLastSpeedHackCheck > limitHigh)
				NoCheatPlugin.logHeavy("NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.eventsSinceLastSpeedHackCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?");
			else if(data.eventsSinceLastSpeedHackCheck > limitMed)
				NoCheatPlugin.logNormal("NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.eventsSinceLastSpeedHackCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?");
			else if(data.eventsSinceLastSpeedHackCheck > limitLow)
				NoCheatPlugin.logMinor("NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.eventsSinceLastSpeedHackCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?");
			// Reset values for next check
			data.eventsSinceLastSpeedHackCheck = 0;
			data.lastSpeedHackCheck = time;
		}

		data.eventsSinceLastSpeedHackCheck++;
	}
}
