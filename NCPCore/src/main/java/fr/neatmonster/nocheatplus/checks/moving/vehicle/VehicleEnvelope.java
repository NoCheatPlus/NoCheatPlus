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

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.location.setback.SetBackEntry;
import fr.neatmonster.nocheatplus.checks.moving.magic.MagicVehicle;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveData;
import fr.neatmonster.nocheatplus.checks.moving.model.VehicleMoveInfo;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Vehicle moving envelope check, for Minecraft 1.9 and higher.
 * 
 * @author asofold
 *
 */
public class VehicleEnvelope extends Check {

    /**
     * Check specific details for re-use.
     * 
     * @author asofold
     *
     */
    public class CheckDetails {

        public boolean canJump, canStepUpBlock;
        public double maxAscend;

        /** Simplified type, like BOAT, MINECART. */
        public EntityType simplifiedType; // Not sure can be kept up.

        public boolean checkAscendMuch;
        public boolean checkDescendMuch;

        /** From could be a new set-back location. */
        public boolean fromIsSafeMedium;
        /** To could be a new set-back location. */
        public boolean toIsSafeMedium;

        /** Interpreted differently depending on check. */
        public boolean inAir;

        public void reset() {
            canJump = canStepUpBlock = false;
            maxAscend = 0.0;
            checkAscendMuch = checkDescendMuch = true;
            fromIsSafeMedium = toIsSafeMedium = inAir = false;
            simplifiedType = null;
        }

    }

    /** Tags for checks. */
    private final List<String> tags = new LinkedList<String>();

    /** Extra details to log on debug. */
    private final List<String> debugDetails = new LinkedList<String>();

    /** Details for re-use. */
    private final CheckDetails checkDetails = new CheckDetails();

    public VehicleEnvelope() {
        super(CheckType.MOVING_VEHICLE_ENVELOPE);
    }

    public SetBackEntry check(final Player player, final Entity vehicle, final VehicleMoveData thisMove, final boolean isFake, final MovingData data, final MovingConfig cc) {
        // Delegate to a sub-check.
        tags.clear();
        tags.add("entity." + vehicle.getType());
        if (data.debug) {
            debugDetails.clear();
            data.ws.setJustUsedIds(debugDetails); // Add just used workaround ids to this list directly, for now.
        }
        final boolean violation = checkEntity(player, vehicle, thisMove, isFake, data, cc);
        if (data.debug && !debugDetails.isEmpty()) {
            debugDetails(player);
            debugDetails.clear();
        }
        if (violation) {
            // Add up one for now.
            data.vehicleEnvelopeVL += 1.0;
            final ViolationData vd = new ViolationData(this, player, data.vehicleEnvelopeVL, 1, cc.vehicleEnvelopeActions);
            vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
            if (executeActions(vd).willCancel()) {
                return data.vehicleSetBacks.getValidSafeMediumEntry();
            }
        }
        else {
            data.vehicleEnvelopeVL *= 0.99; // Random cool down for now.
            // Do not set a set-back here.
        }
        return null;
    }

    private void debugDetails(final Player player) {
        if (!tags.isEmpty()) {
            debugDetails.add("tags:");
            debugDetails.add(StringUtil.join(tags, "+"));
        }
        final StringBuilder builder = new StringBuilder(500);
        builder.append("Details:\n");
        for (final String detail : debugDetails) {
            builder.append(" , ");
            builder.append(detail);
        }
        debug(player, builder.toString());
        debugDetails.clear();
    }

    private double getHDistCap(final EntityType type, final MovingConfig cc) {
        final Double v = cc.vehicleEnvelopeHorizontalSpeedCap.get(type);
        if (v == null) {
            return cc.vehicleEnvelopeHorizontalSpeedCap.get(null);
        }
        else {
            return v;
        }
    }

