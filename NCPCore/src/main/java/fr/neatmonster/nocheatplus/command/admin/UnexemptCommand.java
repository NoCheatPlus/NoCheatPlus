package fr.neatmonster.nocheatplus.command.admin;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class UnexemptCommand extends NCPCommand {

	public UnexemptCommand(JavaPlugin plugin) {
		super(plugin, "unexempt", Permissions.ADMINISTRATION_UNEXEMPT);
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
				sender.sendMessage(TAG + "Check type should be one of: " + StringUtil.join(Arrays.asList(CheckType.values()), " | "));
				return true;
			}
		}
		else checkType = CheckType.ALL;
		if (playerName.equals("*")){
			// Unexempt all.
			// TODO: might care to find players only ?
			NCPExemptionManager.clear();
			sender.sendMessage(TAG + "Nobody will be exempted from: " + checkType);
			return true;
		}
		// Find player.
		final Player player = DataManager.getPlayer(playerName);
		if (player != null) playerName = player.getName();
		NCPExemptionManager.unexempt(playerName, checkType);
		sender.sendMessage(TAG + "Player " + playerName + " will not be exempted from: " + checkType);
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
