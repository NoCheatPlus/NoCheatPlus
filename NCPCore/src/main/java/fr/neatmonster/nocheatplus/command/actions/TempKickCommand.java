package fr.neatmonster.nocheatplus.command.actions;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.command.AbstractCommand;
import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;

public class TempKickCommand extends BaseCommand {

	public TempKickCommand(JavaPlugin plugin) {
		super(plugin, "tempkick", Permissions.COMMAND_TEMPKICK, 
				new String[]{"tkick", "tempban", "tban",});
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		// Args contains sub command label as first arg.
		if (args.length < 3) return false;
		long base = 60000; // minutes (!)
		final String name = args[1];
		long duration = -1;
		try{
			// TODO: parse for abbreviations like 30s 30m 30h 30d, and set base...
			duration = Integer.parseInt(args[2]);
		}
		catch( NumberFormatException e){};
		if (duration <= 0) return false;
		final long finalDuration = duration * base;
		final String reason;
		if (args.length > 3) reason = AbstractCommand.join(args, 3);
		else reason = "";
		tempKick(sender, name, finalDuration, reason);
		return true;
	}

	
	protected void tempKick(CommandSender sender, String name, long duration, String reason){
		Player player = DataManager.getPlayer(name);
		NCPAPIProvider.getNoCheatPlusAPI().denyLogin(name, duration);
		if (player == null) return;
		player.kickPlayer(reason);
		LogUtil.logInfo("[NoCheatPlus] (" + sender.getName() + ") Kicked " + player.getName() + " for " + duration/60000 +" minutes: " + reason);
	}

	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.command.AbstractCommand#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		return null;
	}
	
}
