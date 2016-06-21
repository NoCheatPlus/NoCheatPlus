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
package fr.neatmonster.nocheatplus.utilities.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Vehicle/passenger related static utility.
 * 
 * @author asofold
 *
 */
public class PassengerUtil {

    /**
     * Check getPassenger recursively until a player is found, return that one
     * or null.
     *
     * @param entity
     *            the entity
     * @return the first player passenger
     */
    public static Player getFirstPlayerPassenger(final Entity entity) {
        Entity passenger = entity.getPassenger();
        while (passenger != null){
            if (passenger instanceof Player){
                return (Player) passenger;
            }
            passenger = passenger.getPassenger();
        }
        return null;
    }

    /**
     * Check recursively for vehicles, returns null if players are vehicles,
     * otherwise the lowest vehicle (that has no vehicle).
     *
     * @param passenger
     *            The passenger of vehicles. Typically the player.
     * @return the last non player vehicle
     */
    public static Entity getLastNonPlayerVehicle(final Entity passenger) {
        Entity vehicle = passenger.getVehicle();
        while (vehicle != null){
            if (vehicle instanceof Player){
                return null;
            }
            else if (vehicle.isInsideVehicle()) {
                vehicle = vehicle.getVehicle();
            }
            else {
                break;
            }
        }
        return vehicle;
    }

}
