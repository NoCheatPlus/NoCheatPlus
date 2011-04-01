package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.CancelAction;
import cc.co.evenprime.bukkit.nocheat.actions.LogAction;


/**
 * Check if the player tries to place blocks in midair (which shouldn't be possible)
 * 
 * @author Evenprime
 *
 */
public class AirbuildCheck extends Check {

	// How should airbuild violations be treated?
	public final Action actions[][] = { 
			{ LogAction.logLow,  CancelAction.deny }, 
			{ LogAction.logMed,  CancelAction.deny },
			{ LogAction.logHigh, CancelAction.deny } };

	public final int limits[] = { 1, 3, 10 };

	public AirbuildCheck(NoCheatPlugin plugin) {
		super(plugin);
		setActive(false);
	}

	public void check(BlockPlaceEvent event) {

		// Should we check at all?
		if(plugin.hasPermission(event.getPlayer(), "nocheat.airbuild")) 
			return;

		// Are all 6 sides "air-blocks" -> cancel the event
		if(event.getBlockAgainst().getType() == Material.AIR) {
			final NoCheatData data = plugin.getPlayerData(event.getPlayer());
			final Player p = event.getPlayer();

			if(data.airbuildRunnable == null) {
				data.airbuildRunnable = new Runnable() {

					@Override
					public void run() {
						summary(p, data);
						// deleting its own reference
						data.airbuildRunnable = null;
					}
				};

				// Give a summary in 20 ticks ~ 1 second
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, data.airbuildRunnable, 20);
			}

			data.airbuildPerSecond++;

			// which limit has been reached
			for(int i = limits.length-1; i >= 0; i--) {
				if(data.airbuildPerSecond >= limits[i]) {
					// Only explicitly log certain "milestones"
					if(data.airbuildPerSecond == limits[i]) {
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

	private void action(Action actions[], BlockPlaceEvent event, boolean log) {

		for(Action a : actions) {
			if(log && a instanceof LogAction) {
				final Location l = event.getBlockPlaced().getLocation();
				plugin.log(((LogAction)a).getLevel(), "Airbuild: "+event.getPlayer().getName()+" tried to place block " + event.getBlockPlaced().getType() + " in the air at " + l.getBlockX() + "," + l.getBlockY() +"," + l.getBlockZ());
			}
			else if(a instanceof CancelAction)
				event.setCancelled(true);
		}
	}

	private void summary(Player player, NoCheatData data) {

		// Give a summary according to the highest violation level we encountered in that second
		for(int i = limits.length-1; i >= 0; i--) {
			if(data.airbuildPerSecond >= limits[i]) {
				plugin.log(LogAction.log[i].getLevel(), "Airbuild summary: " +player.getName() + " total violations per second: " + data.airbuildPerSecond);
				break;
			}
		}

		data.airbuildPerSecond = 0;
	}

	@Override
	public String getName() {
		return "airbuild";
	}
}
