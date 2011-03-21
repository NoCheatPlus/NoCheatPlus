package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlugin;


/**
 * Check if the player tries to place blocks in midair (which shouldn't be possible)
 * 
 * @author Evenprime
 *
 */
public class AirbuildCheck {


	public static void check(BlockPlaceEvent event) {

		// Should we check at all?
		if(NoCheatPlugin.hasPermission(event.getPlayer(), "nocheat.airbuild")) 
			return;

		// Are all 6 sides "air-blocks" -> cancel the event
		if(event.getBlockAgainst().getType() == Material.AIR) {
			final NoCheatData data = NoCheatPlugin.getPlayerData(event.getPlayer());
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
				NoCheatPlugin.p.getServer().getScheduler().scheduleAsyncDelayedTask(NoCheatPlugin.p, data.airbuildRunnable, 50);
			}
	
			data.airbuildPerSecond++;

			boolean log = false;
			// Only explicitly log certain "milestones"
			if(data.airbuildPerSecond >= NoCheatConfiguration.airbuildLimitHigh) {
				if(data.airbuildPerSecond == NoCheatConfiguration.airbuildLimitHigh) {
					log = true;
				}
				action(NoCheatConfiguration.airbuildActionHigh, event, log);
			}
			else if(data.airbuildPerSecond >= NoCheatConfiguration.airbuildLimitMed) {
				if(data.airbuildPerSecond == NoCheatConfiguration.airbuildLimitMed) {
					log = true;
				}
				action(NoCheatConfiguration.airbuildActionMed, event, log);
			}
			else if(data.airbuildPerSecond >= NoCheatConfiguration.airbuildLimitLow) {
				if(data.airbuildPerSecond == NoCheatConfiguration.airbuildLimitLow) {
					log = true;
				}
				action(NoCheatConfiguration.airbuildActionLow, event, log);
			}
			else
			{
				// ignore for now
			}
		}
	}

	private static void action(String action, BlockPlaceEvent event, boolean log) {

		// LOG IF NEEDED
		if(log && action.contains("log")) {
			Location l = event.getBlockPlaced().getLocation();
			NoCheatPlugin.logAction(action, "Airbuild violation: "+event.getPlayer().getName()+" tried to place block " + event.getBlockPlaced().getType() + " in the air at " + l.getBlockX() + "," + l.getBlockY() +"," + l.getBlockZ());
		}
		
		// DENY IF NEEDED
		if(action.contains("deny")) {
			event.setCancelled(true);
		}
	}
	
	private static void summary(Player player, NoCheatData data) {
		
		String logLine = "Airbuild violation summary: " +player.getName() + " total events per second: " + data.airbuildPerSecond;
		
		// Give a summary according to the highest violation level we encountered in that second
		if(data.airbuildPerSecond >= NoCheatConfiguration.airbuildLimitHigh) {
			NoCheatPlugin.logAction(NoCheatConfiguration.airbuildActionHigh, logLine);
		}
		else if(data.airbuildPerSecond >= NoCheatConfiguration.airbuildLimitMed) {
			NoCheatPlugin.logAction(NoCheatConfiguration.airbuildActionMed, logLine);
		}
		else if(data.airbuildPerSecond >= NoCheatConfiguration.airbuildLimitLow) {
			NoCheatPlugin.logAction(NoCheatConfiguration.airbuildActionLow, logLine);
		}
		
		data.airbuildPerSecond = 0;
	}
}
