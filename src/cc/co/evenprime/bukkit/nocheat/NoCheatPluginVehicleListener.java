package cc.co.evenprime.bukkit.nocheat;


import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleListener;

/**
 * Handle events for all Player related events
 * @author Evenprime
 */

public class NoCheatPluginVehicleListener extends VehicleListener {
	
    private final NoCheatPluginPlayerListener playerListener;
    
    public NoCheatPluginVehicleListener(NoCheatPluginPlayerListener playerListener) {
    	this.playerListener = playerListener;
    }
   	
    @Override
    public void onVehicleExit(VehicleExitEvent event) {
    	playerListener.ingoreNextXEvents(event.getExited(), 1);
    	
    }
    
    public void onVehicleDamage(VehicleDamageEvent event) {
    	playerListener.ingoreNextXEvents(event.getVehicle().getPassenger(), 1);
    }
}