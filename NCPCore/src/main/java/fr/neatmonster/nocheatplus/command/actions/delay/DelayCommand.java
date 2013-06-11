package fr.neatmonster.nocheatplus.command.actions.delay;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Delay an arbitrary command, the command is always delayed, unless for bad delay input.
 * @author mc_dev
 *
 */
public class DelayCommand extends DelayableCommand {

	public DelayCommand(JavaPlugin plugin){
		super(plugin, "delay", Permissions.ADMINISTRATION_DELAY, 1, 0, true);
	}
	
	@Override
	public boolean execute(CommandSender sender, Command command, String label,
			String[] alteredArgs, long delay) {
		if (alteredArgs.length < 2) return false;
		final String cmd = AbstractCommand.join(alteredArgs, 1);
		schedule(new Runnable() {
			@Override
			public void run() {
				Server server = Bukkit.getServer();
				server.dispatchCommand(server.getConsoleSender(), cmd);
			}
		}, delay);
		return true;
	}

}
