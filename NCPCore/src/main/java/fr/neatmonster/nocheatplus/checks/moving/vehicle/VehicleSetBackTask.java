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

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.entity.PassengerUtil;

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
    private final Entity[] passengers;

    /**
     * 
     * @param vehicle
     * @param player
     *            The player because of whom this teleport is happening. This
     *            should be the player in charge of steering, but that needn't
     *            be the case in future.
     * @param location
     * @param debug
     */
    public VehicleSetBackTask(Entity vehicle, Player player, Location location, boolean debug, Entity... passengers) {
        this.vehicle = vehicle;
        this.player = player;
        this.location = location;
        this.debug = debug;
        this.passengers = passengers;
    }

    @Override
    public void run() {
        final IPlayerData pData = DataManager.getPlayerData(player);
        final MovingData data = pData.getGenericInstance(MovingData.class);
        data.vehicleSetBackTaskId = -1;
        try{
            NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(PassengerUtil.class).teleportWithPassengers(
                    vehicle, player, location, debug, passengers, true, pData);
        }
        catch(Throwable t){
            StaticLog.logSevere(t);
        }
    }

}
