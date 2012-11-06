package fr.neatmonster.nocheatplus.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;

public class CommandUtil {
	
	public static SimpleCommandMap getCommandMap(){
		return (((CraftServer) Bukkit.getServer()).getCommandMap());
	}
	
	/**
	 * Get the command label (trim + lower case).
	 * @param alias
	 * @param strict If to return null if no command is found.
	 * @return
	 */
	public static String getCommandLabel(final String alias, final boolean strict){
		final Command command = getCommand(alias);
		if (command == null){
			return strict ? null : alias.trim().toLowerCase();
		}
		else return command.getLabel().trim().toLowerCase();
	}

	public static Command getCommand(final String alias) {
		final SimpleCommandMap map = getCommandMap();
		final String lcAlias = alias.trim().toLowerCase();
		return map.getCommand(lcAlias);
	}
}
