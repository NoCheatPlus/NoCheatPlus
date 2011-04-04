package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.CustomAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;

/**
 * Log if a player sends to many move events in a specific time frame, usually the result of tinkering with the system clock
 * 
 * @author Evenprime
 *
 */
public class SpeedhackCheck extends Check {

	public SpeedhackCheck(NoCheat plugin) {
		super(plugin);
		setActive(true);
	}

	private static final long interval = 1000;
	private static final int violationsLimit = 3;
	
	// Limits for the speedhack check
	public int limits[] = { 30, 45, 60 };
	
	// How should speedhack violations be treated?
	public Action actions[][] = { 
			{ LogAction.loglow, CancelAction.cancel }, 
			{ LogAction.logmed, CancelAction.cancel },
			{ LogAction.loghigh, CancelAction.cancel } };
	
	public String logMessage = "%1$s sent %2$d move events, but only %3$d were allowed. Speedhack?";

	public void check(PlayerMoveEvent event) {

		// Should we check at all?
		if(plugin.hasPermission(event.getPlayer(), "nocheat.speedhack")) 
			return;

		// Get the player-specific data
		NoCheatData data = plugin.getPlayerData(event.getPlayer());

		// Ignore events if the player has positive y-Velocity (these can be the cause of event spam between server and client)
		if(event.getPlayer().getVelocity().getY() > 0.0D) {
			return;
		}
		
		// Ignore events of players in vehicles (these can be the cause of event spam between server and client)
		if(event.getPlayer().isInsideVehicle()) {
			return;
		}
		
		// Get the time of the server
		long time = System.currentTimeMillis();

		// Is it time for a speedhack check now?
		if(time > interval + data.speedhackLastCheck ) {
			// Yes
			// TODO: Needs some better handling for server lag
			Action action[] = null;

			int low = (int)((limits[0] * (time - data.speedhackLastCheck)) / interval);
			int med = (int)((limits[1] * (time - data.speedhackLastCheck)) / interval);
			int high = (int)((limits[2] * (time - data.speedhackLastCheck)) / interval);


			if(data.speedhackEventsSinceLastCheck > high) action = actions[2];
			else if(data.speedhackEventsSinceLastCheck > med) action = actions[1];
			else if(data.speedhackEventsSinceLastCheck > low) action = actions[0];

			if(action == null) {
				data.speedhackSetBackPoint = event.getFrom().clone();
				data.speedhackViolationsInARow = 0;
			}
			else {
				// If we haven't already got a setback point, create one now
				if(data.speedhackSetBackPoint == null) {
					data.speedhackSetBackPoint = event.getFrom().clone();
				}
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

	private void action(Action actions[], PlayerMoveEvent event, NoCheatData data) {

		if(actions == null) return;
		
		String log = String.format(logMessage, event.getPlayer().getName(), data.speedhackEventsSinceLastCheck, limits[0]);
		
		for(Action a : actions) {
			if(a instanceof LogAction) 
				plugin.log(((LogAction)a).level, log);
			else if(a instanceof CancelAction)
				resetPlayer(event, data);
			else if(a instanceof CustomAction)
				plugin.handleCustomAction(a, event.getPlayer());
		}
	}

	private static void resetPlayer(PlayerMoveEvent event, NoCheatData data) {

		Location l = data.speedhackSetBackPoint;

		data.reset = true;
		// If we have stored a location for the player, we put him back there
		if(l != null) {
			event.setFrom(l);
			event.setTo(l);
			event.setCancelled(true);
			event.getPlayer().teleport(l);
		}
		else {
			event.setFrom(event.getFrom());
			event.setTo(event.getFrom().clone());
			event.setCancelled(true);
			event.getPlayer().teleport(event.getFrom());
		}
	}

	@Override
	public String getName() {
		return "speedhack";
	}
}
