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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
import fr.neatmonster.nocheatplus.checks.moving.location.setback.SetBackEntry;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveConsistency;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveInfo;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.checks.moving.velocity.AccountEntry;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleEntry;
import fr.neatmonster.nocheatplus.components.entity.IEntityAccessLastPositionAndLook;
import fr.neatmonster.nocheatplus.components.location.IGetLocationWithLook;
import fr.neatmonster.nocheatplus.components.location.SimplePositionWithLook;
import fr.neatmonster.nocheatplus.components.registry.event.IHandle;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.entity.PassengerUtil;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.location.RichBoundsLocation;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * Aggregate vehicle checks (moving, a player is somewhere above in the
 * hierarchy of passengers. Players who have other players as vehicles within
 * the hierarchy are ignored.).
 * <hr>
 * Data should be adjusted on entering a vehicle (player joins or enters a
 * vehicle). Because teleporting players with their vehicle means exit +
 * teleport + re-enter, vehicle data should not be reset on player
 * teleportation.
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
    private final Location useLoc1 = new Location(null, 0, 0, 0);

    /** Temporary use, reset world to null afterwards, avoid nesting. */
    private final Location useLoc2 = new Location(null, 0, 0, 0);

    /** Temporary use, avoid nesting. */
    private final SimplePositionWithLook usePos1 = new SimplePositionWithLook();
    /** Temporary use, avoid nesting. */
    @SuppressWarnings("unused")
    private final SimplePositionWithLook usePos2 = new SimplePositionWithLook();

    /** Auxiliary functionality. */
    private final AuxMoving aux = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(AuxMoving.class);
    private final PassengerUtil passengerUtil = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(PassengerUtil.class);

    /** Access last position fields for an entity. Updated on setMCAccess. */
    // TODO: Useless.
    private final IHandle<IEntityAccessLastPositionAndLook> lastPosLook = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(IEntityAccessLastPositionAndLook.class);

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
        // Check data.
        final Vehicle vehicle = event.getVehicle();
        if (vehicle == null) {
            return;
        }
        // TODO: Might account for the case of a player letting the vehicle move but not themselves (do mind latency).
        // Mind that players could be riding horses inside of minecarts etc.
        if (vehicle.getVehicle() != null) {
            // Do ignore events for vehicles inside of other vehicles.
            return;
        }
        final Player player = passengerUtil.getFirstPlayerPassenger(vehicle);
        if (player == null) {
            return;
        }
        if (vehicle.isDead() || !vehicle.isValid()) {
            // TODO: Actually force dismount?
            onPlayerVehicleLeave(player, vehicle);
            return;
        }
        final EntityType vehicleType = vehicle.getType();
        final MovingData data = MovingData.getData(player);
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (data.debug) {
            outputDebugVehicleMoveEvent(player, from, to);
        }
        if (from == null) {
            // Skip simply.
            // TODO: (In case update doesn't, could fake it here.)
            return;
        }
        else if (from.equals(to)) {
            // Not possible by obc code.
        }
        else {
            if (!from.getWorld().equals(to.getWorld())) {
                // TODO: Data adjustments will be necessary with the envelope check.
                return;
            }
            // TODO: Check consistency with assumed/tracked past position, both for from and to. Do something based on result.
        }
        if (normalVehicles.contains(vehicleType)) {
            // Should be the case, as VehicleUpdateEvent always fires.
            // Assume handled.
            return;
        }
        else {
            // Should not be possible, unless plugins somehow force this.
            // TODO: Log warning once / what?
            // TODO: Ignore or continue?
        }
        // Process as move.
        debug(player, "VehicleMoveEvent: legacy handling, potential issue.");
        // TODO: Actually here consistency with past position tracking should be tested.

        // TODO: Abstraction creation before calling checkVehicleMove, compare/align with onVehicleUpdate.
        checkVehicleMove(vehicle, vehicleType, from, to, player, false, data);
    }

    private void outputDebugVehicleMoveEvent(final Player player, final Location from, final Location to) {
        if (from != null && from.equals(to)) {
            debug(player, "VehicleMoveEvent: from=to: " + from);
        }
        else {
            debug(player, "VehicleMoveEvent: from: " + from + " , to: " + to);
        }
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
        final Entity vehicle = passengerUtil.getLastNonPlayerVehicle(player);
        if (data.debug) {
            debug(player, "onPlayerMoveVehicle: vehicle: " + vehicle);
        }
        data.wasInVehicle = true;
        data.sfHoverTicks = -1;
        data.removeAllVelocity();
        data.sfLowJump = false;
        // TODO: What with processingEvents.remove(player.getName());
        if (vehicle == null || vehicle.isDead() || !vehicle.isValid()) {
            // TODO: Note special case, if ever players can move with dead vehicles for a while.
            // TODO: Actually force dismount?
            onPlayerVehicleLeave(player, vehicle);
            return null;
        }
        else {
            // (Auto detection of missing events, might fire one time too many per plugin run.)
            final EntityType vehicleType = vehicle.getType();
            if (!normalVehicles.contains(vehicleType)) {
                // Treat like VehicleUpdateEvent.
                onVehicleUpdate(vehicle, vehicleType, player, true, data);
                return null;
            } else {
                final Location vLoc = vehicle.getLocation();
                data.vehicleConsistency = MoveConsistency.getConsistency(from, to, vLoc);
                // TODO: Consider TeleportUtil.forceMount or similar.
                final MovingConfig cc = MovingConfig.getConfig(player);
                if (data.vehicleConsistency == MoveConsistency.INCONSISTENT) {
                    if (cc.vehicleEnforceLocation) {
                        // checks.moving.vehicle.enforcelocation
                        // TODO: Permission + bypass check(s) !
                        return vLoc;
                    } else {
                        return null;
                    }
                } else {
                    // (Skip chunk loading here.)
                    aux.resetPositionsAndMediumProperties(player, vLoc, data, cc);
                    return null;
                }
            }
        }
    }

    /**
     * This should always fire, prefer over VehicleMoveEvent, if possible.
     * 
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVehicleUpdate(final VehicleUpdateEvent event) {
        // TODO: VehicleUpdateEvent. How to track teleporting of the vehicle?
        // TODO: Track just left vehicle/entity positions otherwise (on tick + vehicle update)?
        // TODO: No problem: (?) update 'authorized state' if no player passenger.
        final Vehicle vehicle = event.getVehicle();
        final EntityType vehicleType = vehicle.getType();
        if (!normalVehicles.contains(vehicleType)) {
            // A little extra sweep to check for debug flags.
            normalVehicles.add(vehicleType);
            if (MovingConfig.getConfig(vehicle.getWorld().getName()).debug) {
                debug(null, "VehicleUpdateEvent fired for: " + vehicleType);
            }
        }
        // TODO: Detect if a VehicleMove event will fire (not strictly possible without nms, depends on visibility of fields, possibly estimate instead?). 
        if (vehicle.getVehicle() != null) {
            // Do ignore events for vehicles inside of other vehicles.
            return;
        }
        final Player player = passengerUtil.getFirstPlayerPassenger(vehicle);
        if (player == null || player.isDead()) {
            return;
        }
        if (vehicle.isDead() || !vehicle.isValid()) {
            // TODO: Actually force dismount?
            onPlayerVehicleLeave(player, vehicle);
            return;
        }
        final MovingData data = MovingData.getData(player);
        //final MovingConfig cc = MovingConfig.getConfig(player);
        if (data.debug) {
            final Location loc = vehicle.getLocation(useLoc1);
            debug(player, "VehicleUpdateEvent: " + vehicleType + " " + loc);
            useLoc1.setWorld(null);
        }
        onVehicleUpdate(vehicle, vehicleType, player, false, data);
    }

    /**
     * Call from both VehicleUpdateEvent and PlayerMoveEvent. Uses useLoc.
     * 
     * @param vehicle
     *            The vehicle that deosn't have a vehicle. Must be valid and not
     *            dead.
     * @param vehicleType
     *            Type of that vehicle.
     * @param player
     *            The first player passenger of that vehicle. Not null, not
     *            dead.
     * @param fake
     *            True, if this is the real VehicleUpdateEvent, false if it's
     *            the PlayerMoveEvent (or other).
     */
    private void onVehicleUpdate(final Entity vehicle, final EntityType vehicleType, final Player player, final boolean fake,
            final MovingData data) {
        // TODO: (private or public?)
        // TODO: Might pass last position for reference.
        if (data.debug) {
            if (lastPosLook != null) {
                // Retrieve last pos.
                lastPosLook.getHandle().getPositionAndLook(vehicle, usePos1);
                debug(player, "Last position is reported as: " + LocUtil.simpleFormat(usePos1));
            }
        }
        checkVehicleMove(vehicle, vehicleType, null, null, player, true, data);
    }

    /**
     * Uses both useLoc1 and useLoc2, possibly others too.
     * 
     * @param vehicle
     * @param vehicleType
     * @param from
     *            May be null, may be ignored anyway. Might be used as
     *            firstPastMove, in case of data missing.
     * @param to
     *            May be null, may be ignored anyway.
     * @param player
     * @param fake
     * @param data
     */
    private void checkVehicleMove(final Entity vehicle, final EntityType vehicleType, 
            final Location from, final Location to, final Player player, final boolean fake, 
            final MovingData data) {
        // TODO: Detect teleportation and similar.
        final World world = vehicle.getWorld();
        final MovingConfig cc = MovingConfig.getConfig(player);
        final VehicleMoveInfo moveInfo = aux.useVehicleMoveInfo();
        // vehicleLocation: Track when it could become null! -> checkIllegal  -> no setback or null location.
        final Location vehicleLocation = vehicle.getLocation(moveInfo.useLoc);
        final VehicleMoveData firstPastMove = data.vehicleMoves.getFirstPastMove();
        // Ensure firstPastMove is valid.
        if (!firstPastMove.valid) {
            // Determine the best location to use as past move.
            // TODO: Could also check the set backs for plausible entries, however that would lead to a violation by default. Could use an indicator.
            final Location refLoc = from == null ? vehicleLocation : from;
            MovingUtil.ensureChunksLoaded(player, refLoc, "vehicle move (no past move)", data, cc);
            aux.resetVehiclePositions(vehicle, refLoc, data, cc);
            if (data.debug) {
                // TODO: Might warn instead.
                debug(player, "Missing past move data, set to: " + firstPastMove.from);
            }
        }
        // Determine best locations to use.
        // (Currently always use firstPastMove and vehicleLocation.)
        final Location useFrom = LocUtil.set(useLoc1, world, firstPastMove.toIsValid ? firstPastMove.to : firstPastMove.from);
        final Location useTo = vehicleLocation;
        // Initialize moveInfo.
        if (vehicleType == EntityType.PIG) {
            // TODO: Special cases by config rather.
            // TODO: Likely will fail with passable.
            moveInfo.setExtendFullWidth(0.52);
        }
        // TODO: Test yOnGround at 0.13 instead of xz-margin
        moveInfo.set(vehicle, useFrom, useTo, 
                vehicleType == EntityType.PIG ? Math.max(0.13, cc.yOnGround) : cc.yOnGround); // TODO: Extra config.
        moveInfo.setExtendFullWidth(0.0);
        // TODO: Check consistency for given/set and log debug/warnings if necessary (to = vehicleLocation? from = firstPastMove).
        // Check coordinates, just in case.
        if (checkIllegal(moveInfo.from, moveInfo.to)) {
            // Likely superfluous.
            // TODO: Obviously applies under unknown conditions.
            SetBackEntry newTo = data.vehicleSetBacks.getValidSafeMediumEntry();
            if (newTo == null) {
                recoverVehicleSetBack(player, vehicle, vehicleLocation, moveInfo, data, cc);
            }
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, CheckUtils.getLogMessagePrefix(player, CheckType.MOVING_VEHICLE) + "Illegal coordinates on checkVehicleMove: from: " + from + " , to: " + to);
            setBack(player, vehicle, newTo, data, cc);
            aux.returnVehicleMoveInfo(moveInfo);
            return;
        }
        // Ensure chunks are loaded.
        MovingUtil.ensureChunksLoaded(player, useFrom, useTo, firstPastMove, "vehicle move", data, cc);
        // Initialize currentMove.
        final VehicleMoveData thisMove = data.vehicleMoves.getCurrentMove();
        thisMove.set(moveInfo.from, moveInfo.to);
        // Prepare all extra properties by default for now.
        MovingUtil.prepareFullCheck(moveInfo.from, moveInfo.to, thisMove, cc.yOnGround);
        thisMove.setExtraVehicleProperties(vehicle);
        // Call checkVehicleMove for actual checks.
        checkVehicleMove(vehicle, vehicleType, vehicleLocation, world, moveInfo, thisMove, firstPastMove, player, fake, data, cc);
        // Cleanup.
        aux.returnVehicleMoveInfo(moveInfo);
    }

    /**
     * Try to recover the vehicle / player position on illegal coordinates.
     * 
     * @param player
     * @param moveInfo
     * @param data
     */
    private void recoverVehicleSetBack(final Player player, final Entity vehicle, 
            final Location vehicleLocation, final VehicleMoveInfo moveInfo, 
            final MovingData data, final MovingConfig cc) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, CheckUtils.getLogMessagePrefix(player, checkType) + "Illegal coordinates on vehicle moving: world: " + moveInfo.from.getWorldName() + " , from: " + LocUtil.simpleFormat(moveInfo.from)  + " , to: " + LocUtil.simpleFormat(moveInfo.to));
        // TODO: Kick for illegal moves?
        if (moveInfo.from.hasIllegalCoords()) {
            // (from is from the past moves usually.)
            // Attempt to use the current location.
            if (LocUtil.isBadCoordinate(vehicleLocation.getX(), vehicleLocation.getY(), vehicleLocation.getZ())) {
                // Can't recover vehicle coordinates.
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, CheckUtils.getLogMessagePrefix(player, checkType) + "Could not recover vehicle coordinates. Player will be kicked.");
                // Just kick.
                player.kickPlayer(cc.msgKickIllegalVehicleMove);
            }
            else {
                // Better than nothing.
                data.vehicleSetBacks.setDefaultEntry(vehicleLocation);
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, CheckUtils.getLogMessagePrefix(player, checkType) + "Using the current vehicle location for set back, due to not having a past location to rely on. This could be a bug.");
            }
        }
        else {
            // Perhaps safe to use.
            data.vehicleSetBacks.setDefaultEntry(moveInfo.from);
        }
    }

    private boolean checkIllegal(final RichBoundsLocation from, final RichBoundsLocation to) {
        // TODO: Only for VehicleMove or not at all (vehicle position is already set to 'to' anyway)?
        return from.hasIllegalCoords() || to.hasIllegalCoords();
    }

    /**
     * The actual checks for vehicle moving. Nested passengers are not handled
     * here. Demands firstPastMove to be valid.
     * <hr>
     * Prerequisite is having currentMove set in the most appropriate way for
     * data.vehicleMoves.
     * 
     * @param vehicle
     *            The vehicle that deosn't have a vehicle. Must be valid and not
     *            dead.
     * @param vehicleType
     *            Type of that vehicle.
     * @param moveInfo 
     * @param firstPastMove 
     * @param thisMove2 
     * @param player
     *            The first player passenger of that vehicle. Not null, not
     *            dead.
     * @param vehicleLoc
     *            Current location of the vehicle. For reference checking, the
     *            given instance will not be stored anywhere from within here.
     * @param fake
     *            False if this is called directly from a VehicleMoveEvent
     *            (should be legacy or real errors). True if called from
     *            onVehicleUpdate.
     * @param data
     * @param cc2 
     */
    private void checkVehicleMove(final Entity vehicle, final EntityType vehicleType, final Location vehicleLocation,
            final World world, final VehicleMoveInfo moveInfo, final VehicleMoveData thisMove, final VehicleMoveData firstPastMove, 
            final Player player, final boolean fake, final MovingData data, MovingConfig cc) {
        // TODO: (private or public?)

        data.joinOrRespawn = false;
        data.vehicleConsistency = MoveConsistency.getConsistency(thisMove, player.getLocation(useLoc1));
        switch (data.vehicleConsistency) {
            case FROM:
            case TO:
                aux.resetPositionsAndMediumProperties(player, player.getLocation(useLoc1), data, cc); // TODO: Drop MC 1.4!
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
            outputDebugVehicleMove(player, vehicle, thisMove, fake);
        }

        // TODO: Check activation of any check?

        // Ensure a common set back for now.
        if (!data.vehicleSetBacks.isDefaultEntryValid()) {
            ensureSetBack(player, thisMove, data);
        }

        // Moving envelope check(s).
        // TODO: Use set back storage for testing if this is appropriate (use SetBackEntry instead, remove Location retrieval then?).
        if ((newTo == null || data.vehicleSetBacks.getSafeMediumEntry().isValidAndOlderThan(newTo))
                && vehicleEnvelope.isEnabled(player, data, cc)) {
            // Skip if this is the first move after set back, with to=set back.
            if (data.timeSinceSetBack == 0 || thisMove.to.hashCode() == data.lastSetBackHash) {
                // TODO: This is a hot fix, to prevent a set back loop. Depends on having only the morepackets set back for vehicles.
                // TODO: Perhaps might want to add || !data.equalsAnyVehicleSetBack(to)
                if (data.debug) {
                    debug(player, "Skip envelope check on first move after set back acknowledging the set back with an odd starting point (from).");
                }
            }
            else {
                // Set up basic details about what/how to check.
                vehicleEnvelope.prepareCheckDetails(vehicle, moveInfo, thisMove);
                // Check.
                final SetBackEntry tempNewTo  = vehicleEnvelope.check(player, vehicle, thisMove, fake, data, cc);
                if (tempNewTo != null) {
                    newTo = tempNewTo;
                }
            }
        }

        // More packets: Sort this in last, to avoid setting the set back early. Always check to adjust set back, for now.
        // TODO: Still always update the frequency part?
        if ((newTo == null || data.vehicleSetBacks.getMidTermEntry().isValidAndOlderThan(newTo))) {
            if (vehicleMorePackets.isEnabled(player, data, cc)) {
                final SetBackEntry tempNewTo = vehicleMorePackets.check(player, thisMove, newTo, data, cc);
                if (tempNewTo != null) {
                    newTo = tempNewTo;
                }
            }
            else {
                // Otherwise we need to clear their data.
                // TODO: Make mid-term set back resetting independent of more packets.
                data.clearVehicleMorePacketsData();
            }
        }

        // Schedule a set back?
        if (newTo == null) {
            // Increase time since set back.
            data.timeSinceSetBack ++;
            // Finally finish processing the current move and move it to past ones.
            data.vehicleMoves.finishCurrentMove();
        }
        else {
            setBack(player, vehicle, newTo, data, cc);
        }
        useLoc1.setWorld(null);
    }

    /**
     * Called if the default set back entry isn't valid.
     * 
     * @param player
     * @param thisMove
     * @param data
     */
    private void ensureSetBack(final Player player, final VehicleMoveData thisMove, final MovingData data) {
        final IGetLocationWithLook ensureLoc;
        if (!data.vehicleSetBacks.isAnyEntryValid()) {
            ensureLoc = thisMove.from;
        }
        else {
            ensureLoc = data.vehicleSetBacks.getOldestValidEntry();
        }
        data.vehicleSetBacks.setDefaultEntry(ensureLoc);
        if (data.debug) {
            debug(player, "Ensure vehicle set back: " + ensureLoc);
        }
        //        if (data.vehicleSetBackTaskId != -1) {
        //            // TODO: This is likely the wrong thing to do!
        //            Bukkit.getScheduler().cancelTask(data.vehicleSetBackTaskId);
        //            data.vehicleSetBackTaskId = -1;
        //            if (data.debug) {
        //                debug(player, "Cancel set back task on ensureSetBack.");
        //            }
        //        }
    }

    private void setBack(final Player player, final Entity vehicle, final SetBackEntry newTo, final MovingData data, final MovingConfig cc) {
        // TODO: Generic set back manager, preventing all sorts of stuff that might be attempted or just happen before the task is running?
        if (data.vehicleSetBackTaskId == -1) {
            // Schedule a delayed task to teleport back the vehicle with the player.
            // (Only schedule if not already scheduled.)
            // TODO: Might log debug if skipping.
            // TODO: Problem: scheduling allows a lot of things to happen until the task is run. Thus control about some things might be necessary.
            // TODO: Reset on world changes or not?
            // TODO: Prevent vehicle data resetting due to dismount/mount/teleport.
            // (Future: Dismount penalty does not need extra handling, both are teleported anyway.)
            if (data.debug) {
                debug(player, "Will set back to: " + newTo);
            }
            boolean scheduleSetBack = cc.scheduleVehicleSetBacks;
            // Schedule as task, if set so.
            if (scheduleSetBack) {
                aux.resetVehiclePositions(vehicle, LocUtil.set(useLoc2, vehicle.getWorld(), newTo), data, cc); // Heavy-ish, though.
                data.vehicleSetBackTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new VehicleSetBackTask(vehicle, player, newTo.getLocation(vehicle.getWorld()), data.debug));

                if (data.vehicleSetBackTaskId == -1) {
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "Failed to schedule vehicle set back task. Player: " + player.getName() + " , set back: " + newTo);
                    scheduleSetBack = false; // Force direct teleport as a fall-back measure.
                }
                else if (data.debug) {
                    debug(player, "Vehicle set back task id: " + data.vehicleSetBackTaskId);
                }
            }
            // Attempt to set back directly if set so, or if needed.
            if (!scheduleSetBack) {
                /*
                 * NOTE: This causes nested vehicle exit+enter and player
                 * teleport events, while the current event is still being
                 * processed (one of player move, vehicle update/move). Position
                 * resetting and updating the set back (if needed) is done there
                 * (hack, subject to current review).
                 */
                if (data.debug) {
                    debug(player, "Attempt to set the player back directly.");
                }
                passengerUtil.teleportWithPassengers(vehicle, player, newTo.getLocation(vehicle.getWorld()), data.debug);
            }

        }
        else if (data.debug) {
            // TODO: Reset positions.
            data.vehicleMoves.invalidate();
            debug(player, "Vehicle set back task already scheduled, skip this time.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVehicleEnter(final VehicleEnterEvent event) {
        final Entity entity = event.getEntered();
        if ((entity instanceof Player)) {
            onPlayerVehicleEnter((Player) entity, event.getVehicle());
        }
    }

    /**
     * Assume entering a vehicle, event or join with being inside a vehicle.
     * Set back and past move overriding are done here, performing the necessary
     * consistency checking. Because teleporting players with their vehicle
     * means exit + teleport + re-enter, vehicle data should not be reset on
     * player teleportation.
     * 
     * @param player
     * @param vehicle
     */
    public void onPlayerVehicleEnter(final Player player,  final Entity vehicle) {
        final MovingData data = MovingData.getData(player);
        if (data.debug) {
            debug(player, "Vehicle enter: first vehicle: " + vehicle.getClass().getName());
        }
        // Check for nested vehicles.
        final Entity lastVehicle = passengerUtil.getLastNonPlayerVehicle(vehicle, true);
        if (lastVehicle == null) {
            data.clearVehicleData();
            if (data.debug) {
                debugNestedVehicleEnter(player);
            }
            return;
        }
        else if (!lastVehicle.equals(vehicle)) {
            // Nested vehicles.
            // TODO: Should in general skip checking these? Set backs don't yet work with these anyway (either... or ...).
            if (data.debug) {
                debug(player, "Vehicle enter: last of nested vehicles: " + lastVehicle.getClass().getName());
            }
            dataOnVehicleEnter(player, lastVehicle, data);
        }
        else {
            // Proceed normally.
            dataOnVehicleEnter(player, vehicle, data);
        }

    }

    private void debugNestedVehicleEnter(Player player) {
        debug(player, "Vehicle enter: Skip on nested vehicles, possibly with multiple players involved, who would do that?");
        List<String> vehicles = new LinkedList<String>();
        Entity tempVehicle = player.getVehicle();
        while (tempVehicle != null) {
            vehicles.add(tempVehicle.getType().toString());
            tempVehicle = tempVehicle.getVehicle();
        }
        if (!vehicles.isEmpty()) {
            debug(player, "Vehicle enter: Nested vehicles: " + StringUtil.join(vehicles, ", "));
        }
    }

    /**
     * Adjust data with given last non player vehicle.
     * 
     * @param player
     * @param vehicle
     */
    private void dataOnVehicleEnter(final Player player,  final Entity vehicle, final MovingData data) {
        // Adjust data.
        final MovingConfig cc = MovingConfig.getConfig(player);
        data.joinOrRespawn = false;
        data.removeAllVelocity();
        // Event should have a vehicle, in case check this last.
        final Location vLoc = vehicle.getLocation(useLoc1);
        data.vehicleConsistency = MoveConsistency.getConsistency(vLoc, null, player.getLocation(useLoc2));
        if (data.isVehicleSetBack) {
            /*
             * Currently checking for consistency is done in
             * TeleportUtil.teleport, so we skip that here: if
             * (data.vehicleSetBacks.getFirstValidEntry(vLoc) == null) {
             */
        }
        else {
            data.vehicleSetBacks.resetAll(vLoc);
        }
        aux.resetVehiclePositions(vehicle, vLoc, data, cc);
        if (data.debug) {
            debug(player, "Vehicle enter: " + vehicle.getType() + " , player: " + useLoc2 + " c=" + data.vehicleConsistency);
        }
        useLoc1.setWorld(null);
        useLoc2.setWorld(null);
        // TODO: more resetting, visible check (visible -> interact entity) ?
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
        // TODO: Queued set backs? Usually means they still get teleported, individually though.
        onPlayerVehicleLeave((Player) entity, event.getVehicle());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroyLowest(final VehicleDestroyEvent event) {
        // Prevent destroying ones own vehicle.
        final Entity attacker = event.getAttacker();
        if (attacker instanceof Player && passengerUtil.isPassenger(attacker, event.getVehicle())) {
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
        for (final Entity entity : passengerUtil.handleVehicle.getHandle().getEntityPassengers(event.getVehicle())) {
            if (entity instanceof Player) {
                onPlayerVehicleLeave((Player) entity, event.getVehicle());
            }
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
        //          // Await set back.
        //          // TODO: might still set ordinary set backs ?
        //          return;
        //      }

        final MovingConfig cc = MovingConfig.getConfig(player);
        // TODO: Loc can be inconsistent, determine which to use ! 
        final Location pLoc = player.getLocation(useLoc1);
        Location loc = pLoc; // The location to use as set back.
        //  TODO: Which vehicle to use ?
        // final Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            final Location vLoc = vehicle.getLocation(useLoc2);
            // (Don't override vehicle set back and last position here.)
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
                        final Location oldLoc = new Location(pLoc.getWorld(), lastMove.to.getX(), lastMove.to.getY(), lastMove.to.getZ());
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
        useLoc1.setWorld(null);
        useLoc2.setWorld(null);
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
    private void outputDebugVehicleMove(final Player player, final Entity vehicle, final VehicleMoveData thisMove, final boolean fake) {
        final StringBuilder builder = new StringBuilder(250);
        final Location vLoc = vehicle.getLocation();
        final Location loc = player.getLocation();
        // TODO: Differentiate debug levels (needs setting up some policy + document in BuildParamteres)?
        final Entity actualVehicle = player.getVehicle();
        final boolean wrongVehicle = actualVehicle == null || actualVehicle.getEntityId() != vehicle.getEntityId();
        builder.append(CheckUtils.getLogMessagePrefix(player, checkType));
        builder.append("VEHICLE MOVE " + (fake ? "(fake)" : "") + " in world " + thisMove.from.getWorldName() + ":");
        builder.append("\nFrom: ");
        builder.append(LocUtil.simpleFormat(thisMove.from));
        builder.append("\nTo: ");
        builder.append(LocUtil.simpleFormat(thisMove.to));
        builder.append("\n" + (thisMove.from.resetCond ? "resetcond" : (thisMove.from.onGround ? "ground" : "---")) + " -> " + (thisMove.to.resetCond ? "resetcond" : (thisMove.to.onGround ? "ground" : "---")));
        builder.append("\n Vehicle: ");
        builder.append(LocUtil.simpleFormat(vLoc));
        builder.append("\n Player: ");
        builder.append(LocUtil.simpleFormat(loc));
        builder.append("\n Vehicle type: " + vehicle.getType() + (wrongVehicle ? (actualVehicle == null ? " (exited?)" : " actual: " + actualVehicle.getType()) : ""));
        builder.append("\n hdist: " + thisMove.hDistance);
        builder.append(" vdist: " + (thisMove.yDistance));
        builder.append(" fake: " + fake);
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, builder.toString());
    }

}