    private boolean checkEntity(final Player player, final Entity vehicle, final VehicleMoveData thisMove, final boolean isFake, final MovingData data, final MovingConfig cc) {
        boolean violation = false;
        if (data.debug) {
            debugDetails.add("inair: " + data.sfJumpPhase);
        }

        // Medium dependent checking.

        // TODO: Try pigs on layered snow. Consider actual bounding box / lost-ground / ...

        // Maximum thinkable horizontal speed.
        // TODO: Further distinguish, best set in CheckDetails.
        if (maxDistHorizontal(thisMove, getHDistCap(checkDetails.simplifiedType, cc))) { // Override type for now.
            return true;
        }

        // TODO: Could limit descend by 2*maxDescend, ascend by much less.

        // TODO: Split code to methods.
        // TODO: Get extended liquid specs (allow confine to certain flags, here: water). Contains info if water is only flowing down, surface properties (non liquid blocks?), still water.
        if (thisMove.from.inWeb) {
            // TODO: Check anything?
            if (data.debug) {
                debugDetails.add("");
            }
            //            if (thisMove.yDistance > 0.0) {
            //                tags.add("ascend_web");
            //                return true;
            //            }
            // TODO: Enforce not ascending ?
            // TODO: max speed.
        }
        else if (thisMove.from.inWater && thisMove.to.inWater) {
            // Default in-medium move.
            if (data.debug) {
                debugDetails.add("water-water");
            }
            // TODO: Should still cover extreme moves here.

            // Special case moving up after falling.
            // TODO: Move to MagicVehicle.oddInWater
            // TODO: Check past moves for falling (not yet available).
            // TODO: Check if the target location somehow is the surface.
            if (MagicVehicle.oddInWater(thisMove, checkDetails, data)) {
                // (Assume players can't control sinking boats for now.)
                checkDetails.checkDescendMuch = checkDetails.checkAscendMuch = false;
                violation = false;
            }
        }
        else if (thisMove.from.onGround && thisMove.to.onGround) {
            // Default on-ground move.
            // TODO: Should still cover extreme moves here.
            if (checkDetails.canStepUpBlock && thisMove.yDistance > 0.0 && thisMove.yDistance <= 1.0) {
                checkDetails.checkAscendMuch = false;
                tags.add("step_up");
            }
            if (thisMove.from.onIce && thisMove.to.onIce) {
                // Default on-ice move.
                if (data.debug) {
                    debugDetails.add("ice-ice");
                }
                // TODO: Should still cover extreme moves here.
            }
            else {
                // (TODO: actually a default on-ground move.)
                if (data.debug) {
                    debugDetails.add("ground-ground");
                }
            }
        }
        else if (checkDetails.inAir) {
            // In-air move.
            if (checkInAir(thisMove, data)) {
                violation = true;
            }
        }
        else {
            // Some transition to probably handle.
            if (data.debug) {
                debugDetails.add("?-?");
            }
            // TODO: Lift off speed etc.
            // TODO: Clearly overlaps other cases.
            // TODO: Skipped vehicle move events happen here as well (...).
            if (!checkDetails.toIsSafeMedium) {
                // TODO: At least do something here?
            }
        }
        // Maximum ascend speed.
        if (checkDetails.checkAscendMuch && thisMove.yDistance > checkDetails.maxAscend) {
            tags.add("ascend_much");
            violation = true;
        }
        // Maximum descend speed.
        if (checkDetails.checkDescendMuch && thisMove.yDistance < -MagicVehicle.maxDescend) {
            // TODO: At times it looks like one move is skipped, resulting in double distance ~ -5 and at the same time 'vehicle moved too quickly'. 
            // TODO: Test with log this to console to see the order of things.
            tags.add("descend_much");
            violation = true;
        }

        if (!violation) {
            // No violation.
            // TODO: sfJumpPhase is abused for in-air move counting here.
            if (checkDetails.inAir) {
                data.sfJumpPhase ++;
            }
            else {
                // Adjust set-back.
                if (checkDetails.toIsSafeMedium) {
                    data.vehicleSetBacks.setSafeMediumEntry(thisMove.to);
                    data.sfJumpPhase = 0;
                }
                else if (checkDetails.fromIsSafeMedium) {
                    data.vehicleSetBacks.setSafeMediumEntry(thisMove.from);
                    data.sfJumpPhase = 0;
                }
                // Reset the resetNotInAir workarounds.
                data.ws.resetConditions(WRPT.G_RESET_NOTINAIR);
            }
            data.vehicleSetBacks.setLastMoveEntry(thisMove.to);
        }

        return violation;
    }

