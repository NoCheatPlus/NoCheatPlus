package fr.neatmonster.nocheatplus.command.actions;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class KickListCommand extends NCPCommand {

	public KickListCommand(NoCheatPlus plugin) {
		super(plugin, "kicklist", Permissions.ADMINISTRATION_KICKLIST);
		}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String[] kicked = NoCheatPlus.getLoginDeniedPlayers();
		if (kicked.length < 100) Arrays.sort(kicked);
		sender.sendMessage(TAG + "Temporarily kicked players:");
		sender.sendMessage(StringUtil.join(Arrays.asList(kicked), " "));
		return true;
	}

}
