package fr.neatmonster.nocheatplus.checks.moving.vehicle;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.location.setback.SetBackEntry;
import fr.neatmonster.nocheatplus.checks.moving.magic.MagicVehicle;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.RichBoundsLocation;
import fr.neatmonster.nocheatplus.utilities.RichEntityLocation;
import fr.neatmonster.nocheatplus.utilities.RichLivingEntityLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Vehicle moving envelope check, for Minecraft 1.9 and higher.
 * 
 * @author asofold
 *
 */
public class VehicleEnvelope extends Check {

    // TODO: Generic debug log with speed vs allowed speed.

    /** Types of entities not handled. For logging once. */
    private final Set<EntityType> notHandled = new HashSet<EntityType>();

    /** Tags for checks. */
    private final List<String> tags = new LinkedList<String>();

    public VehicleEnvelope() {
        super(CheckType.MOVING_VEHICLE_ENVELOPE);
    }

    public SetBackEntry check(final Player player, final Entity vehicle, final Location from, final Location to, final boolean isFake, final MovingData data, final MovingConfig cc) {
        // Delegate to a sub-check.
        tags.clear();
        tags.add("entity." + vehicle.getType());
        final boolean violation;
        if (vehicle instanceof LivingEntity) {
            // TODO: No events (neither player move nor vehicle move). Flying packets with look.
            violation = checkLivingEntity(player, (LivingEntity) vehicle, from, to, isFake, data, cc);
        }
        else {
            violation = checkEntity(player, vehicle, from, to, isFake, data, cc);
        }
        if (violation) {
            // Add up one for now.
            data.vehicleEnvelopeVL += 1.0;
            final ViolationData vd = new ViolationData(this, player, data.vehicleEnvelopeVL, 1, cc.vehicleEnvelopeActions);
            vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
            if (executeActions(vd).willCancel()) {
                return data.vehicleSetBacks.getValidMidTermEntry();
            }
        }
        else {
            data.vehicleEnvelopeVL *= 0.99; // Random cool down for now.
            data.vehicleSetBacks.setSafeMediumEntry(to); // TODO: Set only if it is safe to set. Set on monitor rather.
        }
        return null;
    }

    private boolean checkLivingEntity(final Player player, final LivingEntity vehicle, final Location from, final Location to, final boolean isFake, final MovingData data, final MovingConfig cc) {
        // TODO: Some kind of pool for reuse.
        final BlockCache blockCache = mcAccess.getBlockCache(from.getWorld());
        final RichLivingEntityLocation rFrom = new RichLivingEntityLocation(mcAccess, blockCache);
        rFrom.set(from, vehicle, cc.yOnGround);
        setupLivingEntity(rFrom);
        final RichLivingEntityLocation rTo = new RichLivingEntityLocation(mcAccess, blockCache);
        rTo.set(to, vehicle, cc.yOnGround);
        if (TrigUtil.isSamePos(from, to)) {
            rTo.prepare(rFrom);
        }
        else {
            setupLivingEntity(rTo);
        }
        if (checkIllegal(rFrom, rTo)) {
            tags.add("illegalcoords");
            return true;
        }
        final VehicleMoveData thisMove = new VehicleMoveData().set(rFrom, rTo);

        // TODO: Set up the minimum with illegal + extreme move checking.
        // TODO: Can attributes be used for generic checking jump + speed for all types?
        final double maxDistanceHorizontal;
        if (vehicle instanceof Horse) {
            maxDistanceHorizontal = MagicVehicle.horseMaxDistanceHorizontal;
        }
        else if (vehicle instanceof Pig) {
            maxDistanceHorizontal = MagicVehicle.pigMaxDistanceHorizontal;
        }
        else {
            // Might prevent / dismount or use 'other' settings.
            maxDistanceHorizontal = MagicVehicle.livingEntityMaxDistanceHorizontal;
        }
        // Max h dist.
        // TODO: Must at least account for speed potions.
        if (maxDistHorizontal(thisMove, maxDistanceHorizontal)) {
            return true;
        }
        // TODO: incomplete.
        onNotHandle(vehicle);
        return false;
    }

    private boolean checkEntity(final Player player, final Entity vehicle, final Location from, final Location to, final boolean isFake, final MovingData data, final MovingConfig cc) {
        // TODO: Some kind of pool for reuse.
        final BlockCache blockCache = mcAccess.getBlockCache(from.getWorld());
        final RichEntityLocation rFrom = new RichEntityLocation(mcAccess, blockCache);
        rFrom.set(from, vehicle, cc.yOnGround);
        setupEntity(rFrom);
        final RichEntityLocation rTo = new RichEntityLocation(mcAccess, blockCache);
        rTo.set(to, vehicle, cc.yOnGround);
        if (TrigUtil.isSamePos(from, to)) {
            rTo.prepare(rFrom);
        }
        else {
            setupEntity(rTo);
        }
        if (checkIllegal(rFrom, rTo)) {
            tags.add("illegalcoords");
            return true;
        }
        final VehicleMoveData thisMove = new VehicleMoveData().set(rFrom, rTo);

        // Delegate to sub checks by type of entity.
        if (vehicle instanceof Boat) {
            return checkBoat(player, vehicle, rFrom, rTo, thisMove, isFake, data, cc);
        }
        else if (vehicle instanceof Minecart) {
            return checkMinecart(player, vehicle, rFrom, rTo, thisMove, isFake, data, cc);
        }
        else {
            // Might prevent / dismount or use 'other' settings.
            if (maxDistHorizontal(thisMove, MagicVehicle.entityMaxDistanceHorizontal)) {
                return true;
            }
            onNotHandle(vehicle);
            return false;
        }
    }

    private boolean checkBoat(final Player player, final Entity vehicle, 
            final RichEntityLocation from, final RichEntityLocation to, 
            final VehicleMoveData thisMove, 
            final boolean isFake, final MovingData data, final MovingConfig cc) {
        // Maximum thinkable horizontal speed.
        if (maxDistHorizontal(thisMove, MagicVehicle.boatMaxDistanceHorizontal)) {
            return true;
        }
        // TODO: water, ice, ground, air, transitions
        onNotHandle(vehicle);
        return false;
    }

    private boolean checkMinecart(final Player player, final Entity vehicle, 
            final RichEntityLocation from, final RichEntityLocation to, 
            final VehicleMoveData thisMove, 
            final boolean isFake, final MovingData data, final MovingConfig cc) {
        // Maximum thinkable horizontal speed.
        if (maxDistHorizontal(thisMove, MagicVehicle.minecartMaxDistanceHorizontal)) {
            return true;
        }
        // TODO: rails, lava, ground, other?
        onNotHandle(vehicle);
        return false;
    }

    private boolean checkIllegal(final RichBoundsLocation from, final RichBoundsLocation to) {
        return from.hasIllegalCoords() || to.hasIllegalCoords();
    }

    private boolean maxDistHorizontal(final VehicleMoveData thisMove, final double maxDistanceHorizontal) {
        if (thisMove.hDistance > maxDistanceHorizontal) {
            tags.add("hdist");
            return true;
        }
        else {
            return false;
        }
    }

    private void setupEntity(final RichEntityLocation loc) {
        loc.collectBlockFlags();
    }

    private void setupLivingEntity(final RichEntityLocation loc) {
        loc.collectBlockFlags();
    }

    private void onNotHandle(final Entity vehicle) {
        if (!notHandled.contains(vehicle.getType())) {
            notHandled.add(vehicle.getType());
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, CheckUtils.getLogMessagePrefix(null, type) + "Can't handle entity type: " + vehicle.getType());
        }
    }

}
