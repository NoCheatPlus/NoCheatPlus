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
package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class TeleportUtil.
 */
public class TeleportUtil {

    /** Temp use. LocUtil.clone on passing. setWorld(null) after use. */
    private static final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Teleport the player with vehicle, temporarily eject the passenger and set
     * teleported in MovingData.
     *
     * @param vehicle
     *            the vehicle
     * @param player
     *            the player
     * @param location
     *            the location
     * @param debug
     *            the debug
     */
    public static void teleport(final Entity vehicle, final Player player, final Location location, 
            final boolean debug) {
        // TODO: Rubber band issue needs synchronizing with packet level and ignore certain incoming ones?
        // TODO: This handling could conflict with WorldGuard region flags.
        // TODO: Account for nested passengers and inconsistencies.
        final MovingData data = MovingData.getData(player);
        data.isVehicleSetBack = true;
        // TODO: Adjust to multiple passengers.
        final Entity passenger = vehicle.getPassenger();
        boolean vehicleTeleported = false;
        final boolean playerIsPassenger = player.equals(passenger);
        boolean playerTeleported = false;
        // TODO: TeleportCause needs some central configuration (plugin vs. unknown vs. future).
        if (vehicle.isDead() || !vehicle.isValid()) {
            // TODO: Still consider teleporting the player.
            vehicleTeleported = false;
        }
        else if (playerIsPassenger) { // && vehicle.equals(player.getVehicle).
            // Attempt to only teleport the entity first. On failure use eject.
            // TODO: Probably needs a guard depending on version.
            //            if (vehicle.teleport(location, 
            //                  BridgeMisc.TELEPORT_CAUSE_CORRECTION_OF_POSITION)) {
            //                // Check success.
            //                if (vehicle.getLocation(useLoc).equals(location) && player.equals(vehicle.getPassenger())) {
            //                    vehicleTeleported = true;
            //                    playerTeleported = true;
            //                    if (debug) {
            //                        CheckUtils.debug(player, CheckType.MOVING_VEHICLE, "Direct teleport of entity with passenger succeeded.");
            //                    }
            //                }
            //            }
            if (!playerTeleported){
                vehicle.eject(); // NOTE: VehicleExit fires, unknown TP fires.
                // TODO: Confirm eject worked, handle if not.
                vehicleTeleported = vehicle.teleport(LocUtil.clone(location), 
                        BridgeMisc.TELEPORT_CAUSE_CORRECTION_OF_POSITION);
            }
        }
        else if (passenger == null) {
            vehicleTeleported = vehicle.teleport(location, 
                    BridgeMisc.TELEPORT_CAUSE_CORRECTION_OF_POSITION);
        }
        if (!playerTeleported && player.isOnline() && !player.isDead()) {
            // Mask player teleport as a set back.
            data.prepareSetBack(location);
            playerTeleported = player.teleport(LocUtil.clone(location));
            data.resetTeleported(); // Just in case.
            // Workarounds.
            data.ws.resetConditions(WRPT.G_RESET_NOTINAIR); // Allow re-use of certain workarounds. Hack/shouldbedoneelsewhere?
            // TODO: Magic 1.0, plus is this valid with horse, dragon...
            if (playerIsPassenger && playerTeleported && vehicleTeleported 
                    && player.getLocation().distance(vehicle.getLocation(useLoc)) < 1.5) {
                // Somewhat check against tp showing something wrong (< 1.0).
                vehicle.setPassenger(player); // NOTE: VehicleEnter fires, unknown TP fires.
                // TODO: What on failure of setPassenger?
                // Ensure a set back.
                // TODO: Set backs get invalidated somewhere, likely on an extra unknown TP. Use data.isVehicleSetBack in MovingListener/teleport.
                if (data.vehicleSetBacks.getFirstValidEntry(location) == null) {
                    // At least ensure one of the entries has to match the location we teleported the vehicle to.
                    if (data.debug) {
                        CheckUtils.debug(player, CheckType.MOVING_VEHICLE, "No set back is matching the vehicle location that it has just been set back to. Reset all lazily to: " + location);
                    }
                    data.vehicleSetBacks.resetAllLazily(location);
                }
                // Set this location as past move.
                final VehicleMoveData firstPastMove = data.vehicleMoves.getFirstPastMove();
                if (!firstPastMove.valid || firstPastMove.toIsValid 
                        || !TrigUtil.isSamePos(firstPastMove.from, location)) {
                    final MovingConfig cc = MovingConfig.getConfig(player);
                    NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(AuxMoving.class).resetVehiclePositions(vehicle, location, data, cc);
                }
            }
        }
        data.isVehicleSetBack = false;
        if (debug) { 
            CheckUtils.debug(player, CheckType.MOVING_VEHICLE, "Vehicle set back resolution: " + location + " pt=" + playerTeleported + " vt=" + vehicleTeleported);
        }
        useLoc.setWorld(null);
    }

    /**
     * Force mounting the vehicle, eject existing passenger.
     *
     * @param passenger
     *            the passenger
     * @param vehicle
     *            the vehicle
     */
    public static void forceMount(Entity passenger, Entity vehicle) {
        if (vehicle.getPassenger() != null) {
            vehicle.eject();
        }
        if (passenger.teleport(vehicle) && !vehicle.isDead() && vehicle.isValid()) {
            vehicle.setPassenger(passenger);
        }
    }

}
