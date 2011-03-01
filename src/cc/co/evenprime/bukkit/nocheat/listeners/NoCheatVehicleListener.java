package cc.co.evenprime.bukkit.nocheat.listeners;


import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleListener;


/**
 * Handle events for all Player related events
 * 
 * @author Evenprime
 */

public class NoCheatVehicleListener extends VehicleListener {
	
    private final NoCheatPlayerListener playerListener;
    
    public NoCheatVehicleListener(NoCheatPlayerListener playerListener) {
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