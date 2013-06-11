package fr.neatmonster.nocheatplus.command.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;

public class LagCommand extends BaseCommand {

	public LagCommand(JavaPlugin plugin) {
		super(plugin, "lag", Permissions.ADMINISTRATION_LAG);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		StringBuilder builder = new StringBuilder(300);
		builder.append("---- Lag tracking ----\n");
		// Lag spikes.
		long[] spikeDurations = TickTask.getLagSpikeDurations();
		int[] spikes = TickTask.getLagSpikes();
		builder.append("#### Lag spikes ####\n");	
		if (spikes[0] == 0){
			builder.append("No spikes > " + spikeDurations[0] + " ms within the last 40 to 60 minutes.");
		}
		else if (spikes[0] > 0){
			builder.append("Total: " + spikes[0] + " > " + spikeDurations[0] + " ms within the last 40 to 60 minutes.");
			builder.append("\n| ");
			for (int i = 0; i < spikeDurations.length; i++){
				if (i < spikeDurations.length - 1 && spikes[i] == spikes[i + 1]){
					// Ignore these, get printed later.
					continue;
				}
				if (spikes[i] == 0){
					continue; // Could be break.
				}
				else if (i < spikeDurations.length - 1){
					builder.append((spikes[i] - spikes[i + 1]) + "x" + spikeDurations[i] + "..." + spikeDurations[i + 1] + " | ");
				}
				else{
					builder.append(spikes[i] + "x" + spikeDurations[i] +"... | ");
				}
			}
		}
		builder.append("\n");
		// TPS lag.
		long max = 50L * (1L + TickTask.lagMaxTicks) * TickTask.lagMaxTicks;
		long medium = 50L * TickTask.lagMaxTicks;
		long second = 1200L;
		builder.append("#### TPS lag ####\nPerc.[time]:");
		for (long ms : new long[]{second, medium, max}){
			double lag = TickTask.getLag(ms);
			int p = Math.max(0, (int) ((lag - 1.0) * 100.0));
			builder.append(" " + p + "%[" + StringUtil.fdec1.format((double) ms / 1200.0) + "s]" );
		}
		// Send message.
		sender.sendMessage(builder.toString());
		return true;
	}

}
