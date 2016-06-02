/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
