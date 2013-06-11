package fr.neatmonster.nocheatplus.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Just an interface for sub commands, for future use.
 * @author mc_dev
 *
 */
public abstract class NCPCommand implements TabExecutor{
	
	protected static final String TAG = CommandHandler.TAG;
	
	/**
	 * Convenience method: join with a space in between.
	 * @param args
	 * @param startIndex
	 * @return
	 */
	public static String join(String[] args, int startIndex){
		return join(args, startIndex, " ");
	}
	
	/**
	 * Convenience method.
	 * @param args
	 * @param startIndex
	 * @return
	 */
	public static String join(String[] args, int startIndex, String sep){
		StringBuilder b = new StringBuilder(100);
		if (startIndex < args.length) b.append(args[startIndex]);
		for (int i = startIndex + 1; i < args.length; i++){
			b.append(sep);
			b.append(args[i]);
		}
		return b.toString();
	}
	
	/** Just a plugin reference. */
	protected Plugin plugin;
	
	/** The sub command label. */
	public final String label;
	
	/** Command aliases (important if this is a sub-command). */
	public final String[] aliases;

	/** The command permission. */
	public String permission;
	
	public NCPCommand(JavaPlugin plugin, String label, String permission){
		this(plugin, label, permission, null);
	}

	public NCPCommand(JavaPlugin plugin, String label, String permission, String[] aliases){
		this.plugin = plugin;
		this.label = label;
		this.permission = permission;
		this.aliases = aliases;
	}
	
	/**
	 * As with CommandExecutor, just to have the argument names correctly.
	 */
	public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
	
	/**
	 * 
	 * @param sender
	 * @param command
	 * @param alias
	 * @param args
	 * @return
	 */
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		return null;
	}
}
