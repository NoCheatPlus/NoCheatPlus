package fr.neatmonster.nocheatplus.command.admin;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.command.NCPCommand;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class ExemptionsCommand extends NCPCommand {

	public ExemptionsCommand(JavaPlugin plugin) {
		super(plugin, "exemptions", Permissions.ADMINISTRATION_EXEMPTIONS, new String[]{"exe"});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (args.length != 2) return false;
		String playerName = args[1].trim();
		Player player = DataManager.getPlayer(playerName);
		if (player != null) playerName = player.getName(); 
		final List<String> entries = new LinkedList<String>();
		for (CheckType type : CheckType.values()){
			if (NCPExemptionManager.isExempted(playerName, type)) entries.add(type.toString());
		}
		if (entries.isEmpty()) sender.sendMessage(TAG + "No exemption entries available for " + playerName +" .");
		else sender.sendMessage(TAG + "Exemptions for " + playerName + ": " + StringUtil.join(entries, ", "));
		return true;
	}

}
