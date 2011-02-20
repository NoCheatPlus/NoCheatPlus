package cc.co.evenprime.bukkit.nocheat;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
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
            
    public static Logger log;
    public static PermissionHandler Permissions = null;
    
    public NoCheatPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        playerListener = new NoCheatPluginPlayerListener(this);
        vehicleListener = new NoCheatPluginVehicleListener(this, playerListener);
        

        
        log = NoCheatConfiguration.logger;
     }

    public void onDisable() { 
    }

    public void onEnable() {
    	
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.VEHICLE_EXIT, vehicleListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.VEHICLE_DAMAGE, vehicleListener, Priority.Monitor, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        Logger.getLogger("Minecraft").info( "NoCheat version " + pdfFile.getVersion() + " is enabled!" );
        
        setupPermissions();
        setupConfig();
    }
    
    public void setupPermissions() {
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");


    	if(Permissions == null) {
    	    if(test != null) {
    		Permissions = ((Permissions)test).getHandler();
    	    } else {
    		log.info("Nocheat couldn't find Permissions plugin. Fallback to OP -> all allowed.");
    		this.getServer().getPluginManager().disablePlugin(this);
    	    }
    	}
    }
    
    public void setupConfig() {
    	NoCheatConfiguration.config(new File("plugins/NoCheat/nocheat.yml"));
    	  
    }
}