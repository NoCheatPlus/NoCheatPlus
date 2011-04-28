package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.CustomAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.data.SpeedhackData;
import cc.co.evenprime.bukkit.nocheat.listeners.SpeedhackPlayerListener;

/**
 * Log if a player sends to many move events in a specific time frame, usually the result of tinkering with the system clock
 * 
 * @author Evenprime
 *
 */
public class SpeedhackCheck extends Check {

	public SpeedhackCheck(NoCheat plugin) {
		super(plugin, "speedhack", PermissionData.PERMISSION_SPEEDHACK);
	}

	private static final int violationsLimit = 2;

	// Limits for the speedhack check per second
	public int limits[] = { 30, 45, 60 };

	// How should speedhack violations be treated?
	public Action actions[][] = { 
			{ LogAction.loglow, CancelAction.cancel }, 
			{ LogAction.logmed, CancelAction.cancel },
			{ LogAction.loghigh, CancelAction.cancel } };

	public String logMessage = "%1$s sent %2$d move events, but only %3$d were allowed. Speedhack?";

	public void check(PlayerMoveEvent event) {

		Player player = event.getPlayer();
		// Should we check at all?
		if(skipCheck(player)) return;

		// Ignore events of players in vehicles (these can be the cause of event spam between server and client)
		// Ignore events if the player has positive y-Velocity (these can be the cause of event spam between server and client)
		if(player.isInsideVehicle() || player.getVelocity().getY() > 0.0D) {
			return;
		}

		// Get the player-specific data
		SpeedhackData data = SpeedhackData.get(player);

		// Count the event
		data.eventsSinceLastCheck++;

		// Get the ticks of the server
		int ticks = plugin.getServerTicks();

		// Roughly half a second (= 10 ticks) passed
		if(data.lastCheckTicks + 10 == ticks) {

			// If we haven't already got a setback point, create one now
			if(data.setBackPoint == null) {
				data.setBackPoint = event.getFrom().clone();
			}

			if(plugin.getServerLag() > 200) {
				// Any data would likely be unreliable with that lag
				resetData(data, event.getFrom().clone(), ticks);
			}
			else {
				Action action[] = null;

				final int low  = (limits[0]+1) / 2;
				final int med  = (limits[1]+1) / 2;
				final int high = (limits[2]+1) / 2;

				if(data.eventsSinceLastCheck > high) action = actions[2];
				else if(data.eventsSinceLastCheck > med) action = actions[1];
				else if(data.eventsSinceLastCheck > low) action = actions[0];
				else resetData(data, event.getFrom().clone(), ticks);


				if(action != null)	data.violationsInARow++;

				if(data.violationsInARow >= violationsLimit) {
					action(action, event, data);
				}

				// Reset value for next check
				data.eventsSinceLastCheck = 0;
			}

			data.lastCheckTicks = ticks;
		}
		else if(data.lastCheckTicks + 10 < ticks)
		{
			// The player didn't move for the last 10 ticks
			resetData(data, event.getFrom().clone(), ticks);
		}
	}

	private static void resetData(SpeedhackData data, Location l, int ticks) {
		data.violationsInARow = 0;
		data.eventsSinceLastCheck = 0;
		data.setBackPoint = l;
		data.lastCheckTicks = ticks;
	}

	private void action(Action actions[], PlayerMoveEvent event, SpeedhackData data) {

		if(actions == null) return;

		String log = String.format(logMessage, event.getPlayer().getName(), data.eventsSinceLastCheck*2, limits[0]);

		for(Action a : actions) {
			if(a instanceof LogAction) 
				plugin.log(((LogAction)a).level, log);
			else if(a instanceof CancelAction)
				resetPlayer(event, data);
			else if(a instanceof CustomAction)
				plugin.handleCustomAction(a, event.getPlayer());
		}
	}

	private static void resetPlayer(PlayerMoveEvent event, SpeedhackData data) {

		if(data.setBackPoint == null) data.setBackPoint = event.getFrom().clone();

		// If we have stored a location for the player, we put him back there

		if(event.getPlayer().teleport(data.setBackPoint)) {
			event.setFrom(data.setBackPoint);
			event.setTo(data.setBackPoint);
			event.setCancelled(true);
		}
	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		Listener speedhackPlayerListener = new SpeedhackPlayerListener(this);
		// Register listeners for speedhack check
		pm.registerEvent(Event.Type.PLAYER_MOVE, speedhackPlayerListener, Priority.High, plugin);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, speedhackPlayerListener, Priority.Monitor, plugin);		

	}

	public void teleported(PlayerTeleportEvent event) {

		SpeedhackData data = SpeedhackData.get(event.getPlayer());

		data.setBackPoint = event.getTo();
		data.eventsSinceLastCheck = 0;
	}
}
