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
		final String lcAlias = alias.trim().toLowerCase();
		final Command command = getCommandMap().getCommand(alias);
		if (command == null){
			return strict ? null : lcAlias;
		}
		else return command.getLabel().trim().toLowerCase();
	}
}
