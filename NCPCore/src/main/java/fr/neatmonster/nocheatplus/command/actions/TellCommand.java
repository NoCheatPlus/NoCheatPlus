package fr.neatmonster.nocheatplus.command.actions;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

/**
 * For warnings etc.
 * @author mc_dev
 *
 */
public class TellCommand extends BaseCommand {

	public TellCommand(JavaPlugin plugin) {
		super(plugin, "tell", Permissions.COMMAND_TELL);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, final String[] args) {
		if (args.length < 3) return false;
		final String name = args[1].trim();
		final String message = AbstractCommand.join(args, 2);
		tell(name, message);
		return true;
	}

	private void tell(String name, String message) {
		Player player = DataManager.getPlayer(name);
		if (player != null) player.sendMessage(ColorUtil.replaceColors(message));
	}

	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.command.actions.delay.DelayableCommand#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		return null;
	}
	
}
