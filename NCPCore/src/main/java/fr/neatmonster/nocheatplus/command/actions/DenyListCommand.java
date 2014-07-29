package fr.neatmonster.nocheatplus.command.actions;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class DenyListCommand extends BaseCommand {

	public DenyListCommand(JavaPlugin plugin) {
		super(plugin, "denylist", Permissions.COMMAND_KICKLIST,
		    new String[]{"kicklist", "tempbanned", "deniedlist", "denyloginlist", "deniedlogin"});
		}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String[] kicked = NCPAPIProvider.getNoCheatPlusAPI().getLoginDeniedPlayers();
		if (kicked.length < 100) Arrays.sort(kicked);
		sender.sendMessage(TAG + "Players denied to login (temporarily):");
		sender.sendMessage(StringUtil.join(Arrays.asList(kicked), " "));
		return true;
	}

}
