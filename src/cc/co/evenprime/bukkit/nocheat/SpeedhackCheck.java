package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.event.player.PlayerMoveEvent;


public class SpeedhackCheck {

	public static void check(NoCheatPluginData data, PlayerMoveEvent event) {
		
		// Get the time of the server
		long time = System.currentTimeMillis();
		
		
		if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(event.getPlayer(), "nocheat.speedhack")) {
			return;
		}
		else if(NoCheatPlugin.Permissions == null && event.getPlayer().isOp() ) {
			return;
		}

		// Is it time for a speedhack check now?
		if(time > NoCheatConfiguration.speedhackInterval + data.lastSpeedHackCheck ) {
			// Yes
			// TODO: Needs some better handling for server lag

			int limitLow = (int)((NoCheatConfiguration.speedhackLow * (time - data.lastSpeedHackCheck)) / NoCheatConfiguration.speedhackInterval);
			int limitMed = (int)((NoCheatConfiguration.speedhackMed * (time - data.lastSpeedHackCheck)) / NoCheatConfiguration.speedhackInterval);
			int limitHigh = (int)((NoCheatConfiguration.speedhackHigh * (time - data.lastSpeedHackCheck)) / NoCheatConfiguration.speedhackInterval);

			if(data.eventsSinceLastSpeedHackCheck > limitHigh)
				NoCheatPlugin.log.severe("NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.eventsSinceLastSpeedHackCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?");
			else if(data.eventsSinceLastSpeedHackCheck > limitMed)
				NoCheatPlugin.log.warning("NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.eventsSinceLastSpeedHackCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?");
			else if(data.eventsSinceLastSpeedHackCheck > limitLow)
				NoCheatPlugin.log.info("NoCheatPlugin: "+event.getPlayer().getName()+" sent "+ data.eventsSinceLastSpeedHackCheck + " move events, but only "+limitLow+ " were allowed. Speedhack?");
			// Reset values for next check
			data.eventsSinceLastSpeedHackCheck = 0;
			data.lastSpeedHackCheck = time;
		}

		data.eventsSinceLastSpeedHackCheck++;
	}
}
