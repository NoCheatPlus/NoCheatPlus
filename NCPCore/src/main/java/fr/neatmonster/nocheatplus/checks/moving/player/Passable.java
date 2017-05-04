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
package fr.neatmonster.nocheatplus.checks.moving.player;

import java.util.Arrays;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.compat.blocks.changetracker.BlockChangeTracker;
import fr.neatmonster.nocheatplus.utilities.collision.ICollidePassable;
import fr.neatmonster.nocheatplus.utilities.collision.PassableAxisTracing;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

public class Passable extends Check {

    /** TESTING RATHER. */

    // TODO: Configuration.
    // TODO: Test cases.
    // TODO: Should keep an eye on passable vs. on-ground, when checking with reduced margins.
    // rt_xzFactor = 1.0; // Problems: Doors, fences. 
    private static double rt_xzFactor = 0.98;
    // rt_heightFactor = 1.0; // Since 10.2 (at some point) passable FP with 2-high ceiling.
    private static double rt_heightFactor = 0.99999999;

    /**
     * Convenience for player moving, to keep a better overview.
     * 
     * @param from
     * @param to
     * @return
     */
    public static boolean isPassable(Location from, Location to) {
        return BlockProperties.isPassableAxisWise(from, to);
    }

    private final ICollidePassable rayTracingActual = new PassableAxisTracing();
    private final BlockChangeTracker blockTracker;

    public Passable() {
        super(CheckType.MOVING_PASSABLE);
        // TODO: Configurable maxSteps?
        rayTracingActual.setMaxSteps(60);
        blockTracker = NCPAPIProvider.getNoCheatPlusAPI().getBlockChangeTracker();
    }

    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, 
            final MovingData data, final MovingConfig cc, final int tick, final boolean useBlockChangeTracker) {
        return checkActual(player, from, to, data, cc, tick, useBlockChangeTracker);
    }

    private Location checkActual(final Player player, final PlayerLocation from, final PlayerLocation to, 
            final MovingData data, final MovingConfig cc, final int tick, final boolean useBlockChangeTracker) {
        // TODO: Distinguish feet vs. box.

        // Block distances (sum, max) for from-to (not for loc!).
        final int manhattan = from.manhattan(to);

        // Check default order first.
        String newTag = checkRayTracing(player, from, to, manhattan, data, cc, tick, useBlockChangeTracker);
        if (newTag != null) {
            // Direct return.
            return potentialViolation(player, from, to, manhattan, newTag, data, cc);
        }
        // TODO: Return already here, if not colliding?
        // No early return on violation happened.
        // (Might consider if vl>=1: only decrease if from and loc are passable too, though micro...)
        data.passableVL *= 0.99;
        return null;
    }

    private String checkRayTracing(final Player player, final PlayerLocation from, final PlayerLocation to,
            final int manhattan, final MovingData data, final MovingConfig cc, final int tick, final boolean useBlockChangeTracker) {
        String tags = null;
        // NOTE: axis order is set externally.
        setNormalMargins(rayTracingActual, from);
        rayTracingActual.set(from, to);
        rayTracingActual.setIgnoreInitiallyColliding(true);
        if (useBlockChangeTracker) { // TODO: Extra flag for 'any' block changes.
            rayTracingActual.setBlockChangeTracker(blockTracker, data.blockChangeRef, tick, from.getWorld().getUID());
        }
        //rayTracing.setCutOppositeDirectionMargin(true);
        rayTracingActual.loop();
        rayTracingActual.setIgnoreInitiallyColliding(false);
        //rayTracing.setCutOppositeDirectionMargin(false);
        if (rayTracingActual.collides()) {
            tags = "raytracing_collide_";
        }
        else if (rayTracingActual.getStepsDone() >= rayTracingActual.getMaxSteps()) {
            tags = "raytracing_maxsteps_";
        }
        if (data.debug) {
            debugExtraCollisionDetails(player, rayTracingActual, "std");
        }
        rayTracingActual.cleanup();
        return tags;
    }

    /**
     * Default/normal margins.
     * @param rayTracing
     * @param from
     */
    private void setNormalMargins(final ICollidePassable rayTracing, final PlayerLocation from) {
        rayTracing.setMargins(from.getBoxMarginVertical() * rt_heightFactor, from.getWidth() / 2.0 * rt_xzFactor); // max from/to + resolution ?
    }

    /**
     * Axis-wise ray-tracing violation skipping conditions.
     * 
     * @param player
     * @param from
     * @param to
     * @param manhattan
     * @param tags
     * @param data
     * @param cc
     * @return
     */
    private Location potentialViolation(final Player player, 
            final PlayerLocation from, final PlayerLocation to, final int manhattan, 
            String tags, final MovingData data, final MovingConfig cc) {

        // TODO: Might need the workaround for fences.

        return actualViolation(player, from, to, tags, data, cc);
    }

    private Location actualViolation(final Player player, final PlayerLocation from, final PlayerLocation to,
            final String tags, final MovingData data, final MovingConfig cc) {
        Location setBackLoc = null; // Alternative to from.getLocation().

        // Prefer the set back location from the data.
        if (data.hasSetBack()) {
            setBackLoc = data.getSetBack(to);
            if (data.debug) {
                debug(player, "Using set back location for passable.");
            }
        }

        // Return the reset position.
        data.passableVL += 1d;
        final ViolationData vd = new ViolationData(this, player, data.passableVL, 1, cc.passableActions);
        if (data.debug || vd.needsParameters()) {
            vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
            vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
            vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", TrigUtil.distance(from, to)));
            // TODO: Consider adding from.getTypeId() too, if blocks differ and non-air.
            vd.setParameter(ParameterName.BLOCK_ID, "" + to.getTypeId());
            if (!tags.isEmpty()) {
                vd.setParameter(ParameterName.TAGS, tags);
            }
        }
        if (executeActions(vd).willCancel()) {
            // TODO: Consider another set back position for this, also keeping track of players moving around in blocks.
            final Location newTo;
            if (setBackLoc != null) {
                // Ensure the given location is cloned.
                newTo = LocUtil.clone(setBackLoc);
            } else {
                newTo = from.getLocation();
                if (data.debug) {
                    debug(player, "Using from location for passable.");
                }
            }
            newTo.setYaw(to.getYaw());
            newTo.setPitch(to.getPitch());
            return newTo;
        }
        else{
            // No cancel action set.
            return null;
        }
    }

    /**
     * Debug only if colliding.
     * 
     * @param player
     * @param rayTracing
     * @param tag
     */
    private void debugExtraCollisionDetails(Player player, ICollidePassable rayTracing, String tag) {
        if (rayTracing.collides()) {
            debug(player, "Raytracing collision with order " + Arrays.toString(rayTracing.getAxisOrder()) 
            + " (" + tag + "): " + rayTracing.getCollidingAxis());
        }
        else if (rayTracing.getStepsDone() >= rayTracing.getMaxSteps()) {
            debug(player, "Raytracing max steps exceeded (" + tag + "): "+ rayTracing.getCollidingAxis());
        }
        // TODO: Detect having used past block changes and log or set a tag.
    }

}
