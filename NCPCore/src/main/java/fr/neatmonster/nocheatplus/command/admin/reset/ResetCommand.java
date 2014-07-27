package fr.neatmonster.nocheatplus.command.admin.reset;

import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.admin.reset.counters.CountersCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Reset stuff, e.g. statistics counters.
 * @author dev1mc
 *
 */
public class ResetCommand extends BaseCommand{

	public ResetCommand(JavaPlugin plugin) {
		super(plugin, "reset", Permissions.COMMAND_RESET);
		addSubCommands(
			new CountersCommand(plugin)
			);
	}

}
