package fr.neatmonster.nocheatplus.command;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Just an interface for sub commands, for future use.
 * @author mc_dev
 *
 */
public abstract class BaseCommand extends AbstractCommand<JavaPlugin>{
	

    /** The prefix of every message sent by NoCheatPlus. */
    public static final String TAG = ChatColor.RED + "NCP: " + ChatColor.WHITE;
	
	public BaseCommand(JavaPlugin plugin, String label, String permission){
		this(plugin, label, permission, null);
	}

	public BaseCommand(JavaPlugin access, String label, String permission, String[] aliases){
		super(access, label, permission, aliases);
	}

}
