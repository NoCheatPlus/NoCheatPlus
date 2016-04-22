package fr.neatmonster.nocheatplus.checks.moving.vehicle;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.locations.VehicleSetBack;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveConsistency;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveData;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.moving.velocity.AccountEntry;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleEntry;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.logging.debug.DebugUtil;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * Aggregate vehicle checks (moving, a player is somewhere above in the
 * hierarchy of passengers).
 * 
 * @author asofold
 *
 */
public class VehicleChecks extends CheckListener {

    // TODO: Handle nested passengers somehow, at least warn with some rate limiting.

    /** The instance of NoCheatPlus. */
    private final Plugin plugin = Bukkit.getPluginManager().getPlugin("NoCheatPlus"); // TODO

    private final Set<EntityType> normalVehicles = new HashSet<EntityType>();

    /** Temporary use, reset world to null afterwards, avoid nesting. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    /** Auxiliary functionality. */
    private final AuxMoving aux = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(AuxMoving.class);

    /** The vehicle more packets check. */
    private final VehicleMorePackets vehicleMorePackets = addCheck(new VehicleMorePackets());

    /** The vehicle moving envelope check. */
    private final VehicleEnvelope vehicleEnvelope = new VehicleEnvelope();

    public VehicleChecks() {
        super(CheckType.MOVING_VEHICLE);
    }

