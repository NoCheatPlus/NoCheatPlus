package fr.neatmonster.nocheatplus.command.actions;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

public class BanCommand extends BaseCommand {

	public BanCommand(JavaPlugin plugin) {
		super(plugin, "ban", Permissions.ADMINISTRATION_BAN);
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		// Args contains "ban" as first arg.
		if (args.length < 2) return false;
		final String name = args[1];
		final String reason;
		if (args.length > 2) reason = AbstractCommand.join(args, 2);
		else reason = "";
		ban(sender, name, reason);
		return true;
	}
	
	void ban(CommandSender sender, String name, String reason) {
		Player player = DataManager.getPlayer(name);
		if (player != null){
			player.kickPlayer(reason);
		}
		OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(name);
		offlinePlayer.setBanned(true);
		LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Banned " + offlinePlayer.getName() + " : " + reason);
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
