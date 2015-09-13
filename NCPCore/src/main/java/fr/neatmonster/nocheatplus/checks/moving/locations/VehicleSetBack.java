package fr.neatmonster.nocheatplus.checks.moving.locations;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.TeleportUtil;

/**
 * Task for scheduling a vehicle set back. Resets the morePacketsVehicleTaskId in the MovingData for the player.
 * @author mc_dev
 *
 */
public class VehicleSetBack implements Runnable{
    private final Entity  vehicle;
    private final Player player;
    private final Location location;
    private final boolean debug;
    
	public VehicleSetBack(Entity vehicle, Player player, Location location, boolean debug) {
		this.vehicle = vehicle;
        this.player = player;
        this.location = location;
        this.debug = debug;
	}

    @Override
    public void run() {
    	final MovingData data = MovingData.getData(player);
    	data.morePacketsVehicleTaskId = -1;
        try{
    		data.prepareSetBack(location);
        	TeleportUtil.teleport(vehicle, player, location, debug);
        }
        catch(Throwable t){
        	StaticLog.logSevere(t);
        }
    }
    
}