    /**
     * When a vehicle moves, its player will be checked for various suspicious behaviors.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVehicleMove(final VehicleMoveEvent event) {
        final Vehicle vehicle = event.getVehicle();
        final EntityType entityType = vehicle.getType();
        if (!normalVehicles.contains(entityType)) {
            // A little extra sweep to check for debug flags.
            normalVehicles.add(entityType);
            if (MovingConfig.getConfig(vehicle.getWorld().getName()).debug) {
                debug(null, "VehicleMoveEvent fired for: " + entityType);
            }
        }
        // TODO: Might account for the case of a player letting the vehicle move but not themselves (do mind latency).
        // Mind that players could be riding horses inside of minecarts etc.
        if (vehicle.getVehicle() != null) {
            // Do ignore events for vehicles inside of other vehicles.
            return;
        }
        onVehicleMove(vehicle, event.getFrom(), event.getTo(), false);
    }

    /**
     * Called from player-move checking, if the player is inside of a vehicle.
     * @param player
     * @param from
     * @param to
     * @param data
     */
    public Location onPlayerMoveVehicle(final Player player, final Location from, final Location to, final MovingData data) {
        // Workaround for pigs and other (1.5.x and before)!
        // Note that with 1.6 not even PlayerMove fires for horses and pigs.
        // (isInsideVehicle is the faster check without object creation, do re-check though, if it changes to only check for Vehicle instances.)
        final Entity vehicle = CheckUtils.getLastNonPlayerVehicle(player);
        data.wasInVehicle = true;
        data.sfHoverTicks = -1;
        data.removeAllVelocity();
        data.sfLowJump = false;
        // TODO: What with processingEvents.remove(player.getName());
        if (vehicle != null) {
            final Location vLoc = vehicle.getLocation(); // TODO: Use a location as argument.
            // (Auto detection of missing events, might fire one time too many per plugin run.)
            if (!normalVehicles.contains(vehicle.getType())) {
                onVehicleMove(vehicle, vLoc, vLoc, true);
                return null;
            } else {
                data.vehicleConsistency = MoveConsistency.getConsistency(from, to, vLoc);
                // TODO: Consider TeleportUtil.forceMount or similar.
                final MovingConfig cc = MovingConfig.getConfig(player);
                if (data.vehicleConsistency == MoveConsistency.INCONSISTENT) {
                    if (cc.vehicleEnforceLocation) {
                        return vLoc;
                    } else {
                        return null;
                    }
                } else {
                    aux.resetPositionsAndMediumProperties(player, vLoc, data, cc);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * The actual checks for vehicle moving. Nested passengers are not handled
     * here.
     * 
     * @param vehicle
     * @param from
     * @param to
     * @param fake
     */
    public void onVehicleMove(final Entity vehicle, final Location from, final Location to, final boolean fake) {   
        // (No re-check for vehicles that have vehicles, pre condition is that this has already been checked.)
        // TODO: If fake: Use the last known position of the vehicle instead of from (if available, once implemented).
        final Player player = CheckUtils.getFirstPlayerPassenger(vehicle);
        if (player == null) {
            return;
        }
        if (vehicle.isDead() || !vehicle.isValid()) {
            onPlayerVehicleLeave(player, vehicle);
            return;
        }
        if (!from.getWorld().equals(to.getWorld())) {
            // TODO: Data adjustments will be necessary with the envelope check.
            return;
        }

        final MovingData data = MovingData.getData(player);
        final MovingConfig cc = MovingConfig.getConfig(player);
        data.joinOrRespawn = false;
        data.vehicleConsistency = MoveConsistency.getConsistency(from, to, player.getLocation(useLoc));
        switch (data.vehicleConsistency) {
            case FROM:
            case TO:
                aux.resetPositionsAndMediumProperties(player, player.getLocation(useLoc), data, cc); // TODO: Drop MC 1.4!
                break;
            case INCONSISTENT:
                // TODO: Any exploits exist? -> TeleportUtil.forceMount(player, vehicle)
                // TODO: Test with latency.
                break;
        }

        Location newTo = null;
        data.sfNoLowJump = true;

        if (cc.noFallVehicleReset) {
            // Reset noFall data.
            data.noFallSkipAirCheck = true; // Might allow one time cheat.
            data.sfLowJump = false;
            data.clearNoFallData();
        }

        if (data.debug) {
            // Log move.
            outputDebugVehicleMove(player, vehicle, from, to, fake);
        }

        // Ensure a common set-back for now.
        // TODO: Check activation of any check?
        if (!data.hasVehicleMorePacketsSetBack()){
            // TODO: Check if other set-back is appropriate or if to set on other events.
            data.setVehicleMorePacketsSetBack(from);
            if (data.vehicleSetBackTaskId != -1) {
                // TODO: Set back outdated or not?
                Bukkit.getScheduler().cancelTask(data.vehicleSetBackTaskId);
            }
        }

        // Moving envelope check(s).
        if (newTo == null && vehicleEnvelope.isEnabled(player, data, cc)) {
            newTo = vehicleEnvelope.check(player, vehicle, from, to, fake, data, cc);
        }

        // More packets: Sort this in last, to avoid setting the set-back early. Always check to adjust set-back, for now.
        if (vehicleMorePackets.isEnabled(player, data, cc)) {
            // If the player is handled by the vehicle more packets check, execute it.
            final Location mpNewTo = vehicleMorePackets.check(player, from, to, data, cc);
            if (mpNewTo != null) {
                // Just prefer this for now.
                newTo = mpNewTo;
            }
        }
        else {
            // Otherwise we need to clear their data.
            data.clearMorePacketsData();
        }

        // Schedule a set-back?
        if (newTo != null && data.vehicleSetBackTaskId == -1) {
            // Schedule a delayed task to teleport back the vehicle with the player.
            // (Only schedule if not already scheduled.)
            // TODO: Might log debug if skipping.
            // TODO: Problem: scheduling allows a lot of things to happen until the task is run. Thus control about some things might be necessary.
            // TODO: Reset on world changes or not?
            // TODO: Prevent vehicle data resetting due to dismount/mount/teleport.
            // TODO: Once there is dismount penalties, teleport individually (!).
            data.vehicleSetBackTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new VehicleSetBack(vehicle, player, newTo, data.debug));
            // TODO: Handle scheduling failure.
        }
        useLoc.setWorld(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVehicleEnter(final VehicleEnterEvent event) {
        final Entity entity = event.getEntered();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) entity;
        final MovingData data = MovingData.getData(player);
        data.joinOrRespawn = false;
        data.removeAllVelocity();
        // Event should have a vehicle, in case check this last.
        data.vehicleConsistency = MoveConsistency.getConsistency(event.getVehicle().getLocation(), null, player.getLocation(useLoc));
        useLoc.setWorld(null); // TODO: A pool ?
        // TODO: more resetting, visible check ?
    }

    /**
     * Called from player-move checking, if vehicle-leave has not been called after entering, but the player is not inside of a vehicle anymore.
     * @param player
     * @param data
     * @param cc
     */
    public void onVehicleLeaveMiss(final Player player, final MovingData data, final MovingConfig cc) {
        if (data.debug) {
            StaticLog.logWarning("VehicleExitEvent missing for: " + player.getName());
        }
        onPlayerVehicleLeave(player, null);
        //      if (BlockProperties.isRails(pFrom.getTypeId())) {
        // Always clear no fall data, let Minecraft do fall damage.
        data.noFallSkipAirCheck = true; // Might allow one time cheat.
        data.sfLowJump = false;
        data.clearNoFallData();
        // TODO: What with processingEvents.remove(player.getName());
        //      }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event) {
        final Entity entity = event.getExited();
        if (!(entity instanceof Player)) {
            return;
        }
        onPlayerVehicleLeave((Player) entity, event.getVehicle());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroyLowest(final VehicleDestroyEvent event) {
        // Prevent destroying ones own vehicle.
        final Entity attacker = event.getAttacker();
        if (attacker instanceof Player && attacker.equals(event.getVehicle().getPassenger())) {
            final Player player = (Player) attacker;
            final MovingData data = MovingData.getData(player);
            final MovingConfig cc = MovingConfig.getConfig(player);
            if (cc.vehiclePreventDestroyOwn) {
                if (CheckUtils.isEnabled(CheckType.MOVING_SURVIVALFLY, player, data, cc)
                        || CheckUtils.isEnabled(CheckType.MOVING_CREATIVEFLY, player, data, cc)) {
                }
                event.setCancelled(true);
                player.sendMessage(ChatColor.DARK_RED + "Destroying your own vehicle is disabled.");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        final Entity entity = event.getVehicle().getPassenger();
        if (entity instanceof Player) {
            onPlayerVehicleLeave((Player) entity, event.getVehicle());
        }
    }

    /**
     * Call on leaving or just having left a vehicle.
     * @param player
     * @param vehicle May be null in case of "not possible to determine".
     */
    private void onPlayerVehicleLeave(final Player player, final Entity vehicle) {
        final MovingData data = MovingData.getData(player);
        data.wasInVehicle = false;
        data.joinOrRespawn = false;
        //      if (data.vehicleSetBackTaskId != -1) {
        //          // Await set-back.
        //          // TODO: might still set ordinary set-backs ?
        //          return;
        //      }

        final MovingConfig cc = MovingConfig.getConfig(player);
        // TODO: Loc can be inconsistent, determine which to use ! 
        final Location pLoc = player.getLocation(useLoc);
        Location loc = pLoc; // The location to use as set-back.
        //  TODO: Which vehicle to use ?
        // final Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            final Location vLoc = vehicle.getLocation();
            // Workaround for some entities/animals that don't fire VehicleMoveEventS.
            if (!normalVehicles.contains(vehicle.getType()) || cc.noFallVehicleReset) {
                data.noFallSkipAirCheck = true; // Might allow one time cheat.
                data.clearNoFallData();
            }
            // Check consistency with vehicle location.
            if (MoveConsistency.getConsistency(vLoc, null, pLoc) == MoveConsistency.INCONSISTENT) {
                // TODO: Consider teleporting the player (...)
                // TODO: What with the case of vehicle moved to another world !?
                loc = vLoc; // 
                if (data.vehicleConsistency != MoveConsistency.INCONSISTENT) {
                    final MoveData lastMove = data.moveData.getFirst();
                    if (lastMove.toIsValid) {
                        final Location oldLoc = new Location(pLoc.getWorld(), lastMove.to.x, lastMove.to.y, lastMove.to.z);
                        if (MoveConsistency.getConsistency(oldLoc, null, pLoc) != MoveConsistency.INCONSISTENT) {
                            loc = oldLoc;
                        }
                    }
                }

            }
            if (data.debug) {
                debug(player, "Vehicle leave: " + vehicle.getType() + "@" + pLoc.distance(vLoc));
            }
        }

        // Adjust loc if in liquid (meant for boats !?).
        if (BlockProperties.isLiquid(loc.getBlock().getType())) {
            loc.setY(Location.locToBlock(loc.getY()) + 1.25);
        }

        if (data.debug) {
            debug(player, "Vehicle leave: " + pLoc.toString() + (pLoc.equals(loc) ? "" : " / player at: " + pLoc.toString()));
        }
        aux.resetPositionsAndMediumProperties(player, loc, data, cc);
        data.setSetBack(loc);
        // Give some freedom to allow the "exiting move".
        data.removeAllVelocity();
        // TODO: Use-once entries usually are intended to allow one offset, but not jumping/flying on.
        data.addHorizontalVelocity(new AccountEntry(0.9, 1, 1));
        data.addVerticalVelocity(new SimpleEntry(0.6, 1)); // TODO: Typical margin?
        useLoc.setWorld(null);
    }

    /**
     * Intended for vehicle-move events.
     * 
     * @param player
     * @param vehicle
     * @param from
     * @param to
     * @param fake true if the event was not fired by an external source (just gets noted).
     */
    private void outputDebugVehicleMove(final Player player, final Entity vehicle, final Location from, final Location to, final boolean fake) {
        final StringBuilder builder = new StringBuilder(250);
        final Location vLoc = vehicle.getLocation();
        final Location loc = player.getLocation();
        // TODO: Differentiate debug levels (needs setting up some policy + document in BuildParamteres)?
        final Entity actualVehicle = player.getVehicle();
        final boolean wrongVehicle = actualVehicle == null || actualVehicle.getEntityId() != vehicle.getEntityId();
        builder.append(CheckUtils.getLogMessagePrefix(player, checkType));
        builder.append("VEHICLE MOVE " + (fake ? "(fake)" : "") + " in world " + from.getWorld().getName() + ":\n");
        DebugUtil.addMove(from, to, null, builder);
        builder.append("\n Vehicle: ");
        DebugUtil.addLocation(vLoc, builder);
        builder.append("\n Player: ");
        DebugUtil.addLocation(loc, builder);
        builder.append("\n Vehicle type: " + vehicle.getType() + (wrongVehicle ? (actualVehicle == null ? " (exited?)" : " actual: " + actualVehicle.getType()) : ""));
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, builder.toString());
    }

}
