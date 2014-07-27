package fr.neatmonster.nocheatplus.command.actions;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

public class KickCommand extends BaseCommand {

	public KickCommand(JavaPlugin plugin) {
		super(plugin, "kick", Permissions.COMMAND_KICK);
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if (!demandConsoleCommandSender(sender)) {
			return true;
		}
		// Args contains "kick" as first arg.
		if (args.length < 2) return false;
		final String name = args[1];
		final String reason;
		if (args.length > 2) reason = AbstractCommand.join(args, 2);
		else reason = "";
		kick(sender, name, reason);
		return true;
	}
	
	void kick(CommandSender sender, String name, String reason) {
		Player player = DataManager.getPlayer(name);
		if (player == null) return;
		player.kickPlayer(reason);
		LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Kicked " + player.getName() + " : " + reason);
	}

	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.command.AbstractCommand#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		return null;
	}
	
}
