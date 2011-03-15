package cc.co.evenprime.bukkit.nocheat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.listeners.NoCheatBlockListener;
import cc.co.evenprime.bukkit.nocheat.listeners.NoCheatEntityListener;
import cc.co.evenprime.bukkit.nocheat.listeners.NoCheatPlayerListener;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;

/**
* 
* NoCheatPlugin
* 
* Check various player events for their plausibilty and log/deny them based on configuration
* 
* @author Evenprime
*/
public class NoCheatPlugin extends JavaPlugin {
	
	// Various listeners needed for different Checks
    private NoCheatPlayerListener playerListener;
    private NoCheatBlockListener blockListener;
    private NoCheatEntityListener entityListener;

    // My main logger
    private static Logger consoleLogger;
    private static Logger fileLogger;
    
    private static NoCheatPlugin p;
    
    // Permissions 2.0, if available
    public static PermissionHandler Permissions = null;
    
    // CraftIRC 2.0, if available
    public static CraftIRC Irc = null;
       
    // Store data between Events
    public static Map<Player, NoCheatData> playerData = new HashMap<Player, NoCheatData>();

    public NoCheatPlugin() { 
    	p = this;
    }
    
    /**
     * Main access to data that needs to be stored between different events.
     * Always returns a NoCheatData object, because if there isn't one
     * for the specified player, one will be created.
     * 
     * @param p
     * @return
     */
    public static NoCheatData getPlayerData(Player p) {
    	NoCheatData data = null;
    	
		if((data = playerData.get(p)) == null ) {
			synchronized(playerData) {
				data = playerData.get(p);
				if(data == null) {
					// If we have no data for the player, create some
					data = new NoCheatData();
					playerData.put(p, data);
				}
			}
		}
		
		return data;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
    {
    	if(sender instanceof Player) {
    		if(!hasPermission((Player)sender, "nocheat.p")) {
    			sender.sendMessage("NC: You are not allowed to use this command.");
    			return false;
    		}
    	}

        if(args.length == 0) {
        	sender.sendMessage("NC: Using "+ ((Permissions == null) ? "isOp()" : "Permissions") + ". Activated checks/bugfixes: " + getActiveChecksAsString());
        	return true;
        }
        else if(args.length == 1 && args[0] != null && args[0].trim().equals("-p")) { 
        	if(sender instanceof Player) {
        		Player p = (Player) sender;
        		
        		sender.sendMessage("NC: You have permissions: " + getPermissionsForPlayerAsString(p));
        		return true;
        	}
        	else {
        		sender.sendMessage("NC: You have to be a player to use this command");
        		return true;
        	}
        }
        else if(args.length == 2 && args[0] != null && args[0].trim().equals("-p")) {
        	Player p = getServer().getPlayer(args[1]);
        	
        	if(p != null) {
        		sender.sendMessage("NC: "+p.getName() + " has permissions: " + getPermissionsForPlayerAsString(p));
        		return true;
        	}
        	else {
        		sender.sendMessage("NC: Player " + args[1] + " was not found.");
        		return true;
        	}
        }
        
        return false;
    }



	public void onDisable() { 

		
    	PluginDescriptionFile pdfFile = this.getDescription();
    	Logger.getLogger("Minecraft").info( "[NoCheatPlugin] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    public void onEnable() {
    	// Create our listeners and feed them with neccessary information
    	playerListener = new NoCheatPlayerListener();
    	blockListener  = new NoCheatBlockListener();
    	entityListener = new NoCheatEntityListener();

    	fileLogger = NoCheatConfiguration.logger;
    	consoleLogger = Logger.getLogger("Minecraft");
    	    	
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Lowest, this); // used for speedhack and moving checks
    	pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this); // used to delete old data of users
    	pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Low, this); // used for airbuild check
    	pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Lowest, this); // used for moving, speedhack and teleportfrombed check
    	pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.Lowest, this); // used for moving check to reset jumping phase

    	PluginDescriptionFile pdfFile = this.getDescription();
    	
    	// parse the nocheat.yml config file
    	setupConfig();
    			
    	// Get, if available, the Permissions and irc plugin
    	setupPermissions();
    	setupIRC();
    	    	    	   	
    	Logger.getLogger("Minecraft").info( "[NoCheatPlugin] version [" + pdfFile.getVersion() + "] is enabled with the following checks: "+getActiveChecksAsString());
    }

	/**
     * Get, if available, a reference to the Permissions-plugin
     */
    public void setupPermissions() {
    	PermissionHandler p = null;

    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

    	if(test != null && test instanceof Permissions) {
    		p = ((Permissions)test).getHandler();
    		if(p == null) {
    			this.getServer().getPluginManager().enablePlugin(test);
    		}
    		p = ((Permissions)test).getHandler();
    	}

    	if(p == null) {
        	PluginDescriptionFile pdfFile = this.getDescription();
        	Logger.getLogger("Minecraft").warning("[NoCheatPlugin] version [" + pdfFile.getVersion() + "] couldn't find Permissions plugin. Fallback to 'isOp()' equals 'nocheat.*'");
    	}

    	Permissions = p;
    }
    
    /**
     * Get, if available, a reference to the Permissions-plugin
     */
    public void setupIRC() {
    	CraftIRC p = null;

    	Plugin test = this.getServer().getPluginManager().getPlugin("CraftIRC");

    	if(test != null && test instanceof CraftIRC) {
    		p = (CraftIRC)test;
    	}
    	
    	if(p == null) {
        	PluginDescriptionFile pdfFile = this.getDescription();
        	Logger.getLogger("Minecraft").warning("[NoCheatPlugin] version [" + pdfFile.getVersion() + "] couldn't find CrafTIRC plugin. Disabling logging to IRC.");
    	}

    	Irc = p;
    }
        
    /**
     * Log a violation message to all locations declared in the config file
     * @param message
     */
    private static void log(Level l, String message) {
    	if(l != null) {
	    	logToChat(l, message);
	    	logToIRC(l, message);
	    	logToConsole(l, message);
	    	fileLogger.log(l, message);
    	}
    }
    
    
    private static void logToChat(Level l, String message) {
    	if(NoCheatConfiguration.chatLevel.intValue() <= l.intValue()) {
    		for(Player player : p.getServer().getOnlinePlayers()) {
    			if(hasPermission(player, "nocheat.notify")) {
    				player.sendMessage("["+l.getName()+"] " + message);
    			}
    		}
    	}
    }
    private static void logToIRC(Level l, String message) {
    	if(Irc != null && NoCheatConfiguration.ircLevel.intValue() <= l.intValue()) {
    		Irc.sendMessageToTag("["+l.getName()+"] " + message , NoCheatConfiguration.ircTag);
    	}
    }
    
    private static void logToConsole(Level l, String message) {
    	if( NoCheatConfiguration.consoleLevel.intValue() <= l.intValue()) {
    		consoleLogger.log(l, message);
    	}
    }
    
    public static void logAction(String actions, String message) {
    	if(actions == null) return;
		
		// LOGGING IF NEEDED AND WHERE NEEDED
		Level logLevel = null;
				
		if(actions.contains("loglow")) {
			logLevel = Level.INFO;
		}
		if(actions.contains("logmed")) {
			logLevel = Level.WARNING;
		}
		if(actions.contains("loghigh")) {
			logLevel = Level.SEVERE;
		}
		
		if(logLevel != null) {
			NoCheatPlugin.log(logLevel, "NC: "+message);
		}
    }
    
    public static boolean hasPermission(Player player, String permission) {

    	if(player == null || permission == null) {
    		return false;
    	}

    	if(NoCheatPlugin.Permissions != null && NoCheatPlugin.Permissions.has(player, permission))
    		return true;
    	else if(NoCheatPlugin.Permissions == null && player.isOp())
    		return true;
    	else
    		return false;
    }
    
    /**
     * Read the config file
     */
    private void setupConfig() {
    	NoCheatConfiguration.config(new File("plugins/NoCheat/nocheat.yml"));
    }
    

    private String getActiveChecksAsString() {
    	return (NoCheatConfiguration.movingCheckActive ? "moving ": "") + 
        (NoCheatConfiguration.speedhackCheckActive ? "speedhack " : "") +
        (NoCheatConfiguration.airbuildCheckActive ? "airbuild " : "") +
		(NoCheatConfiguration.bedteleportCheckActive ? "bedteleport " : "");
	}
    

	private String getPermissionsForPlayerAsString(Player p) {
		return (!NoCheatConfiguration.movingCheckActive ? "moving* ": (hasPermission(p, "nocheat.moving") ? "moving " : "") + 
        (!NoCheatConfiguration.speedhackCheckActive ? "speedhack* " : (hasPermission(p, "nocheat.speedhack") ? "speedhack " : "")) +
        (!NoCheatConfiguration.airbuildCheckActive ? "airbuild* " : (hasPermission(p, "nocheat.airbuild") ? "airbuild " : "")) +
		(!NoCheatConfiguration.bedteleportCheckActive ? "bedteleport* " : (hasPermission(p, "nocheat.bedteleport") ? "bedteleport " : "")) +
		(hasPermission(p, "nocheat.notify") ? "notify " : ""));

	}
}