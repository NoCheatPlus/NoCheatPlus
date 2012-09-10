package fr.neatmonster.nocheatplus.command;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

public class RemovePlayerCommand extends NCPCommand {

	public RemovePlayerCommand(NoCheatPlus plugin) {
		super(plugin, "removeplayer", Permissions.ADMINISTRATION_REMOVEPLAYER, new String[]{
			"remove",	
		});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (args.length < 2 || args.length > 3) return false;
		String playerName = args[1];
		final CheckType checkType;
		if (args.length == 3){
			try{
				checkType = CheckType.valueOf(args[2].toUpperCase().replace('-', '_'));
			} catch (Exception e){
				sender.sendMessage(TAG + "Could not interpret: " + args[2]);
				sender.sendMessage(TAG + "Check type should be one of: " + CheckUtils.join(Arrays.asList(CheckType.values()), " | "));
				return true;
			}
		}
		else checkType = CheckType.ALL;
		if (CheckType.removeData(playerName, checkType))
			sender.sendMessage(TAG + "Removed data (" + checkType + "): " + playerName);
		else
			sender.sendMessage(TAG + "No data present (" + checkType + ", exact spelling): " + playerName);
		return true;
	}

}
