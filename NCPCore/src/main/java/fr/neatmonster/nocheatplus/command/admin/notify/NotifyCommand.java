package fr.neatmonster.nocheatplus.command.admin.notify;

import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Toggle notifications on and off.
 * @author mc_dev
 *
 */
public class NotifyCommand extends BaseCommand {

	public NotifyCommand(JavaPlugin plugin) {
		super(plugin, "notify", Permissions.COMMAND_NOTIFY, new String[]{"alert", "alerts"});
		addSubCommands(
			new NotifyOffCommand(plugin),
			new NotifyOnCommand(plugin)
			);
	}
	
}