    /**
     * Prepare checkDetails according to vehicle-specific interpretation of side
     * conditions.
     * 
     * @param vehicle
     * @param moveInfo Cheating.
     * @param thisMove
     */
    protected void prepareCheckDetails(final Entity vehicle, final VehicleMoveInfo moveInfo, final VehicleMoveData thisMove) {
        checkDetails.reset();
        // TODO: These properties are for boats, might need to distinguish further.
        checkDetails.fromIsSafeMedium = thisMove.from.inWater || thisMove.from.onGround || thisMove.from.inWeb;
        checkDetails.toIsSafeMedium = thisMove.to.inWater || thisMove.to.onGround || thisMove.to.inWeb;
        checkDetails.inAir = !checkDetails.fromIsSafeMedium && !checkDetails.toIsSafeMedium;
        // Distinguish by entity class (needs future proofing at all?).
        if (vehicle instanceof Boat) {
            checkDetails.simplifiedType = EntityType.BOAT;
            checkDetails.maxAscend = MagicVehicle.maxAscend;
        }
        else if (vehicle instanceof Minecart) {
            checkDetails.simplifiedType = EntityType.MINECART;
            // Bind to rails.
            thisMove.setExtraMinecartProperties(moveInfo); // Cheating.
            if (thisMove.fromOnRails) {
                checkDetails.fromIsSafeMedium = true;
                checkDetails.inAir = false;
            }
            if (thisMove.toOnRails) {
                checkDetails.toIsSafeMedium = true;
                checkDetails.inAir = false;
            }
        }
        else if (vehicle instanceof Horse) {
            checkDetails.simplifiedType = EntityType.HORSE;
            checkDetails.canJump = checkDetails.canStepUpBlock = true;
        }
        else if (vehicle instanceof Pig) {
            checkDetails.simplifiedType = EntityType.PIG;
            checkDetails.canJump = false;
            checkDetails.canStepUpBlock = true;
        }
        else {
            checkDetails.simplifiedType = thisMove.vehicleType;
        }

        // Generic settings.
        // (maxAscend is not checked for stepping up blocks)
        if (checkDetails.canJump) {
            checkDetails.maxAscend = 1.0; // Coarse envelope. Actual lift off gain should be checked on demand.
        }
    }

    /**
     * Generic in-air check.
     * @param thisMove
     * @param data
     * @return
     */
    private boolean checkInAir(final VehicleMoveData thisMove, final MovingData data) {

        // TODO: Distinguish sfJumpPhase and inAirDescendCount (after reaching the highest point).

        if (data.debug) {
            debugDetails.add("air-air");
        }

        if (checkDetails.canJump) {
            // TODO: Max. y-distance to set-back.
            // TODO: Friction.
        }
        else {
            if (thisMove.yDistance > 0.0) {
                tags.add("ascend_at_all");
                return true;
            }
        }

        boolean violation = false;
        // Absolute vertical distance to set back.
        // TODO: Add something like this.
        //            final double setBackYdistance = to.getY() - data.vehicleSetBacks.getValidSafeMediumEntry().getY();
        //            if (data.sfJumpPhase > 4) {
        //                double estimate = Math.min(2.0, MagicVehicle.boatGravityMin * ((double) data.sfJumpPhase / 4.0) * ((double) data.sfJumpPhase / 4.0 + 1.0) / 2.0);
        //                if (setBackYdistance > -estimate) {
        //                    tags.add("slow_fall_vdistsb");
        //                    return true;
        //                }
        //            }
        // Enforce falling speed (vdist) envelope by in-air phase count.
        // Slow falling (vdist), do not bind to descending in general.
        // TODO: Distinguish gravity by vehicle type or not (plus max fall target).
        final double minDescend = -MagicVehicle.boatGravityMin * (checkDetails.canJump ? Math.max(data.sfJumpPhase - MagicVehicle.maxJumpPhaseAscend, 0) : data.sfJumpPhase);
        final double maxDescend = -MagicVehicle.boatGravityMax * data.sfJumpPhase - 0.5;
        if (data.sfJumpPhase > (checkDetails.canJump ? MagicVehicle.maxJumpPhaseAscend : 1)
                && thisMove.yDistance > Math.max(minDescend, -MagicVehicle.boatVerticalFallTarget)) {
            tags.add("slow_fall_vdist");
            violation = true;
        }
        // Fast falling (vdist).
        else if (data.sfJumpPhase > 1 && thisMove.yDistance < maxDescend) {
            tags.add("fast_fall_vdist");
            violation = true;
        }
        if (violation) {
            // Post violation detection workarounds.
            if (MagicVehicle.oddInAir(thisMove, minDescend, maxDescend, checkDetails, data)) {
                violation = false;
                checkDetails.checkDescendMuch = checkDetails.checkAscendMuch = false; // (Full envelope has been checked.)
            }
            if (data.debug) {
                debugDetails.add("minDescend: " + minDescend);
                debugDetails.add("maxDescend: " + maxDescend);
            }
        }
        return violation;
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

}
