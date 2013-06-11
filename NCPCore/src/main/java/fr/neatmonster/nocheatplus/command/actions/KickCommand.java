package fr.neatmonster.nocheatplus.command.actions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.actions.delay.DelayableCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

public class KickCommand extends DelayableCommand {

	public KickCommand(JavaPlugin plugin) {
		super(plugin, "kick", Permissions.ADMINISTRATION_KICK);
	}

	@Override
	public boolean execute(final CommandSender sender, Command command, String label,
			String[] alteredArgs, long delay) {
		// Args contains "kick" as first arg.
		if (alteredArgs.length < 2) return false;
		final String name = alteredArgs[1];
		final String reason;
		if (alteredArgs.length > 2) reason = AbstractCommand.join(alteredArgs, 2);
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
		Player player = DataManager.getPlayer(name);
		if (player == null) return;
		player.kickPlayer(reason);
		LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Kicked " + player.getName() + " : " + reason);
	}

}
