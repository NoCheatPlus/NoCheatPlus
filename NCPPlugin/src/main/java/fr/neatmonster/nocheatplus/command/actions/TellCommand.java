package fr.neatmonster.nocheatplus.command.actions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.command.DelayableCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

/**
 * For warnings etc.
 * @author mc_dev
 *
 */
public class TellCommand extends DelayableCommand {

	public TellCommand(NoCheatPlus plugin) {
		super(plugin, "tell", Permissions.ADMINISTRATION_TELL);
	}

	@Override
	public boolean execute(CommandSender sender, Command command, String label,
			final String[] alteredArgs, long delay) {
		if (alteredArgs.length < 3) return false;
		final String name = alteredArgs[1].trim();
		final String message = join(alteredArgs, 2);
		schedule(new Runnable() {
			@Override
			public void run() {
				tell(name, message);
			}
		}, delay);
		return true;
	}

	private void tell(String name, String message) {
		Player player = DataManager.getPlayer(name);
		if (player != null) player.sendMessage(ColorUtil.replaceColors(message));
	}

}
