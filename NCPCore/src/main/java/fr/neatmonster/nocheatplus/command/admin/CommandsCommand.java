/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
			"Console commands (actions):",
	        "/<command> ban [delay=(ticks)] (player) [(reason)...]: ban player",
	        "/<command> kick [delay=(ticks)] (player) [(reason)...]: kick player",
	        "/<command> tell [delay=(ticks)] (player) (message)...: tell a message",
	        "/<command> delay [delay=(ticks)] (command)...: delay a command",
	        "/<command> denylogin [delay=(ticks)] (player) (minutes) [(reason)...]",
	        "More administrative commands:",
	        "/<command> log counters: Show some stats/debug counters summary.",
            "/<command> reset counters: Reset some stats/debug counters",
            "/<command> debug player (player): Log debug info for the player.",
	        "/<command> denylist: Show players, currently denied to log in.",
	        "/<command> allowlogin (player): Allow a player to login again.",
	        "/<command> exemptions (player): Show exemptions.",
            "/<command> exempt (player) [(check type)]: Exempt a player.",
            "/<command> unexempt (player) [(check type)]: Unexempt a player.",
	};
	
	final String allCommands;

	public CommandsCommand(JavaPlugin plugin) {
		super(plugin, "commands", Permissions.COMMAND_COMMANDS, new String[]{"cmds"});
		for (int i = 0; i < moreCommands.length; i++){
			moreCommands[i] = moreCommands[i].replace("<command>", "ncp");
		}
		String all = TAG + "All commands info:\n";
		Command cmd = plugin.getCommand("nocheatplus");
		if (cmd != null){
			all += cmd.getUsage().replace("<command>", "ncp");
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
