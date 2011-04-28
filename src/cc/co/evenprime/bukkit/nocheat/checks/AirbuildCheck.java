package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.CustomAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;
import cc.co.evenprime.bukkit.nocheat.data.AirbuildData;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.AirbuildBlockListener;


/**
 * Check if the player tries to place blocks in midair (which shouldn't be possible)
 * 
 * @author Evenprime
 *
 */
public class AirbuildCheck extends Check {

	// How should airbuild violations be treated?
	public final Action actions[][] = { 
			{ LogAction.loglow,  CancelAction.cancel }, 
			{ LogAction.logmed,  CancelAction.cancel },
			{ LogAction.loghigh, CancelAction.cancel } };

	public final int limits[] = { 1, 3, 10 };

	public AirbuildCheck(NoCheat plugin) {
		super(plugin, "airbuild", PermissionData.PERMISSION_AIRBUILD);
	}

	public void check(BlockPlaceEvent event) {

		// Should we check at all?
		if(skipCheck(event.getPlayer())) return;

		// Are all 6 sides "air-blocks" -> cancel the event
		if(event.getBlockAgainst().getType() == Material.AIR) {
			final AirbuildData data = AirbuildData.get(event.getPlayer());
			final Player p = event.getPlayer();

			if(data.summaryTask == null) {
				data.summaryTask = new Runnable() {

					@Override
					public void run() {
						summary(p, data);
						// deleting its own reference
						data.summaryTask = null;
					}
				};

				// Give a summary in 20 ticks ~ 1 second
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, data.summaryTask, 20);
			}

			data.perSecond++;

			// which limit has been reached
			for(int i = limits.length-1; i >= 0; i--) {
				if(data.perSecond >= limits[i]) {
					// Only explicitly log certain "milestones"
					if(data.perSecond == limits[i]) {
						action(actions[i], event, true);
					}
					else {
						action(actions[i], event, false);
					}
					break;
				}
			}
		}
	}

	private void action(Action actions[], BlockPlaceEvent event, boolean loggingAllowed) {

		if(actions == null) return;

		boolean cancelled = false;

		// Prepare log message if needed
		String logMessage = null;
		if(loggingAllowed) {
			final Location l = event.getBlockPlaced().getLocation();
			logMessage = "Airbuild: "+event.getPlayer().getName()+" tried to place block " + event.getBlockPlaced().getType() + " in the air at " + l.getBlockX() + "," + l.getBlockY() +"," + l.getBlockZ();
		}

		// Execute actions in order
		for(Action a : actions) {
			if(loggingAllowed && a instanceof LogAction) {
				plugin.log(((LogAction)a).level, logMessage);
			}
			else if(!cancelled && a instanceof CancelAction) {
				event.setCancelled(true);
				cancelled = true;
			}
			else if(a instanceof CustomAction) {
				plugin.handleCustomAction(a, event.getPlayer());
			}
		}
	}

	private void summary(Player player, AirbuildData data) {

		// Give a summary according to the highest violation level we encountered in that second
		for(int i = limits.length-1; i >= 0; i--) {
			if(data.perSecond >= limits[i]) {
				plugin.log(LogAction.log[i].level, "Airbuild summary: " +player.getName() + " total violations per second: " + data.perSecond);
				break;
			}
		}

		data.perSecond = 0;
	}

	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		// Register listeners for airbuild check
		pm.registerEvent(Event.Type.BLOCK_PLACE, new AirbuildBlockListener(this), Priority.Low, plugin);

	}
}
