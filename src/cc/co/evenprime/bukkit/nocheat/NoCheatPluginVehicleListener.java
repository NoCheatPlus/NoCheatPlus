package cc.co.evenprime.bukkit.nocheat;


import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleListener;

/**
 * Handle events for all Player related events
 * @author Evenprime
 */

public class NoCheatPluginVehicleListener extends VehicleListener {
	
    private final NoCheatPlugin plugin;
    private final NoCheatPluginPlayerListener playerListener;
    
    public NoCheatPluginVehicleListener(NoCheatPlugin plugin, NoCheatPluginPlayerListener playerListener) {
    	this.plugin = plugin;
    	this.playerListener = playerListener;
    }
   	
    @Override
    public void onVehicleExit(VehicleExitEvent event) {
    	playerListener.ingoreNextXEvents(event.getExited());
    	
    }
    
    public void onVehicleDamage(VehicleDamageEvent event) {
    	playerListener.ingoreNextXEvents(event.getVehicle().getPassenger());
    }
}