package fr.neatmonster.nocheatplus.command.admin;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

public class ExemptCommand extends NCPCommand {

	public ExemptCommand(NoCheatPlus plugin) {
		super(plugin, "exempt", Permissions.ADMINISTRATION_EXEMPT);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		// TODO: Reduce copy and paste by introducing some super class.
		if (args.length < 2 || args.length > 3) return false;
		String playerName = args[1];
		final CheckType checkType;
		if (args.length == 3){
			try{
				checkType = CheckType.valueOf(args[2].toUpperCase().replace('-', '_').replace('.', '_'));
			} catch (Exception e){
				sender.sendMessage(TAG + "Could not interpret: " + args[2]);
				sender.sendMessage(TAG + "Check type should be one of: " + CheckUtils.join(Arrays.asList(CheckType.values()), " | "));
				return true;
			}
		}
		else checkType = CheckType.ALL;
		
		final Player player = Bukkit.getPlayerExact(playerName);
		if (player == null){
			sender.sendMessage(TAG + "Player not online: " + playerName);
			return true;
		}
		else playerName = player.getName();
		NCPExemptionManager.exemptPermanently(player, checkType);
		sender.sendMessage(TAG + "Player " + playerName + " is now exempted from: " + checkType); 
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		// At least complete CheckType
		if (args.length == 3) return CommandUtil.getCheckTypeTabMatches(args[2]);
		return null;
	}

}
