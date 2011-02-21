package cc.co.evenprime.bukkit.nocheat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
* Check PLAYER_MOVE events for their plausibility and cancel them if they are implausible
* 
* @author Evenprime
*/
public class NoCheatPlugin extends JavaPlugin {
	
    private final NoCheatPluginPlayerListener playerListener;
    private final NoCheatPluginVehicleListener vehicleListener;
    private final NoCheatPluginBlockListener blockListener;
            
    public static Logger log;
    public static PermissionHandler Permissions = null;
    
    // Store data between Events
    public static Map<Player, NoCheatPluginData> playerData = new HashMap<Player, NoCheatPluginData>();

    public NoCheatPlugin() {

    	playerListener = new NoCheatPluginPlayerListener();
    	vehicleListener = new NoCheatPluginVehicleListener(playerListener);
    	blockListener  = new NoCheatPluginBlockListener();

    	log = NoCheatConfiguration.logger;
    }
    
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
    }

    public void onEnable() {

    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Lowest, this);
    	pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
    	pm.registerEvent(Event.Type.VEHICLE_EXIT, vehicleListener, Priority.Monitor, this);
    	pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, Priority.Monitor, this);
    	pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Low, this);

    	PluginDescriptionFile pdfFile = this.getDescription();
    	Logger.getLogger("Minecraft").info( "NoCheat version " + pdfFile.getVersion() + " is enabled!" );

    	setupPermissions();
    	setupConfig();
    }

    public void setupPermissions() {
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

   		if(test != null) {
    		Permissions = ((Permissions)test).getHandler();
    	} else {
    		log.info("Nocheat couldn't find Permissions plugin. Fallback to 'isOp()' equals 'all allowed'.");
    		this.getServer().getPluginManager().disablePlugin(this);
    	}
    }

    public void setupConfig() {
    	NoCheatConfiguration.config(new File("plugins/NoCheat/nocheat.yml"));

    }
}