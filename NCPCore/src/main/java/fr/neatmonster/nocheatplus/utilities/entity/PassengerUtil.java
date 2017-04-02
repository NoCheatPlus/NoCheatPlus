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

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.entity.IEntityAccessVehicle;
import fr.neatmonster.nocheatplus.components.registry.event.IHandle;

/**
 * Vehicle/passenger related static utility. Registered as generic instance for
 * now.
 * 
 * @author asofold
 *
 */
public class PassengerUtil {

    public final IHandle<IEntityAccessVehicle> handleVehicle = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(IEntityAccessVehicle.class);

    /**
     * Test if the given entity is a passenger of the given vehicle.
     * 
     * @param entity
     * @param vehicle
     * @return
     */
    public boolean isPassenger(final Entity entity, final Entity vehicle) {
        return handleVehicle.getHandle().getEntityPassengers(vehicle).contains(entity);
    }

    /**
     * Check getPassenger recursively until a player is found, return that one
     * or null. This is intended to be the player in charge of steering the
     * vehicle.
     *
     * @param entity
     *            The vehicle.
     * @return The first player found amongst passengers recursively, excludes
     *         the given entity.
     */
    public Player getFirstPlayerPassenger(Entity entity) {
        List<Entity> passengers = handleVehicle.getHandle().getEntityPassengers(entity);
        while (!passengers.isEmpty()){
            entity = passengers.get(0); // The one in charge.
            if (entity instanceof Player){
                return (Player) entity;
            }
            passengers = handleVehicle.getHandle().getEntityPassengers(entity);
        }
        return null;
    }

    /**
     * Check recursively for vehicles, returns null if players are vehicles,
     * otherwise the lowest vehicle (that has no vehicle).
     *
     * @param passenger
     *            The passenger of vehicles. Typically the player.
     * @return Supposedly the vehicle that is steered.
     */
    public Entity getLastNonPlayerVehicle(final Entity passenger) {
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
     *            (meaning: vehicle enter, the player is not in a vehicle, test
     *            with this set to true and the vehicle returned by the event).
     * @return Supposedly the vehicle that is steered.
     */
    public Entity getLastNonPlayerVehicle(final Entity passenger, final boolean includePassenger) {
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
    //    public Player getFirstPlayerIncludingPassengersRecursively(Entity entity) {
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
