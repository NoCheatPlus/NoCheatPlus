package cc.co.evenprime.bukkit.nofly;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
* 
* NoFlyPlugin
* 
* Check PLAYER_MOVE events for their plausibility and cancel them if they are implausible
* 
* @author Evenprime
*/
public class NoFlyPlugin extends JavaPlugin {
    private final NoFlyPluginPlayerListener playerListener = new NoFlyPluginPlayerListener(this);
        
    public static final Logger log = Logger.getLogger("Minecraft");
    
    public NoFlyPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

     }

    public void onDisable() { }

    public void onEnable() {
    	
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Lowest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
}