package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;


/**
 * Check if the player tries to place blocks in midair (which shouldn't be possible)
 * 
 * @author Evenprime
 *
 */
public class AirbuildCheck extends Check {

	// How should airbuild violations be treated?
	public final String actions[] = { "loglow deny", "logmed deny", "loghigh deny" };

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

	private void action(String action, BlockPlaceEvent event, boolean log) {

		// LOG IF NEEDED
		if(log && action.contains("log")) {
			Location l = event.getBlockPlaced().getLocation();
			plugin.logAction(action, "Airbuild violation: "+event.getPlayer().getName()+" tried to place block " + event.getBlockPlaced().getType() + " in the air at " + l.getBlockX() + "," + l.getBlockY() +"," + l.getBlockZ());
		}

		// DENY IF NEEDED
		if(action.contains("deny")) {
			event.setCancelled(true);
		}
	}

	private void summary(Player player, NoCheatData data) {

		// Give a summary according to the highest violation level we encountered in that second
		for(int i = limits.length-1; i >= 0; i--) {
			if(data.airbuildPerSecond >= limits[i]) {
				plugin.logAction(actions[i], "Airbuild violation summary: " +player.getName() + " total events per second: " + data.airbuildPerSecond);
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
