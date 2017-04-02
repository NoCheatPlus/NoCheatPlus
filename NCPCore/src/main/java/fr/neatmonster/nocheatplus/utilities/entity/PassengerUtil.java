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
        return getLastNonPlayerVehicle(passenger, false);
    }

    /**
     * Check recursively for vehicles, returns null if players are vehicles,
     * otherwise the lowest vehicle (that has no vehicle).
     *
     * @param passenger
     *            The passenger of vehicles. Typically the player.
     * @param includePassenger
     *            If set to true, the passenger is counted as a vehicle as well
     *            (meaning: vehicle enter, ther player is not in a vehicle, test
     *            with this set to true and the vehicle returned by the event).
     * @return the last non player vehicle
     */
    public static Entity getLastNonPlayerVehicle(final Entity passenger, final boolean includePassenger) {
        Entity vehicle = includePassenger ? passenger : passenger.getVehicle();
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

    //    /**
    //     * Get a player from an entity. This will return the first player found
    //     * amongst the entity itself and passengers checked recursively.
    //     *
    //     * @param entity
    //     *            the entity
    //     * @return the player passenger recursively
    //     */
    //    public static Player getFirstPlayerIncludingPassengersRecursively(Entity entity) {
    //        while (entity != null) {
    //            if (entity instanceof Player) {
    //                // Scrap the case of players riding players for the moment.
    //                return (Player) entity;
    //            }
    //            final Entity passenger = entity.getPassenger();
    //            if (entity.equals(passenger)) {
    //                // Just in case :9.
    //                break;
    //            }
    //            else {
    //                entity = passenger;
    //            }
    //        }
    //        return null;
    //    }

}
