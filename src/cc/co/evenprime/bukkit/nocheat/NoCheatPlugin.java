package cc.co.evenprime.bukkit.nocheat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.listeners.NoCheatBlockListener;
import cc.co.evenprime.bukkit.nocheat.listeners.NoCheatEntityListener;
import cc.co.evenprime.bukkit.nocheat.listeners.NoCheatPlayerListener;

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
    private static Logger log;
    
    private static NoCheatPlugin p;
    
    // Permissions 2.0, if available
    public static PermissionHandler Permissions = null;
       
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

    public void onDisable() { 
    	PluginDescriptionFile pdfFile = this.getDescription();
    	Logger.getLogger("Minecraft").info( "[NoCheatPlugin] version [" + pdfFile.getVersion() + "] is disabled.");
    }

    public void onEnable() {
    	// Create our listeners and feed them with neccessary information
    	playerListener = new NoCheatPlayerListener();
    	blockListener  = new NoCheatBlockListener();
    	entityListener = new NoCheatEntityListener();

    	log = NoCheatConfiguration.logger;
    	    	
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Lowest, this); // needed for speedhack and moving checks
    	pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this); // used to delete old data of users
    	pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Low, this); // used for airbuild check
    	pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Highest, this); // used for dupebydeath check
    	pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Lowest, this); // used for teleportfrombed check

    	PluginDescriptionFile pdfFile = this.getDescription();
    	
    	// Get, if available, the Permissions plugin
    	setupPermissions();
    	   	
    	// parse the nocheat.yml config file
    	setupConfig();
    	
    	String checks = (NoCheatConfiguration.movingCheckActive ? "moving ": "") + 
    	                (NoCheatConfiguration.speedhackCheckActive ? "speedhack " : "") +
    	                (NoCheatConfiguration.airbuildCheckActive ? "airbuild " : "") +
    	                (NoCheatConfiguration.dupebydeathCheckActive ? "dupebydeath " : "");
    	
    	Logger.getLogger("Minecraft").info( "[NoCheatPlugin] version [" + pdfFile.getVersion() + "] is enabled with the following checks: "+checks);
    }

    /**
     * Get, if available, a reference to the Permissions-plugin
     */
    public void setupPermissions() {
    	Permissions = null;
    	
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

   		if(test != null) {
   			Permissions = ((Permissions)test).getHandler();
   			if(Permissions == null) {
   				this.getServer().getPluginManager().enablePlugin(test);
   			}
    		Permissions = ((Permissions)test).getHandler();
    	}
   		
   		if(Permissions == null) {
    		log.info("Nocheat couldn't find Permissions plugin. Fallback to 'isOp()' equals 'all allowed'.");
    	}
    }
    
    /**
     * Log a violation message to all locations declared in the config file
     * @param message
     */
    private static void log(Level l, String message) {
    	if(l != null) {
	    	logToChat(l, message);
	    	log.log(l, message);
    	}
    }

    
    
    private static void logToChat(Level l, String message) {
    	if(NoCheatConfiguration.notifyLevel.intValue() <= l.intValue()) {
	    	for(Player player : p.getServer().getOnlinePlayers()) {
	    		if((Permissions != null && Permissions.has(player, "nocheat.notify")) ||
 			   (Permissions == null && player.isOp())) {
	    			player.sendMessage("["+l.getName()+"] " + message);
	    		}
	    	}
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
    /**
     * Read the config file
     */
    public void setupConfig() {
    	NoCheatConfiguration.config(new File("plugins/NoCheat/nocheat.yml"));
    }
}