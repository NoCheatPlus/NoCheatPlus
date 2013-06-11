package fr.neatmonster.nocheatplus.command.actions;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.actions.delay.DelayableCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

public class BanCommand extends DelayableCommand {

	public BanCommand(JavaPlugin plugin) {
		super(plugin, "ban", Permissions.ADMINISTRATION_BAN);
	}

	@Override
	public boolean execute(final CommandSender sender, Command command, String label,
			String[] alteredArgs, long delay) {
		// Args contains "ban" as first arg.
		if (alteredArgs.length < 2) return false;
		final String name = alteredArgs[1];
		final String reason;
		if (alteredArgs.length > 2) reason = AbstractCommand.join(alteredArgs, 2);
		else reason = "";
		schedule(new Runnable() {
			@Override
			public void run() {
				ban(sender, name, reason);
			}
		}, delay);
		return true;
	}
	
	void ban(CommandSender sender, String name, String reason) {
		Player player = DataManager.getPlayer(name);
		if (player != null)
			player.kickPlayer(reason);
		OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(name);
		offlinePlayer.setBanned(true);
		LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Banned " + offlinePlayer.getName() + " : " + reason);
	}

}
