package fr.neatmonster.nocheatplus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.players.Permissions;

public class TempKickCommand extends DelayableCommand {

	public TempKickCommand(NoCheatPlus plugin) {
		super(plugin, "tempkick", Permissions.ADMINISTRATION_TEMPKICK, new String[]{
				"tkick", "tempban", "tban",
		}, 1, -1, false);
	}

	@Override
	public boolean execute(final CommandSender sender, Command command, String label,
			String[] alteredArgs, long delay) {
		// Args contains sub command label as first arg.
		if (alteredArgs.length < 3) return false;
		long base = 60000; // minutes (!)
		final String name = alteredArgs[1];
		long duration = -1;
		try{
			// TODO: parse for abbreviations like 30s 30m 30h 30d, and set base...
			duration = Integer.parseInt(alteredArgs[2]);
		}
		catch( NumberFormatException e){};
		if (duration <= 0) return false;
		final long finalDuration = duration * base;
		final String reason;
		if (alteredArgs.length > 3) reason = join(alteredArgs, 3);
		else reason = "";
		schedule(new Runnable() {
			@Override
			public void run() {
				tempKick(sender, name, finalDuration, reason);
			}
		}, delay);
		return true;
	}

	
	protected void tempKick(CommandSender sender, String name, long duration, String reason){
		Player player = Bukkit.getPlayerExact(name);
		NoCheatPlus.denyLogin(name, duration);
		if (player == null) return;
		player.kickPlayer(reason);
		System.out.println("[NoCheatPlus] (" + sender.getName() + ") Kicked " + player.getName() + " for " + duration/60000 +" minutes: " + reason);
	}
}
