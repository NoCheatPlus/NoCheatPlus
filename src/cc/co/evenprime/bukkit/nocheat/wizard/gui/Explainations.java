package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.util.HashMap;
import java.util.Map;

public class Explainations {

	private static final Map<String, String> explainations = new HashMap<String,String>();
	
	static {
		
		set("active.moving", "If activated, players will be checked for moving in illegal ways, e.g. flying, making bigger\n" +
				"steps than possible, teleporting themselves by manipulating their client to give the server wrong coordinates.\n" +
				"You can further individualize this check in the 'moving' section of the config file.\n" +
				"By default OPs are not checked (can be changed in the 'moving' section).\n" +
				"(Permissions plugin): Only players that don't have the permission 'nocheat.moving' will be checked.");
		set("active.speedhack", "If activated, players will be checked for sending too many player_move events per second.\n" +
				"Players can intentionally send too many player_move events to move faster than normal.\n" +
				"You can further individualize this check in the 'speedhack' section of the config file.\n" +
				"By default OPs are not checked (can be changed in the 'speedhack' section).\n" +
				"(Permissions plugin): Only players that don't have the permission 'nocheat.speedhack' will be checked.");
		set("active.airbuild", "If activated, players will be checked for placing blocks against air, which is normally not possible.\n" +
				"You can further individualize this check in the 'airbuild' section of the config file.\n" +
				"By default OPs are not checked (can be changed in the 'airbuild' section).\n" +
				"(Permissions plugin): Only players that don't have the permission 'nocheat.airbuild' will be checked.");
		set("active.bedteleport", "If activated, players will be prevented from teleporting when lying in beds.\n" +
				"Teleporting while in bed can cause a graphical glitch of horizontally floating players.\n" +
				"By default OPs are not checked (can be changed in the 'bedteleport' section).\n" +
				"(Permissions plugin): Only players that don't have the permission 'nocheat.bedteleport' will be checked.");
		set("active.bogusitems", "If activated, items with invalid attributes will be deleted from players inventories at various ocasions.\n" +
				"Invalid attributes like a negative stack-size allow for infinitely usable items and/or item duplication.\n" +
				"If one of your plugins intentionally produces such bogus items, you should not activate this (or you'll likely lose them).\n" +
				"By default OPs are not checked (can be changed in the 'bogusitems' section).\n" +
				"(Permissions plugin): Only players that don't have the permission 'nocheat.bogusitems' will be checked.");
		set("active.nuke", "If activated, players will no longer be allowed to destroy blocks that are not in front of them.\n" +
				"Because this is done by most 'nuke' hack-clients, it is easy to prevent (for now). If a player tries destroying\n" +
				"blocks that are outside his field of sight, he'll get kicked from the server.\n" +
				"This is only a temporary solution (and will probably not hold for long), but it's better than nothing, I guess...");
		
		set("logging.filename", "Determines where the various messages by NoCheat are stored at, if logging to file is activated.");
		set("logging.logtofile", "Determine what severeness messages need to have to be printed to the logfile.\n" +
				"The values that can be used are:\n" +
				"off = no logging at all,\n" +
				"low = log all low/med/high severeness messages,\n" +
				"med = log only med/high severeness messages and\n" +
				"high = only log high severeness messages.");
		set("logging.logtoconsole", "Determine what severeness messages need to have to be printed to minecrafts server console.\n" +
				"The values that can be used are:\n" +
				"off = no logging at all,\n" +
				"low = log all low/med/high severeness messages,\n" +
				"med = log only med/high severeness messages and\n" +
				"high = only log high severeness messages.");
		set("logging.logtochat", "Determine what severeness messages need to have to be printed to the games chat.\n" +
				"Only OPs will get these messages.\n" +
				"The values that can be used are:\n" +
				"off = no logging at all,\n" +
				"low = log all low/med/high severeness messages,\n" +
				"med = log only med/high severeness messages and\n" +
				"high = only log high severeness messages.\n" +
				"By default only OPs get these messages.\n" +
				"(Permissions plugin): Only players with the permission 'nocheat.notify', will get these messages.");
		set("logging.logtoirc", "(CraftIRC plugin) Determine what severeness messages need to have to be printed to the IRC channel.\n" +
				"The values that can be used are\n" +
				"The values that can be used are:\n" +
				"off = no logging at all,\n" +
				"low = log all low/med/high severeness messages,\n" +
				"med = log only med/high severeness messages and\n" +
				"high = only log high severeness messages.\n" +
				"med = log only med/high messages and high = only log high messages.");
		set("logging.logtoirctag", "(CraftIRC plugin only)  Determine the tag used for the messages by nocheat.");
		
		set("speedhack.logmessage", "Customize the log message used in case of a player speedhacking.\n" +
				"[player] will be replaced with the name of the player,\n" +
				"[events] will be replaced with the number of move events per second of that player,\n" +
				"[limit] will be replaced with the the lowest limit set by you.");
		set("speedhack.checkops", "Also check players with OP-status, unless there is another reason\n" +
				"to not check them, e.g. they got the relevant permission from a Permissions plugin.");
		set("speedhack.limits.low", "The number of move events per second that have to be sent by a player\n" +
				"to consider it cheating and execute the actions defined at 'speedhack.action.low'.\n" +
				"Never use a number < 20, because 20 is the number of legitimate move events a player can produce.");
		set("speedhack.limits.med", "The number of move events per second that have to be sent by a player\n" +
				"to consider it cheating and execute the actions defined at 'speedhack.action.med'.\n" +
				"Never use a number < 20, because 20 is the number of legitimate move events a player can produce.");
		set("speedhack.limits.high", "The number of move events per second that have to be sent by a player\n" +
				"to consider it cheating and execute the actions defined at 'speedhack.action.high'.\n" +
				"Never use a number < 20, because 20 is the number of legitimate move events a player can produce.");
		
		set("speedhack.action.low", "Execute these actions when 'speedhack.limits.low' is reached.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = teleport the player back to where he was the second before, and any of your custom actions.");
		set("speedhack.action.med", "Execute these actions when 'speedhack.limits.med' is reached.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = teleport the player back to where he was the second before, and any of your custom actions.");
		set("speedhack.action.high", "Execute these actions when 'speedhack.limits.high' is reached.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = teleport the player back to where he was the second before, and any of your custom actions.");
		
		set("moving.logmessage", "The message that gets logged in case of 'move' violations.\n" +
				"[player] will be replaced with the name of the player,\n" +
				"[world] will be replaced with the name of the world the player was in at that time,\n" +
				"[from] will be replaced with the the location the player came from,\n" +
				"[to] will be replaced with the location the player went to,\n" +
				"[distance] will be replaced with the distance the player moved in each direction.");
		set("moving.summarymessage", "The message that gets logged some seconds after a 'move' violation\n" +
				"and gives an overview of what else happend during that timespan (logging each violation\n" +
				"would be too spammy, therefore this approach was chosen as a middlepath).\n" +
				"[player] will be replaced with the name of the player,\n" +
				"[timeframe] will be replaced with the number of seconds that were observed,\n" +
				"[violations] will be replaced with the number of violations of each severeness during that time.");
		set("moving.summaryafter", "After how many seconds should a summary of all violations in that timeframe\n" +
				"be displayed?");
		set("moving.allowflying", "If true, all players are allowed to fly (at normal walking speeds)\n" +
				"(Permissions plugin): Players with permission 'nocheat.flying' are allowed to fly independent of\n" +
				"the setting of this option.");
		set("moving.allowfakesneak", "If true, players are allowed to move at normal walking speeds\n" +
				"while sneaking. If you use plugins that set the 'sneak'-status for the player (e.g. Hero-Sneak),\n" +
				"you probably want to enable this option/set it to true.\n" +
				"(Permissions plugin): Players with permission 'nocheat.fakesneak' are allowed to move at normal" +
				"walking speeds while sneaking independent of the setting of this option.");
		set("moving.allowfastswim", "If true, all players are allowed to swim at normal walking speeds,\n" +
				"instead of being forced to move at the usually slower swimming speed.\n" +
				"(Permissions plugin): Players with permission 'nocheat.fastswim' are allowed to move at normal" +
				"walking speeds while swimming independent of the setting of this option.");
		set("moving.waterelevators", "If true, using Minecraft 1.4/1.5 style water elevators will be allowed.\n" +
				"Set this to true if your players use clientside mods that allow the use of those old-school\n" +
				"water elevators and you don't want this plugin to prevent them from doing that.");
		
		set("moving.checkops", "Also check players with OP-status, unless there is another reason\n" +
				"to not check them, e.g. they got the relevant permission from a Permissions plugin.");
		
		set("moving.enforceteleport", "Enforce teleports made by the NoCheat plugin by overruling decisions\n" +
				"of other plugins to prevent/cancel the teleport. This is usually a 'not-so-nice' thing to do,\n" +
				"but sometimes the only way to get the plugin to work properly in combination with others.");
		
		set("moving.action.low", "Execute these actions when a player moves further/higher in one step than the\n" +
				"limits defined by this plugin allow.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = teleport the player back to where he was right before, and any of your custom actions.");
		set("moving.action.med", "Execute these actions when a player moves further/higher in one step than the\n" +
				"limits defined by this plugin + 0.5 blocks allow.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = teleport the player back to where he was right before, and any of your custom actions.");
		set("moving.action.high", "Execute these actions when a player moves further/higher in one step than the\n" +
				"limits defined by this plugin + 2.0 blocks allow.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = teleport the player back to where he was right before, and any of your custom actions.");
		
		set("airbuild.checkops", "Also check players with OP-status, unless there is another reason\n" +
				"to not check them, e.g. they got the relevant permission from a Permissions plugin.");
		set("airbuild.limits.low", "The number of blocks per second a player places in midair\n" +
				"to be considered cheating and execute the actions defined at 'airbuild.action.low'.");
		set("airbuild.limits.med", "The number of blocks per second a player places in midair\n" +
				"to be considered cheating and execute the actions defined at 'airbuild.action.med'.");
		set("airbuild.limits.high", "The number of blocks per second a player places in midair\n" +
				"to be considered cheating and execute the actions defined at 'airbuild.action.high'.");
		
		set("airbuild.action.low", "Execute these actions when 'airbuild.limits.low' is reached.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = prevent the player from placing the block, and any of your custom actions.");
		set("airbuild.action.med", "Execute these actions when 'airbuild.limits.med' is reached.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = prevent the player from placing the block, and any of your custom actions.");
		set("airbuild.action.high", "Execute these actions when 'airbuild.limits.high' is reached.\n" +
				"Actions are executed in order. Available actions are loglow = log a message with low severeness,\n" +
				"logmed = log a message with medium severeness, loghigh = log a message with high severeness,\n" +
				"cancel = prevent the player from placing the block, and any of your custom actions.");
		
		set("bogusitems.checkops", "Also check players with OP-status, unless there is another reason\n" +
				"to not check them, e.g. they got the relevant permission from a Permissions plugin.");
		
		set("bedteleport.checkops", "Also check players with OP-status, unless there is another reason\n" +
				"to not check them, e.g. they got the relevant permission from a Permissions plugin.");
		
		set("nuke.logmessage", "The message that appears in your logs if somebody gets kicked for trying to nuke\n" +
				"the server map");
		set("nuke.kickmessage", "The message that is shown to players that get kicked for nuking");
		set("nuke.checkops",  "Also check players with OP-status, unless there is another reason\n" +
				"to not check them, e.g. they got the relevant permission from a Permissions plugin.");
		set("nuke.limitreach",  "Deny blockbreaking over longer distances than the standard minecraft\n" +
				"client allows.");
	}
	
	private static void set(String id, String text) {
		explainations.put(id, text);	
	}
	
	
	public static String get(String id) {
		String result = explainations.get(id);
		
		if(result == null) {
			result = "No description available";
		}
		
		return result;
	}
	
	
	
}
