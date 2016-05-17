package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;

public class TeleportUtil {

    /** Temp use. LocUtil.clone on passing. setWorld(null) after use. */
    private static final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Teleport the player with vehicle, temporarily eject the passenger and set
     * teleported in MovingData.
     * 
     * @param vehicle
     * @param player
     * @param location
     */
    public static void teleport(final Entity vehicle, final Player player, final Location location, final boolean debug) {
        // TODO: Rubber band issue needs synchronizing with packet level and ignore certain incoming ones?
        // TODO: This handling could conflict with WorldGuard region flags.
        // TODO: Account for nested passengers and inconsistencies.
        final Entity passenger = vehicle.getPassenger();
        boolean vehicleTeleported = false;
        final boolean playerIsPassenger = player.equals(passenger);
        boolean playerTeleported = false;
        // TODO: TeleportCause needs some central configuration (plugin vs. unknown vs. future).
        if (vehicle.isDead() || !vehicle.isValid()) {
            vehicleTeleported = false;
        }
        else if (playerIsPassenger) { // && vehicle.equals(player.getVehicle).
            // Attempt to only teleport the entity first. On failure use eject.
            // TODO: Probably needs a guard depending on version.
            //            if (vehicle.teleport(location, TeleportCause.PLUGIN)) {
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
                vehicle.eject(); // NOTE: VehicleExit fires.
                // TODO: Confirm eject worked, handle if not.
                vehicleTeleported = vehicle.teleport(LocUtil.clone(location), TeleportCause.PLUGIN);
            }
        }
        else if (passenger == null) {
            vehicleTeleported = vehicle.teleport(location, TeleportCause.PLUGIN);
        }
        if (!playerTeleported && player.isOnline() && !player.isDead()) {
            // Mask teleport as set-back.
            final MovingData data = MovingData.getData(player);
            data.prepareSetBack(location);
            playerTeleported = player.teleport(LocUtil.clone(location));
            data.resetTeleported(); // Just in case.
            // Workarounds.
            data.ws.resetConditions(WRPT.G_RESET_NOTINAIR); // Allow re-use of certain workarounds. Hack/shouldbedoneelsewhere?
            // TODO: Magic 1.0, plus is this valid with horse, dragon...
            if (playerIsPassenger && playerTeleported && vehicleTeleported && player.getLocation().distance(vehicle.getLocation(useLoc)) < 1.5) {
                // Somewhat check against tp showing something wrong (< 1.0).
                vehicle.setPassenger(player); // NOTE: VehicleEnter fires.
                // TODO: What on failure of setPassenger?
                // Ensure a set-back.
                // TODO: Player teleportation leads to resetting the vehicle set-back, which is bad, because there are multiple possible set-back locations stored, then possibly interfering.
                if (!data.vehicleSetBacks.isAnyEntryValid()) {
                    if (data.debug) {
                        CheckUtils.debug(player, CheckType.MOVING_VEHICLE, "No valid vehicle set-back present after setting back with vehicle. Set new Location as default: " + location);
                    }
                    data.vehicleSetBacks.setDefaultEntry(location);
                }
                // Set this location as past move.
                final MovingConfig cc = MovingConfig.getConfig(player);
                NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(AuxMoving.class).resetVehiclePositions(vehicle, location, data, cc);
            }
        }
        if (debug) { 
            CheckUtils.debug(player, CheckType.MOVING_VEHICLE, "Vehicle set back resolution: " + location + " pt=" + playerTeleported + " vt=" + vehicleTeleported);
        }
        useLoc.setWorld(null);
    }

    /**
     * Force mounting the vehicle, eject existing passenger.
     * 
     * @param passenger
     * @param vehicle
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
