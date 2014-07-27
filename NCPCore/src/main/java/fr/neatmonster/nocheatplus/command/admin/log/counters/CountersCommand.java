package fr.neatmonster.nocheatplus.command.admin.log.counters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.stats.Counters;

public class CountersCommand extends BaseCommand {

	public CountersCommand(JavaPlugin plugin) {
		super(plugin, "counters", null); // TODO: Maybe add a permission.
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		sender.sendMessage(NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class).getMergedCountsString(true));
		return true;
	}
	
	

}
