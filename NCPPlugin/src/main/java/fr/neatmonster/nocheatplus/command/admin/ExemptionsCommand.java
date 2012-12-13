package fr.neatmonster.nocheatplus.command.admin;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

public class ExemptionsCommand extends NCPCommand {

	public ExemptionsCommand(NoCheatPlus plugin) {
		super(plugin, "exemptions", Permissions.ADMINISTRATION_EXEMPTIONS, new String[]{"exe"});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (args.length != 2) return false;
		String playerName = args[1].trim();
		Player player = Bukkit.getPlayerExact(playerName);
		if (player != null) playerName = player.getName(); 
		final List<String> entries = new LinkedList<String>();
		for (CheckType type : CheckType.values()){
			if (NCPExemptionManager.isExempted(playerName, type)) entries.add(type.toString());
		}
		if (entries.isEmpty()) sender.sendMessage(TAG + "No exemption entries available for " + playerName +" .");
		else sender.sendMessage(TAG + "Exemptions for " + playerName + ": " + CheckUtils.join(entries, ", "));
		return true;
	}

}
