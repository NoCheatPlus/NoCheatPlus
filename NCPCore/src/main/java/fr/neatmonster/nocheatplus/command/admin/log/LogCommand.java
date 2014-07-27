package fr.neatmonster.nocheatplus.command.admin.log;

import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.admin.log.counters.CountersCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

public class LogCommand extends BaseCommand{

	public LogCommand(JavaPlugin plugin) {
		super(plugin, "log", Permissions.COMMAND_LOG);
		addSubCommands(
			new CountersCommand(plugin)
			);
	}

}
