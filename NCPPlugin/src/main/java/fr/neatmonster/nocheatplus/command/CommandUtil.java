package fr.neatmonster.nocheatplus.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.logging.LogUtil;

public class CommandUtil {
	
	/**
	 * Return plugin + server commands [Subject to change].
	 * @return Returns null if not CraftBukkit or CommandMap not available.
	 */
	public static CommandMap getCommandMap(){
		// TODO: compat / null
		try{
			return NoCheatPlus.getMCAccess().getCommandMap();
		}
		catch(Throwable t){
			LogUtil.logSevere(t);
			return null;
		}
	}
	
	/**
	 * Fails with an exception if SimpleCommandMap is not found, currently.
	 * @return
	 */
	public static Collection<Command> getCommands(){
		CommandMap commandMap = getCommandMap();
		if (commandMap != null && commandMap instanceof SimpleCommandMap){
			return ((SimpleCommandMap) commandMap).getCommands();
		}
		else{
			final Collection<Command> commands = new LinkedHashSet<Command>(100);
			for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()){
				if (plugin instanceof JavaPlugin){
					final JavaPlugin javaPlugin = (JavaPlugin) plugin;
					final Map<String, Map<String, Object>> map = javaPlugin.getDescription().getCommands();
					if (map != null){
						for (String label : map.keySet()){
							Command command = javaPlugin.getCommand(label);
							if (command != null) commands.add(command);
						}
					}
				}
			}
			// TODO: Vanilla / CB commands !?
			return commands;
		}
	}
	
	/**
	 * Get the command label (trim + lower case), include server commands [subject to change].
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

	/**
	 * Get a command, include server commands [subject to change].
	 * @param alias
	 * @return
	 */
	public static Command getCommand(final String alias) {
		final String lcAlias = alias.trim().toLowerCase();
		final CommandMap map = getCommandMap();
		if (map != null){
			return map.getCommand(lcAlias);
		} else {
			// TODO: maybe match versus plugin commands.
			return null;
		}
	}

	/**
	 * Match for CheckType, some smart method, to also match after first "_" for convenience of input. 
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
