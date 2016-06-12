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
package fr.neatmonster.nocheatplus.checks.moving.magic;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.player.Passable;
import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Lost ground workarounds.
 * 
 * @author asofold
 *
 */
public class LostGround {

    /**
     * Check if touching the ground was lost (client did not send, or server did not put it through).
     * @param player
     * @param from
     * @param to
     * @param hDistance
     * @param yDistance
     * @param sprinting
     * @param data
     * @param cc
     * @return If touching the ground was lost.
     */
    public static boolean lostGround(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc, final Collection<String> tags) {
        // TODO: Regroup with appropriate conditions (toOnGround first?).
        // TODO: Some workarounds allow step height (0.6 on MC 1.8).
        // TODO: yDistance limit does not seem to be appropriate.
        if (yDistance >= -0.7 && yDistance <= Math.max(cc.sfStepHeight, LiftOffEnvelope.NORMAL.getMaxJumpGain(data.jumpAmplifier) + 0.174)) { // MovingUtil.estimateJumpLiftOff(player, data, 0.174))) {
            // "Mild" Ascending / descending.
            //Ascending
            if (yDistance >= 0) {
                if (lastMove.toIsValid && lostGroundAscend(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags)) {
                    return true;
                }
            }
            // Descending.
            if (yDistance <= 0) {
                if (lostGroundDescend(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags)) {
                    return true;	
                }
            }
        }
        else if (yDistance < -0.7) {
            // Clearly descending.
            // TODO: Might want to remove this one.
            if (lastMove.toIsValid && hDistance <= 0.5) {
                if (lostGroundFastDescend(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a ground-touch has been lost due to event-sending-frequency or other reasons.<br>
     * This is for ascending only (yDistance >= 0). Needs last move data.
     * @param player
     * @param from
     * @param loc 
     * @param to
     * @param hDistance
     * @param yDistance
     * @param sprinting
     * @param data
     * @param cc
     * @return
     */
    private static boolean lostGroundAscend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc, final Collection<String> tags) {
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final double setBackYDistance = to.getY() - data.getSetBackY();
        // Step height related.
        // TODO: Combine / refine preconditions for step height related.
        // TODO: || yDistance <= jump estimate?
        if (yDistance <= cc.sfStepHeight && hDistance <= 1.5) { // hDistance is arbitrary, just to confine.
            if (setBackYDistance <= Math.max(0.0, 1.3 + 0.2 * data.jumpAmplifier)) {
                // Half block step up (definitive).
                // TODO: && hDistance < 0.5  ~  switch to about 2.2 * baseSpeed once available.
                if (to.isOnGround()) {
                    // TODO: hDistance > 0.0
                    if (lastMove.yDistance < 0.0 || yDistance <= cc.sfStepHeight && from.isOnGround(cc.sfStepHeight - yDistance)) {
                        return applyLostGround(player, from, true, thisMove, data, "step", tags);
                    }
                }
                // Noob tower (moving up placing blocks underneath). Rather since 1.9: player jumps off with 0.4 speed but ground within 0.42.
                // TODO: Confine by actually having placed a block nearby, possibly rather low jumpphase (3, 4).
                final double maxJumpGain = data.liftOffEnvelope.getMaxJumpGain(data.jumpAmplifier);
                if (yDistance > 0.0 && yDistance < maxJumpGain && lastMove.yDistance < 0.0 && maxJumpGain > yDistance 
                        && Math.abs(lastMove.yDistance) + Magic.GRAVITY_MAX > cc.yOnGround + maxJumpGain - yDistance
                        && from.isOnGround(cc.yOnGround + maxJumpGain - yDistance)) {
                    return applyLostGround(player, from, true, thisMove, data, "nbtwr", tags);
                }
            }

            // Could step up (but might move to another direction, potentially).
            if (lastMove.yDistance < 0.0) { // TODO: <= ?
                // Generic could step.
                // TODO: Possibly confine margin depending on side, moving direction (see client code).
                // TODO: Should this also be checked vs. last from?
                if (BlockProperties.isOnGroundShuffled(to.getBlockCache(), from.getX(), from.getY() + cc.sfStepHeight, from.getZ(), to.getX(), to.getY(), to.getZ(), 0.1 + from.getBoxMarginHorizontal(), to.getyOnGround(), 0.0)) {
                    // TODO: Set a data property, so vdist does not trigger (currently: scan for tag)
                    // TODO: !to.isOnGround?
                    return applyLostGround(player, from, false, thisMove, data, "couldstep", tags);
                }
                // Close by ground miss (client side blocks y move, but allows h move fully/mostly, missing the edge on server side).
                // Possibly confine by more criteria.
                if (!to.isOnGround()) { // TODO: Note, that there may be cases with to on ground (!).
                    // (Use covered area to last from.)
                    // TODO: Plausible: last to is about this from?
                    // TODO: Otherwise cap max. amount (seems not really possible, could confine by passable checking).
                    // TODO: Might estimate by the yDist from before last from (cap x2 and y2).
                    // TODO: A ray-tracing version of isOnground?
                    if (lostGroundEdgeAsc(player, from.getBlockCache(), from.getWorld(), from.getX(), from.getY(), from.getZ(), from.getBoxMarginHorizontal(), from.getyOnGround(), lastMove, data, "asc1", tags, from.getMCAccess())) {
                        return true;
                    }

                    // Special cases.
                    if (yDistance == 0.0 && lastMove.yDistance <= -0.23 && (hDistance <= lastMove.hDistance * 1.1)) {
                        // Similar to couldstep, with 0 y-distance but slightly above any ground nearby (no micro move!).
                        // TODO: (hDistance <= data.sfLastHDist || hDistance <= hDistanceBaseRef)
                        // TODO: Confining in x/z direction in general: should detect if collided in that direction (then skip the x/z dist <= last time).
                        // TODO: Temporary test (should probably be covered by one of the above instead).
                        // TODO: Code duplication with edgeasc7 below.
                        if (lostGroundEdgeAsc(player, from.getBlockCache(), to.getWorld(), to.getX(), to.getY(), to.getZ(), from.getX(), from.getY(), from.getZ(), hDistance, to.getBoxMarginHorizontal(), 0.3, data, "asc5", tags, from.getMCAccess())) {
                            return true;
                        }
                    }

                    else if (from.isOnGround(from.getyOnGround(), 0.0625, 0.0)) {
                        // (Minimal margin.)
                        //data.sfLastAllowBunny = true; // TODO: Maybe a less powerful flag (just skipping what is necessary).
                        return applyLostGround(player, from, false, thisMove, data, "edgeasc2", tags); // Maybe true ?
                    }
                }
            }
        }
        // Nothing found.
        return false;
    }

    /**
     * Preconditions move dist is 0, not on ground, last h dist > 0, last y dist
     * < 0. Needs last move data.
     * 
     * @param player
     * @param from
     * @param loc
     * @param to
     * @param hDistance
     * @param yDistance
     * @param sprinting
     * @param data
     * @param cc
     * @return
     */
    public static boolean lostGroundStill(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc, final Collection<String> tags) {
        if (lastMove.yDistance <= -0.23) {
            // TODO: Code duplication with edgeasc5 above.
            if (lostGroundEdgeAsc(player, from.getBlockCache(), to.getWorld(), to.getX(), to.getY(), to.getZ(), from.getX(), from.getY(), from.getZ(), hDistance, to.getBoxMarginHorizontal(), 0.3, data, "asc7", tags, from.getMCAccess())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vertical collision with ground on client side, shifting over an edge with
     * the horizontal move. Needs last move data.
     * 
     * @param player
     * @param blockCache
     * @param world
     * @param x1
     *            Target position.
     * @param y1
     * @param z1
     * @param boxMarginHorizontal
     *            Center to edge, at some resolution.
     * @param yOnGround
     * @param data
     * @param tag
     * @return
     */
    private static boolean lostGroundEdgeAsc(final Player player, final BlockCache blockCache, final World world, final double x1, final double y1, final double z1, final double boxMarginHorizontal, final double yOnGround, final PlayerMoveData lastMove, final MovingData data, final String tag, final Collection<String> tags, final MCAccess mcAccess) {
        return lostGroundEdgeAsc(player, blockCache, world, x1, y1, z1, lastMove.from.getX(), lastMove.from.getY(), lastMove.from.getZ(), lastMove.hDistance, boxMarginHorizontal, yOnGround, data, tag, tags, mcAccess);
    }

    /**
     * 
     * @param player
     * @param blockCache
     * @param world
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @param hDistance2
     * @param boxMarginHorizontal
     *            Center to edge, at some resolution.
     * @param yOnGround
     * @param data
     * @param tag
     * @param tags
     * @param mcAccess
     * @return
     */
    private static boolean lostGroundEdgeAsc(final Player player, final BlockCache blockCache, final World world, 
            final double x1, final double y1, final double z1, double x2, final double y2, double z2, 
            final double hDistance2, final double boxMarginHorizontal, final double yOnGround, 
            final MovingData data, final String tag, final Collection<String> tags, final MCAccess mcAccess) {
        // First: calculate vector towards last from.
        x2 -= x1;
        z2 -= z1;
        // double y2 = data.fromY - y1; // Just for consistency checks (lastYDist).
        // Second: cap the size of the extra box (at least horizontal).
        double fMin = 1.0; // Factor for capping.
        if (Math.abs(x2) > hDistance2) {
            fMin = Math.min(fMin, hDistance2 / Math.abs(x2));
        }
        if (Math.abs(z2) > hDistance2) {
            fMin = Math.min(fMin, hDistance2 / Math.abs(z2));
        }
        // TODO: Further / more precise ?
        // Third: calculate end points.
        x2 = fMin * x2 + x1;
        z2 = fMin * z2 + z1;
        // Finally test for ground.
        // (We don't add another xz-margin here, as the move should cover ground.)
        if (BlockProperties.isOnGroundShuffled(blockCache, x1, y1, z1, x2, y1, z2, boxMarginHorizontal, yOnGround, 0.0)) {
            //data.sfLastAllowBunny = true; // TODO: Maybe a less powerful flag (just skipping what is necessary).
            // TODO: data.fromY for set back is not correct, but currently it is more safe (needs instead: maintain a "distance to ground").
            return applyLostGround(player, new Location(world, x2, y2, z2), true, data.playerMoves.getCurrentMove(), data, "edge" + tag, tags, mcAccess); // Maybe true ?
        } else {
            return false;
        }
    }

    /**
     * Check if a ground-touch has been lost due to event-sending-frequency or
     * other reasons.<br>
     * This is for descending "mildly" only (-0.5 <= yDistance <= 0). Needs last
     * move data.
     * 
     * @param player
     * @param from
     * @param to
     * @param hDistance
     * @param yDistance
     * @param sprinting
     * @param data
     * @param cc
     * @return
     */
    private static boolean lostGroundDescend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc, final Collection<String> tags) {
        // TODO: re-organize for faster exclusions (hDistance, yDistance).
        // TODO: more strict conditions 
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final double setBackYDistance = to.getY() - data.getSetBackY();

        // Collides vertically.
        // Note: checking loc should make sense, rather if loc is higher than from?
        if (yDistance < 0.0 && !to.isOnGround() && from.isOnGround(from.getY() - to.getY() + 0.001)) {
            // Test for passability of the entire box, roughly from feet downwards.
            // TODO: Efficiency with Location instances.
            // TODO: Full bounds check (!).
            final Location ref = from.getLocation();
            ref.setY(to.getY());
            if (Passable.isPassable(from.getLocation(), ref)) {
                // TODO: Needs new model (store detailed on-ground properties).
                return applyLostGround(player, from, false, thisMove, data, "vcollide", tags);
            }
        }

        if (!lastMove.toIsValid) {
            return false;
        }

        if (data.sfJumpPhase <= 7) {
            // Check for sprinting down blocks etc.
            if (lastMove.yDistance <= yDistance && setBackYDistance < 0 && !to.isOnGround()) {
                // TODO: setbackydist: <= - 1.0 or similar
                // TODO: <= 7 might work with speed II, not sure with above.
                // TODO: account for speed/sprint
                // TODO: account for half steps !?
                if (from.isOnGround(0.6, 0.4, 0.0, 0L) ) {
                    // TODO: further narrow down bounds ?
                    // Temporary "fix".
                    // TODO: Seems to virtually always be preceded by a "vcollide" move.
                    return applyLostGround(player, from, true, thisMove, data, "pyramid", tags);
                }
            }

            // Check for jumping up strange blocks like flower pots on top of other blocks.
            if (yDistance == 0.0 && lastMove.yDistance > 0.0 && lastMove.yDistance < 0.25 && data.sfJumpPhase <= Math.max(0, 6 + data.jumpAmplifier * 3.0) && setBackYDistance > 1.0 && setBackYDistance < Math.max(0.0, 1.5 + 0.2 * data.jumpAmplifier) && !to.isOnGround()) {
                // TODO: confine by block types ?
                if (from.isOnGround(0.25, 0.4, 0, 0L) ) {
                    // Temporary "fix".
                    //data.sfThisAllowBunny = true;
                    return applyLostGround(player, from, true, thisMove, data, "ministep", tags);
                }
            }
        }
        // Lost ground while falling onto/over edges of blocks.
        if (yDistance < 0 && hDistance <= 1.5 && lastMove.yDistance < 0.0 && yDistance > lastMove.yDistance && !to.isOnGround()) {
            // TODO: Should this be an extra lost-ground(to) check, setting toOnGround  [for no-fall no difference]?
            // TODO: yDistance <= 0 might be better.
            // Also clear accounting data.
            //			if (to.isOnGround(0.5) || from.isOnGround(0.5)) {
            if (from.isOnGround(0.5, 0.2, 0) || to.isOnGround(0.5, Math.min(0.2, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance))) {
                return applyLostGround(player, from, true, thisMove, data, "edgedesc", tags);
            }
        }

        // Nothing found.
        return false;
    }

    /**
     * Check if a ground-touch has been lost due to event-sending-frequency or
     * other reasons.<br>
     * This is for fast descending only (yDistance < -0.5). Needs last move
     * data.
     * 
     * @param player
     * @param from
     * @param to
     * @param hDistance
     * @param yDistance
     * @param sprinting
     * @param data
     * @param cc
     * @return
     */
    private static boolean lostGroundFastDescend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc, final Collection<String> tags) {
        // TODO: re-organize for faster exclusions (hDistance, yDistance).
        // TODO: more strict conditions 
        // Lost ground while falling onto/over edges of blocks.
        if (yDistance > lastMove.yDistance && !to.isOnGround()) {
            // TODO: Should this be an extra lost-ground(to) check, setting toOnGround  [for no-fall no difference]?
            // TODO: yDistance <= 0 might be better.
            // Also clear accounting data.
            // TODO: stairs ?
            // TODO: Can it be safe to only check to with raised margin ? [in fact should be checked from higher yMin down]
            // TODO: Interpolation method (from to)?
            if (from.isOnGround(0.5, 0.2, 0) || to.isOnGround(0.5, Math.min(0.3, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance))) {
                // (Usually yDistance should be -0.078)
                return applyLostGround(player, from, true, data.playerMoves.getCurrentMove(), data, "fastedge", tags);
            }
        }
        return false;
    }

    /**
     * Apply lost-ground workaround.
     * @param player
     * @param refLoc
     * @param setBackSafe If to use the given location as set-back.
     * @param data
     * @param tag Added to "lostground_" as tag.
     * @return Always true.
     */
    private static boolean applyLostGround(final Player player, final Location refLoc, final boolean setBackSafe, final PlayerMoveData thisMove, final MovingData data, final String tag, final Collection<String> tags, final MCAccess mcAccess) {
        if (setBackSafe) {
            data.setSetBack(refLoc);
        }
        else {
            // Keep Set-back.
        }
        return applyLostGround(player, thisMove, data, tag, tags, mcAccess);
    }

    /**
     * Apply lost-ground workaround.
     * @param player
     * @param refLoc
     * @param setBackSafe If to use the given location as set-back.
     * @param data
     * @param tag Added to "lostground_" as tag.
     * @return Always true.
     */
    private static boolean applyLostGround(final Player player, final PlayerLocation refLoc, final boolean setBackSafe, final PlayerMoveData thisMove, final MovingData data, final String tag, final Collection<String> tags) {
        // Set the new setBack and reset the jumpPhase.
        if (setBackSafe) {
            data.setSetBack(refLoc);
        }
        else {
            // Keep Set-back.
        }
        return applyLostGround(player, thisMove, data, tag, tags, refLoc.getMCAccess());
    }

    /**
     * Apply lost-ground workaround (data adjustments and tag).
     * @param player
     * @param refLoc
     * @param setBackSafe If to use the given location as set-back.
     * @param data
     * @param tag Added to "lostground_" as tag.
     * @return Always true.
     */
    private static boolean applyLostGround(final Player player, final PlayerMoveData thisMove, final MovingData data, final String tag, final Collection<String> tags, final MCAccess mcAccess) {
        // Reset the jumpPhase.
        // ? set jumpphase to 1 / other, depending on stuff ?
        data.sfJumpPhase = 0;
        data.jumpAmplifier = MovingUtil.getJumpAmplifier(player, mcAccess);
        data.clearAccounting();
        // Tell NoFall that we assume the player to have been on ground somehow.
        thisMove.touchedGround = true;
        thisMove.touchedGroundWorkaround = true;
        tags.add("lostground_" + tag);
        return true;
    }

}
