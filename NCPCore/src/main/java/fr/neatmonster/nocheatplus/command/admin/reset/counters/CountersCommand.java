package fr.neatmonster.nocheatplus.command.admin.reset.counters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.stats.Counters;

public class CountersCommand extends BaseCommand {
	
	public CountersCommand(JavaPlugin plugin) {
		super(plugin, "counters", null);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class).resetAll();
		sender.sendMessage("Counters reset.");
		return true;
	}
	
}
