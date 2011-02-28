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
    private final NoCheatPluginPlayerListener playerListener;
    private final NoCheatPluginVehicleListener vehicleListener;
    private final NoCheatPluginBlockListener blockListener;
    private final NoCheatEntityListener entityListener;
            
    // My main logger
    private static Logger log;
    
    // Permissions 2.0, if available
    public static PermissionHandler Permissions = null;
    
    // A reference to the instance of the plugin
    private static NoCheatPlugin p = null;
    
    // Store data between Events
    public static Map<Player, NoCheatPluginData> playerData = new HashMap<Player, NoCheatPluginData>();

    /**
     * Ridiculously long constructor to keep supporting older bukkit versions, as long as it is possible
     * 
     * @param pluginLoader
     * @param instance
     * @param desc
     * @param f1
     * @param f2
     * @param cLoader
     */
    public NoCheatPlugin() {

        // Create our listeners and feed them with neccessary information
    	playerListener = new NoCheatPluginPlayerListener();
    	vehicleListener = new NoCheatPluginVehicleListener(playerListener);
    	blockListener  = new NoCheatPluginBlockListener();
    	entityListener = new NoCheatEntityListener();

    	log = NoCheatConfiguration.logger;
    	
    	p = this;
    }
    
    /**
     * Main access to data that needs to be stored between different events.
     * Always returns a NoCheatPluginData object, because if there isn't one
     * for the specified player, one will be created.
     * 
     * @param p
     * @return
     */
    public static NoCheatPluginData getPlayerData(Player p) {
    	NoCheatPluginData data = null;
    	
		if((data = playerData.get(p)) == null ) {
			synchronized(playerData) {
				data = playerData.get(p);
				if(data == null) {
					// If we have no data for the player, create some
					data = new NoCheatPluginData();
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

    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Lowest, this); // needed for speedhack and moving checks
    	pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this); // used to delete old data of users
    	pm.registerEvent(Event.Type.VEHICLE_EXIT, vehicleListener, Priority.Monitor, this); // used for moving check
    	pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, Priority.Monitor, this); // used for moving check
    	pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Low, this); // used for airbuild check
    	pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Highest, this); // used for dupebydeath check

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
     * Log a minor violation message to all locations declared in the config file
     * @param message
     */
    public static void logMinor(String message) {
    	if(NoCheatConfiguration.notifyLevel.intValue() <= Level.INFO.intValue()) {
    		for(Player player : p.getServer().getOnlinePlayers()) {
    			if((Permissions != null && Permissions.has(player, "nocheat.notify")) ||
    			   (Permissions == null && player.isOp())) {
    				player.sendMessage("[INFO] " + message);
    			}
    		}
    	}
    	log.info(message);
    }

    /**
     * Log a normal violation message to all locations declared in the config file
     * @param message
     */
    public static void logNormal(String message) {
    	if(NoCheatConfiguration.notifyLevel.intValue() <= Level.WARNING.intValue()) {
    		for(Player player : p.getServer().getOnlinePlayers()) {
    			if((Permissions != null && Permissions.has(player, "nocheat.notify")) ||
 			   (Permissions == null && player.isOp())) {
    				player.sendMessage("[WARNING] " + message);
    			}
    		}
    	}
    	log.warning(message);
    }
    
    /**
     * Log a heavy violation message to all locations declared in the config file
     * @param message
     */
    public static void logHeavy(String message) {
    	if(NoCheatConfiguration.notifyLevel.intValue() <= Level.SEVERE.intValue()) {
	    	for(Player player : p.getServer().getOnlinePlayers()) {
	    		if((Permissions != null && Permissions.has(player, "nocheat.notify")) ||
 			   (Permissions == null && player.isOp())) {
	    			player.sendMessage("[SEVERE] " + message);
	    		}
	    	}
    	}
    	log.severe(message);
    }
    
    /**
     * Read the config file
     */
    public void setupConfig() {
    	NoCheatConfiguration.config(new File("plugins/NoCheat/nocheat.yml"));
    }
}