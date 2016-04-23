package fr.neatmonster.nocheatplus.checks.moving.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.TeleportUtil;

/**
 * Task for scheduling a vehicle set back. Resets the vehicleSetBackTaskId in
 * the MovingData for the player.
 * 
 * @author mc_dev
 *
 */
public class VehicleSetBackTask implements Runnable{
    private final Entity  vehicle;
    private final Player player;
    private final Location location;
    private final boolean debug;

    public VehicleSetBackTask(Entity vehicle, Player player, Location location, boolean debug) {
        this.vehicle = vehicle;
        this.player = player;
        this.location = location;
        this.debug = debug;
    }

    @Override
    public void run() {
        final MovingData data = MovingData.getData(player);
        data.vehicleSetBackTaskId = -1;
        try{
            TeleportUtil.teleport(vehicle, player, location, debug);
        }
        catch(Throwable t){
            StaticLog.logSevere(t);
        }
    }

}
