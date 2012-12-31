package fr.neatmonster.nocheatplus.command.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TickTask;

public class LagCommand extends NCPCommand {

	public LagCommand(NoCheatPlus plugin) {
		super(plugin, "lag", Permissions.ADMINISTRATION_LAG);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		long max = 50L * (1L + TickTask.lagMaxTicks) * TickTask.lagMaxTicks;
		long medium = 50L * TickTask.lagMaxTicks;
		long second = 1200L;
		StringBuilder builder = new StringBuilder(300);
		builder.append("Lag tracking (roughly):");
		builder.append("\nAverage lag:");
		for (long ms : new long[]{second, medium, max}){
			double lag = TickTask.getLag(ms);
			int p = Math.max(0, (int) ((lag - 1.0) * 100.0));
			builder.append(" " + p + "%[" + CheckUtils.fdec1.format((double) ms / 1200.0) + "s]" );
		}
		long[] spikeDurations = TickTask.getLagSpikeDurations();
		int[] spikes = TickTask.getLagSpikes();
		builder.append("\nLast hour spikes (" + spikes[0] + " total, all > " + spikeDurations[0] + " ms):\n| ");
		if (spikes[0] > 0){
			for (int i = 0; i < spikeDurations.length; i++){
				if (i < spikeDurations.length - 1 && spikes[i] == spikes[i + 1]){
					// Ignore these, get printed later.
					continue;
				}
				if (spikes[i] == 0){
					builder.append("none |");
				}
				else if (i < spikeDurations.length - 1){
					builder.append((spikes[i] - spikes[i + 1]) + "x" + spikeDurations[i] + "..." + spikeDurations[i + 1] + " | ");
				}
				else{
					builder.append(spikes[i] + "x" + spikeDurations[i] +"... | ");
				}
			}
		}
		else{
			builder.append("none | ");
		}
		sender.sendMessage(builder.toString());
		return true;
	}

}
