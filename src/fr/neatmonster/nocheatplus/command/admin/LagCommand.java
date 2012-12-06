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
		// TODO: lag spikes !
		builder.append("Lag tracking (roughly):");
		builder.append("\nAverage lag:");
		for (long ms : new long[]{second, medium, max}){
			double lag = TickTask.getLag(ms);
			int p = Math.max(0, (int) ((lag - 1.0) * 100.0));
			builder.append(" " + p + "%[" + CheckUtils.fdec1.format((double) ms / 1200.0) + "s]" );
		}
		builder.append("\nLast hour spikes: ");
		int spikesM = TickTask.getModerateLagSpikes();
		builder.append((spikesM > 0 ? (" | " + spikesM) : " | none") + " over 150 ms");
		int spikesH = TickTask.getHeavyLagSpikes();
		builder.append((spikesH > 0 ? (" | " + spikesH) : " | none") + " of which over 1 s");
		sender.sendMessage(builder.toString());
		return true;
	}

}
