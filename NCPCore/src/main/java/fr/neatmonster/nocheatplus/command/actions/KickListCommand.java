package fr.neatmonster.nocheatplus.command.actions;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class KickListCommand extends BaseCommand {

	public KickListCommand(JavaPlugin plugin) {
		super(plugin, "kicklist", Permissions.COMMAND_KICKLIST);
		}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String[] kicked = NCPAPIProvider.getNoCheatPlusAPI().getLoginDeniedPlayers();
		if (kicked.length < 100) Arrays.sort(kicked);
		sender.sendMessage(TAG + "Temporarily kicked players:");
		sender.sendMessage(StringUtil.join(Arrays.asList(kicked), " "));
		return true;
	}

}
