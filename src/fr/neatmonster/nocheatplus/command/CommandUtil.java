package fr.neatmonster.nocheatplus.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;

import fr.neatmonster.nocheatplus.checks.CheckType;

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

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static List<String> getCheckTypeTabMatches(final String input) {
		final String ref = input.toUpperCase().replace('-', '_').replace('.', '_');
		final List<String> res = new ArrayList<String>();
		for (final CheckType checkType : CheckType.values()){
			final String name = checkType.name();
			if (name.startsWith(ref)) res.add(name);
		}
		if (ref.indexOf('_') == -1){
			for (final CheckType checkType : CheckType.values()){
				final String name = checkType.name();
				final String[] split = name.split("_", 2);
				if (split.length > 1 && split[1].startsWith(ref)) res.add(name);
			}
		}
		if (!res.isEmpty()){
			Collections.sort(res);
			return res;
		}
		return null;
	}
}
