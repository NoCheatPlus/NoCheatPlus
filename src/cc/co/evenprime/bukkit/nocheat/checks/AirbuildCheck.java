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
	public String actionLow = "loglow deny";
	public String actionMed = "logmed deny";
	public String actionHigh = "loghigh deny";
		
	public int limitLow = 1;
	public int limitMed = 3;
	public int limitHigh = 10;

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

				// Give a summary in 50 ticks ~ 1 second
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, data.airbuildRunnable, 50);
			}

			data.airbuildPerSecond++;

			boolean log = false;
			// Only explicitly log certain "milestones"
			if(data.airbuildPerSecond >= limitHigh) {
				if(data.airbuildPerSecond == limitHigh) {
					log = true;
				}
				action(actionHigh, event, log);
			}
			else if(data.airbuildPerSecond >= limitMed) {
				if(data.airbuildPerSecond == limitMed) {
					log = true;
				}
				action(actionMed, event, log);
			}
			else if(data.airbuildPerSecond >= limitLow) {
				if(data.airbuildPerSecond == limitLow) {
					log = true;
				}
				action(actionLow, event, log);
			}
			else
			{
				// ignore for now
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

		String logLine = "Airbuild violation summary: " +player.getName() + " total events per second: " + data.airbuildPerSecond;

		// Give a summary according to the highest violation level we encountered in that second
		if(data.airbuildPerSecond >= limitHigh) {
			plugin.logAction(actionHigh, logLine);
		}
		else if(data.airbuildPerSecond >= limitMed) {
			plugin.logAction(actionMed, logLine);
		}
		else if(data.airbuildPerSecond >= limitLow) {
			plugin.logAction(actionLow, logLine);
		}

		data.airbuildPerSecond = 0;
	}

	@Override
	public String getName() {
		return "airbuild";
	}
}
