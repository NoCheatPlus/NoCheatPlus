package fr.neatmonster.nocheatplus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.players.Permissions;

public class KickCommand extends DelayableCommand {

	public KickCommand(NoCheatPlus plugin) {
		super(plugin, "kick", Permissions.ADMINISTRATION_KICK);
	}

	@Override
	public boolean execute(final CommandSender sender, Command command, String label,
			String[] alteredArgs, long delay) {
		// Args contains "kick" as first arg.
		if (alteredArgs.length < 2) return false;
		final String name = alteredArgs[1];
		final String reason;
		if (alteredArgs.length > 2) reason = join(alteredArgs, 2);
		else reason = "";
		schedule(new Runnable() {
			@Override
			public void run() {
				kick(sender, name, reason);
			}
		}, delay);
		return true;
	}
	
	void kick(CommandSender sender, String name, String reason) {
		Player player = Bukkit.getPlayerExact(name);
		if (player == null) return;
		player.kickPlayer(reason);
		System.out.println("[NoCheatPlus] (" + sender.getName() + ") Kicked " + player.getName() + " : " + reason);
	}

}
