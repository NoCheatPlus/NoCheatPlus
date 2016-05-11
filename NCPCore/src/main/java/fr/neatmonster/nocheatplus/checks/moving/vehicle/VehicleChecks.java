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
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;
import fr.neatmonster.nocheatplus.checks.moving.location.setback.SetBackEntry;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveConsistency;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.moving.velocity.AccountEntry;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleEntry;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

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
     * TEST
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVehicleUpdate(final VehicleUpdateEvent event) {
        // TODO: VehicleUpdateEvent. How to track teleporting of the vehicle?
        // TODO: No problem: (?) update 'authorized state' if no player passenger.
        final Vehicle vehicle = event.getVehicle();
        // TODO: Detect if a VehicleMove event will fire (not strictly possible without nms, depends on visibility of fields, possibly estimate instead?). 
        // TODO: normalVehicles handling.
        if (vehicle.getVehicle() != null) {
            // Do ignore events for vehicles inside of other vehicles.
            return;
        }
        final Player player = CheckUtils.getFirstPlayerPassenger(vehicle);
        if (player == null) {
            return;
        }
        if (vehicle.isDead() || !vehicle.isValid()) {
            // TODO: Actually force dismount?
            onPlayerVehicleLeave(player, vehicle);
            return;
        }
        //        final MovingData data = MovingData.getData(player);
        //        final MovingConfig cc = MovingConfig.getConfig(player);
        final Location loc = vehicle.getLocation(useLoc);
        debug(player, "Vehicle update: " + vehicle.getType() + " " + loc);
        useLoc.setWorld(null);
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
        if (data.debug) {
            debug(player, "onPlayerMoveVehicle: vehicle: " + vehicle);
        }
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
            // TODO: Actually force dismount?
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

        SetBackEntry newTo = null;
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

        // TODO: Check activation of any check?

        // Ensure a common set-back for now.
        if (!data.vehicleSetBacks.isDefaultEntryValid()) {
            // TODO: Check if other set-back is appropriate or if to set on other events.
            data.vehicleSetBacks.setDefaultEntry(from);
            if (data.debug) {
                debug(player, "Ensure vehicle set-back: " + from);
            }
            if (data.vehicleSetBackTaskId != -1) {
                // TODO: Set back outdated or not?
                Bukkit.getScheduler().cancelTask(data.vehicleSetBackTaskId);
            }
        }

        // Moving envelope check(s).
        // TODO: Use set-back storage for testing if this is appropriate (use SetBackEntry instead, remove Location retrieval then?).
        if ((newTo == null || data.vehicleSetBacks.getSafeMediumEntry().isValidAndOlderThan(newTo))
                && vehicleEnvelope.isEnabled(player, data, cc)) {
            // Skip if this is the first move after set-back, with to=set-back.
            if (data.timeSinceSetBack == 0 || to.hashCode() == data.lastSetBackHash) {
                // TODO: This is a hot fix, to prevent a set-back loop. Depends on having only the morepackets set-back for vehicles.
                // TODO: Perhaps might want to add || !data.equalsAnyVehicleSetBack(to)
                if (data.debug) {
                    debug(player, "Skip envelope check on first move after set-back acknowledging the set-back with an odd starting point (from).");
                }
            }
            else {
                final SetBackEntry tempNewTo  = vehicleEnvelope.check(player, vehicle, from, to, fake, data, cc);
                if (tempNewTo != null) {
                    newTo = tempNewTo;
                }
            }
        }

        // More packets: Sort this in last, to avoid setting the set-back early. Always check to adjust set-back, for now.
        // TODO: Still always update the frequency part?
        if ((newTo == null || data.vehicleSetBacks.getMidTermEntry().isValidAndOlderThan(newTo))) {
            if (vehicleMorePackets.isEnabled(player, data, cc)) {
                final SetBackEntry tempNewTo = vehicleMorePackets.check(player, from, to, newTo == null && data.vehicleSetBackTaskId == -1, data, cc);
                if (tempNewTo != null) {
                    newTo = tempNewTo;
                }
            }
            else {
                // Otherwise we need to clear their data.
                // TODO: Should only if disabled.
                data.clearVehicleMorePacketsData();
            }
        }

        // Schedule a set-back?
        if (newTo == null) {
            // Increase time since set-back.
            data.timeSinceSetBack ++;
        }
        else {
            setBack(player, vehicle, newTo.getLocation(from.getWorld()), data);
        }
        useLoc.setWorld(null);
    }

    private void setBack(final Player player, final Entity vehicle, final Location newTo, final MovingData data) {
        // TODO: Generic set-back manager, preventing all sorts of stuff that might be attempted or just happen before the task is running?
        if (data.vehicleSetBackTaskId == -1) {
            // Schedule a delayed task to teleport back the vehicle with the player.
            // (Only schedule if not already scheduled.)
            // TODO: Might log debug if skipping.
            // TODO: Problem: scheduling allows a lot of things to happen until the task is run. Thus control about some things might be necessary.
            // TODO: Reset on world changes or not?
            // TODO: Prevent vehicle data resetting due to dismount/mount/teleport.
            // (Future: Dismount penalty does not need extra handling, both are teleported anyway.)
            data.vehicleSetBackTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new VehicleSetBackTask(vehicle, player, newTo, data.debug));
            // TODO: Handle scheduling failure.
            if (data.debug) {
                debug(player, "Vehicle set-back task scheduled: " + newTo + " id=" + data.vehicleSetBackTaskId);
            }
        }
        else if (data.debug) {
            debug(player, "Vehicle set-back task already scheduled, skip this time.");
        }
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
        final Entity vehicle = event.getVehicle();
        data.vehicleConsistency = MoveConsistency.getConsistency(vehicle.getLocation(), null, player.getLocation(useLoc));
        if (data.debug) {
            debug(player, "Vehicle enter: " + vehicle.getType() + "@" + useLoc + " c=" + data.vehicleConsistency);
        }
        useLoc.setWorld(null);
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
                // TODO: This message must be configurable.
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
                    // TODO: This may need re-setting on player move -> vehicle move.
                    final PlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
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

    //    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=false)
    //    public void onEntityTeleport(final EntityTeleportEvent event) {
    //        final Entity entity = event.getEntity();
    //        if (entity == null) {
    //            return;
    //        }
    //        final Player player = CheckUtils.getFirstPlayerPassenger(entity);
    //        if (player != null && MovingData.getData(player).debug) {
    //            debug(player, "Entity teleport with player as passenger: " + entity + " from=" + event.getFrom() + " to=" + event.getTo());
    //        }
    //        else {
    //            // Log if the debug config flag is set.
    //            final World world = LocUtil.getFirstWorld(event.getFrom(), event.getTo());
    //            if (world != null && MovingConfig.getConfig(world.getName()).debug) {
    //                // TODO: Keep (expiring) entity data, for recently mounted, possibly for fight checks too?
    //                debug(null, "Entity teleport: " + entity + " from=" + event.getFrom() + " to=" + event.getTo());
    //            }
    //        }
    //    }

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
        builder.append("VEHICLE MOVE " + (fake ? "(fake)" : "") + " in world " + from.getWorld().getName() + ":");
        builder.append("\nFrom: ");
        builder.append(LocUtil.simpleFormat(from));
        builder.append("\nTo: ");
        builder.append(LocUtil.simpleFormat(to));
        builder.append("\n Vehicle: ");
        builder.append(LocUtil.simpleFormat(vLoc));
        builder.append("\n Player: ");
        builder.append(LocUtil.simpleFormat(loc));
        builder.append("\n Vehicle type: " + vehicle.getType() + (wrongVehicle ? (actualVehicle == null ? " (exited?)" : " actual: " + actualVehicle.getType()) : ""));
        builder.append("\n hdist: " + TrigUtil.xzDistance(from, to));
        builder.append(" vdist: " + (to.getY() - from.getY()));
        builder.append(" fake: " + fake);
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, builder.toString());
    }

}
