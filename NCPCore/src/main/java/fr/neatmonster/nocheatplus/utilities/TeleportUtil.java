package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;

public class TeleportUtil {
	
	/** Temp use. LocUtil.clone on passing. setWorld(null) after use. */
	private static final Location useLoc = new Location(null, 0, 0, 0);

	/**
	 * Teleport the player with vehicle, temporarily eject the passenger and set teleported in MovingData.
	 * @param vehicle
	 * @param player
	 * @param location
	 */
	public static void teleport(final Entity vehicle, final Player player, final Location location, final boolean debug) {
		// TODO: This handling could conflict with WorldGuard region flags.
		// TODO: Account for nested passengers and inconsistencies.
		final Entity passenger = vehicle.getPassenger();
		final boolean vehicleTeleported;
		final boolean playerIsPassenger = player.equals(passenger);
		if (playerIsPassenger && !vehicle.isDead()){ // && vehicle.equals(player.getVehicle).
			// TODO: Does VehicleExit fire here !?  Consequences?
			vehicle.eject();
			vehicleTeleported = vehicle.teleport(location, TeleportCause.PLUGIN);
			
		}
		else if (passenger == null && !vehicle.isDead()){
			vehicleTeleported = vehicle.teleport(location, TeleportCause.PLUGIN);
		}
		else vehicleTeleported = false;
		final boolean playerTeleported = player.teleport(location);
		if (playerIsPassenger && playerTeleported && vehicleTeleported && player.getLocation().distance(vehicle.getLocation(useLoc)) < 1.0){
			// Somewhat check against tp showing something wrong (< 1.0).
			// TODO: Does VehicleEnter fire here !?  Consequences?
			vehicle.setPassenger(player);
		}
		if (debug){
			NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " vehicle set back: " + location);
		}
		useLoc.setWorld(null);
	}
	
	/**
	 * Force mounting the vehicle including teleportation.
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
