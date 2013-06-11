package fr.neatmonster.nocheatplus.command.admin;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * This command just shows a list of all commands.
 * @author mc_dev
 *
 */
public class CommandsCommand extends BaseCommand {
	
	final String[] moreCommands = new String[]{
	        "/<command> ban [delay=(ticks)] (player) [(reason)...]: ban player",
	        "/<command> kick [delay=(ticks)] (player) [(reason)...]: kick player",
	        "/<command> tempkick [delay=(ticks)] (player) (minutes) [(reason)...]",
	        "/<command> unkick (player): Allow a player to login again.",
	        "/<command> kicklist: Show temporarily kicked players.",
	        "/<command> tell [delay=(ticks)] (player) (message)...: tell a message",
	        "/<command> delay [delay=(ticks)] (command)...: delay a command",
	};
	
	final String allCommands;

	public CommandsCommand(JavaPlugin plugin) {
		super(plugin, "commands", Permissions.ADMINISTRATION_COMMANDS, new String[]{"cmds"});
		for (int i = 0; i < moreCommands.length; i++){
			moreCommands[i] = moreCommands[i].replace("<command>", "ncp");
		}
		String all = TAG + "All commands info:\n";
		Command cmd = plugin.getCommand("nocheatplus");
		if (cmd != null){
			all += cmd.getUsage().replace("<command>", "ncp") + "Auxiliary commands (actions):\n";
		}
		all += StringUtil.join(Arrays.asList(moreCommands), "\n");
		allCommands = all;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		sender.sendMessage(allCommands);
		return true;
	}

}
