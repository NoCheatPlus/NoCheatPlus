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

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.collision.ICollidePassable;
import fr.neatmonster.nocheatplus.utilities.collision.PassableAxisTracing;
import fr.neatmonster.nocheatplus.utilities.collision.PassableRayTracing;

public class Passable extends Check {

    /** TESTING RATHER. */

    // TODO: Make this configurable once a working set of settings has been found.
    // TODO: Once made configurable... intense testing... and test cases.
    private static boolean rt_legacy = true;
    // TODO: rt_xzFactor = 1.0; // Problems: Doors, fences. 
    private static double rt_xzFactor = 0.98;
    // TODO: Test bumping head into things.
    private static double rt_heightFactor = 1.0;

    /**
     * Convenience for player moving, to keep a better overview.
     * 
     * @param from
     * @param to
     * @return
     */
    public static boolean isPassable(Location from, Location to) {
        return rt_legacy ? BlockProperties.isPassable(from, to) : BlockProperties.isPassableAxisWise(from, to);
    }

    // TODO: Store both and select on check (with config then).
    private final ICollidePassable rayTracing = rt_legacy ? new PassableRayTracing() : new PassableAxisTracing();

    public Passable() {
        super(CheckType.MOVING_PASSABLE);
        rayTracing.setMaxSteps(60); // TODO: Configurable ?
    }

    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc)
    {
        // TODO: if (!from.isSameCoords(loc)) {...check passable for loc -> from !?... + sf etc too?}
        // TODO: Future: Account for the players bounding box? [test very-strict setting for at least the end points...]
        String tags = "";
        // Block distances (sum, max) for from-to (not for loc!).
        final int manhattan = from.manhattan(to);
        // Skip moves inside of ignored blocks right away [works as long as we only check between foot-locations].
        if (manhattan <= 1 && BlockProperties.isPassable(from.getTypeId())) {
            // TODO: Monitor: BlockProperties.isPassable checks slightly different than before.
            if (manhattan == 0){
                return null;
            } else {
                // manhattan == 1
                if (BlockProperties.isPassable(to.getTypeId())) {
                    return null;
                }
            }
        } 
        boolean toPassable = to.isPassable();
        // General condition check for using ray-tracing.
        if (toPassable && cc.passableRayTracingCheck && (!cc.passableRayTracingBlockChangeOnly || manhattan > 0)) {
            rayTracing.setMargins(from.getEyeHeight() * rt_heightFactor, from.getWidth() / 2.0 * rt_xzFactor); // max from/to + resolution ?
            rayTracing.set(from, to);
            rayTracing.loop();
            if (rayTracing.collides() || rayTracing.getStepsDone() >= rayTracing.getMaxSteps()) {
                if (data.debug) {
                    debugExtraCollisionDetails(player, rayTracing, "initial");
                }
                final int maxBlockDist = manhattan <= 1 ? manhattan : from.maxBlockDist(to);
                if (maxBlockDist <= 1 && rayTracing.getStepsDone() == 1 && !from.isPassable()) {
                    // Redo ray-tracing for moving out of blocks.
                    if (collidesIgnoreFirst(from, to)) {
                        toPassable = false;
                        tags = "raytracing_2x_";
                        if (data.debug) {
                            debugExtraCollisionDetails(player, rayTracing, "ingorFirst");
                        }
                    }
                    else if (data.debug) {
                        debug(player, "Allow moving out of a block.");
                    }
                }
                else{
                    if (!allowsSplitMove(from, to, manhattan, data)) {
                        toPassable = false;
                        tags = "raytracing_";
                    }
                }
            }
            // TODO: Future: If accuracy is demanded, also check the head position (or bounding box right away).
            rayTracing.cleanup();
        }

        // TODO: Checking order: If loc is not the same as from, a quick return here might not be wanted.
        if (toPassable) {
            // Quick return.
            // (Might consider if vl>=1: only decrease if from and loc are passable too, though micro...)
            data.passableVL *= 0.99;
            return null;
        } else {
            return potentialViolation(player, from, to, manhattan, tags, data, cc);
        }

    }

    private Location potentialViolation(final Player player, final PlayerLocation from, final PlayerLocation to, final int manhattan, String tags, final MovingData data, final MovingConfig cc) {
        // Moving into a block, possibly a violation.

        // First check if the player is moving from a passable location.
        // If not, the move might still be allowed, if moving inside of the same block, or from and to have head position passable.
        if (from.isPassable()) {
            // Put one workaround for 1.5 high blocks here:
            if (from.isBlockAbove(to) && (BlockProperties.getBlockFlags(to.getTypeId()) & BlockProperties.F_HEIGHT150) != 0) {
                // Check if the move went from inside of the block.
                if (BlockProperties.collidesBlock(to.getBlockCache(), from.getX(), from.getY(), from.getZ(), from.getX(), from.getY(), from.getZ(), to.getBlockX(), to.getBlockY(), to.getBlockZ(), to.getTypeId())) {
                    // Allow moving inside of 1.5 high blocks.
                    return null;
                }
            }
            // From should be the set-back.
            tags += "into";
        }

        //				} else if (BlockProperties.isPassableExact(from.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(lbX, lbY, lbZ))) {
        // (Mind that this can be the case on the same block theoretically.)
        // Keep loc as set-back.
        //				}
        else if (manhattan == 1 && to.isBlockAbove(from) && BlockProperties.isPassable(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))) {
            //				else if (to.isBlockAbove(from) && BlockProperties.isPassableExact(from.getBlockCache(), from.getX(), from.getY() + player.getEyeHeight(), from.getZ(), from.getTypeId(from.getBlockX(), Location.locToBlock(from.getY() + player.getEyeHeight()), from.getBlockZ()))) {
            // Allow the move up if the head is free.
            // TODO: Better distinguish ray-tracing (through something thin) or check to-head-passable too?
            return null;
        }
        else if (manhattan > 0) {
            // Otherwise keep from as set-back.
            tags += "cross";
        }
        else{
            // All blocks are the same, allow the move.
            return null;
        }

        Location setBackLoc = null; // Alternative to from.getLocation().

        // Prefer the set-back location from the data.
        if (data.hasSetBack()) {
            setBackLoc = data.getSetBack(to);;
            if (data.debug) {
                debug(player, "Using set-back location for passable.");
            }
        }

        // TODO: set data.set-back ? or something: still some aji here.

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
     * Test collision with ignoring the first block.
     * @param from
     * @param to
     * @return
     */
    private boolean collidesIgnoreFirst(PlayerLocation from, PlayerLocation to) {
        rayTracing.set(from, to);
        rayTracing.setIgnoreFirst();
        rayTracing.loop();
        return rayTracing.collides() || rayTracing.getStepsDone() >= rayTracing.getMaxSteps();
    }

    /**
     * Test the move split into y-move and horizontal move, provided some pre-conditions are met.
     * @param from
     * @param to
     * @param manhattan
     * @return
     */
    private boolean allowsSplitMove(final PlayerLocation from, final PlayerLocation to, final int manhattan, final MovingData data) {
        if (!rayTracing.mightNeedSplitAxisHandling()) {
            return false;
        }
        // Always check y first.
        rayTracing.set(from.getX(), from.getY(), from.getZ(), from.getX(), to.getY(), from.getZ());
        rayTracing.loop();
        if (!rayTracing.collides() && rayTracing.getStepsDone() < rayTracing.getMaxSteps()) {
            // horizontal second.
            rayTracing.set(from.getX(), to.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
            rayTracing.loop();
            if (!rayTracing.collides() && rayTracing.getStepsDone() < rayTracing.getMaxSteps()) {
                return true;
            }
        }
        // Horizontal first may be obsolete, due to splitting moves anyway and due to not having been called ever (!). 
        //        final double yDiff = to.getY() - from.getY() ;
        //        if (manhattan <= 3 && Math.abs(yDiff)  < 1.0 && yDiff < 0.0) {
        //            // Workaround for client-side calculations not being possible (y vs. horizontal move). Typically stairs.
        //            // horizontal first.
        //            if (data.debug) {
        //                DebugUtil.debug(from.getPlayer().getName() + " passable - Test horizontal move first.");
        //            }
        //            rayTracing.set(from.getX(), from.getY(), from.getZ(), to.getX(), from.getY(), to.getZ());
        //            rayTracing.loop();
        //            if (!rayTracing.collides() && rayTracing.getStepsDone() < rayTracing.getMaxSteps()) {
        //                // y second.
        //                rayTracing.set(to.getX(), from.getY(), to.getZ(), to.getX(), to.getY(), to.getZ());
        //                rayTracing.loop();
        //                if (!rayTracing.collides() && rayTracing.getStepsDone() < rayTracing.getMaxSteps()) {
        //                    return true;
        //                }
        //            }
        //        }
        if (data.debug) {
            debug(from.getPlayer(), "Raytracing collision (split move): (no details)");
        }
        return false;
    }

    private void debugExtraCollisionDetails(Player player, ICollidePassable rayTracing, String tag) {
        if (rayTracing.collides()) {
            debug(player, "Raytracing collision (" + tag + "): " + rayTracing.getCollidingAxis());
        }
        else if (rayTracing.getStepsDone() >= rayTracing.getMaxSteps()) {
            debug(player, "Raytracing max steps exceeded (" + tag + "): "+ rayTracing.getCollidingAxis());
        }
    }

}
