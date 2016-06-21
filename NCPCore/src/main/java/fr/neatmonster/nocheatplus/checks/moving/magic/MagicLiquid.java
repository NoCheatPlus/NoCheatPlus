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

import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * Magic workarounds for moving in liquid (SurvivalFly.vDistLiquid).
 * 
 * @author asofold
 *
 */
public class MagicLiquid {

    /**
     * 
     * @param from
     * @param to
     * @param baseSpeed
     * @param frictDist
     * @param thisMove
     * @param lastMove
     * @param data
     * @return The allowed distance for reference, in case the move is allowed.
     *         If no workaround applies, null is returned.
     */
    public static Double liquidWorkarounds(final PlayerLocation from, final PlayerLocation to, final double baseSpeed, final double frictDist, final PlayerMoveData lastMove, final MovingData data) {
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final double yDistance = thisMove.yDistance;
        // Special cases.
        // TODO: Re-arrange.
        final PlayerMoveData pastMove1 = data.playerMoves.getSecondPastMove();
        if (yDistance >= 0) {
            // TODO: liftOffEnvelope: refine conditions (general) , should be near water level.
            // TODO: 1.5 high blocks ?
            // TODO: Conditions seem warped.
            if (yDistance <= 0.5) {
                if (lastMove.toIsValid && yDistance < lastMove.yDistance
                        && lastMove.yDistance - yDistance > Math.max(0.001, yDistance - baseSpeed)) {
                    // Decrease more than difference to baseSpeed.
                    return yDistance;
                }
                if (yDistance <= data.liftOffEnvelope.getMaxJumpGain(data.jumpAmplifier) && !BlockProperties.isLiquid(from.getTypeIdAbove()) 
                        // TODO: What now !?
                        || !to.isInLiquid() // TODO: impossible !?
                        || (thisMove.to.onGround || lastMove.toIsValid && lastMove.yDistance - yDistance >= 0.010 || to.isAboveStairs())) {
                    double vAllowedDistance = baseSpeed + 0.5;
                    double vDistanceAboveLimit = yDistance - vAllowedDistance;
                    if (vDistanceAboveLimit <= 0.0) {
                        return vAllowedDistance;
                    }
                }
            }

            if (lastMove.toIsValid) {
                // Lenient on marginal violation if speed decreases by 'enough'.
                // (Observed on 'dirty' phase.)
                if (Math.abs(frictDist - yDistance) <= 2.0 * Magic.GRAVITY_MAX
                        && yDistance < lastMove.yDistance - 4.0 * Math.abs(frictDist - yDistance)
                        ) {
                    return yDistance;
                }
                // Jumping with velocity into water from below, just slightly more decrease than gravity, twice.
                if (yDistance > frictDist && yDistance < lastMove.yDistance - Magic.GRAVITY_MAX && data.insideMediumCount <= 1) {
                    // (Should be able to do without aw-ww-ww confinement.)
                    // (dirty seems to be set/kept reliably)
                    return yDistance;
                }
                if (pastMove1.toIsValid && pastMove1.to.extraPropertiesValid) {
                    // Cases considering two past moves with moving up.
                    // Splash move with space space pressed (this move leaving liquid).
                    if (pastMove1.yDistance > 0.0 && thisMove.yDistance > 0.0
                            && pastMove1.yDistance - Magic.GRAVITY_MAX > lastMove.yDistance 
                            && lastMove.yDistance - Magic.GRAVITY_ODD > thisMove.yDistance
                            && Magic.intoLiquid(lastMove) && Magic.leavingLiquid(thisMove)
                            ) {
                        return yDistance;
                    }
                    // Velocity use in lastMove, keep air friction roughly.
                    if (!Magic.resetCond(pastMove1) && lastMove.yDistance - Magic.GRAVITY_MAX > thisMove.yDistance
                            && Magic.intoLiquid(lastMove) && Magic.leavingLiquid(thisMove)
                            ) {
                        return yDistance;
                    }
                }
            }
        }
        // Otherwise, only if last move is available.
        else if (lastMove.toIsValid) {
            // TODO: Are all these cases really for descending?
            // Falling into water, mid-speed (second move after diving in).
            if (yDistance > -0.9 && yDistance < lastMove.yDistance 
                    && Math.abs(yDistance - lastMove.yDistance) <= Magic.GRAVITY_MAX + Magic.GRAVITY_MIN 
                    && yDistance - lastMove.yDistance < -Magic.GRAVITY_MIN
                    //&& !BlockProperties.isLiquid(to.getTypeId(to.getBlockX(), Location.locToBlock(to.getY() + to.getEyeHeight()), to.getBlockZ()))
                    ) {
                return lastMove.yDistance - Magic.GRAVITY_MAX - Magic.GRAVITY_MIN;
            }
            // Increase speed slightly on second in-medium move (dirty flag may have been reset).
            else if (data.insideMediumCount <= 1
                    // (No strong decrease:)
                    && yDistance > lastMove.yDistance - Magic.GRAVITY_MAX
                    && (
                            // Ordinary (some old case).
                            lastMove.yDistance < 0.8 && yDistance < lastMove.yDistance - Magic.GRAVITY_SPAN
                            // Check with three moves, rather shortly touching water.
                            || lastMove.yDistance < -0.5 // Arbitrary, actually observed has been < -1.0
                            && pastMove1.toIsValid && pastMove1.to.extraPropertiesValid
                            && Math.abs(pastMove1.yDistance - lastMove.yDistance) < Magic.GRAVITY_MIN
                            && yDistance <= lastMove.yDistance
                            && Magic.inLiquid(lastMove) && Magic.intoLiquid(pastMove1)
                            )
                    ) {
                return yDistance;
            }
            // In-water rough near-0-inversion from allowed speed to a negative amount, little more than allowed (magic -0.2 roughly).
            else if (lastMove.yDistance >= Magic.GRAVITY_MAX / 10.0 && lastMove.yDistance <= Magic.GRAVITY_MAX + Magic.GRAVITY_MIN / 2.0
                    && yDistance < 0.0 && yDistance > -2.0 * Magic.GRAVITY_MAX - Magic.GRAVITY_MIN / 2.0
                    && to.isInLiquid() // TODO: Might skip the liquid check, though.
                    && lastMove.from.inLiquid && lastMove.to.extraPropertiesValid && lastMove.to.inLiquid // TODO: in water only?
                    ) {
                return yDistance;
            }
            // Lava rather.
            else if (data.lastFrictionVertical < 0.65 // (Random, but smaller than water.) 
                    && (
                            // Moving downstream.
                            lastMove.yDistance < 0.0 && yDistance > -0.5 && yDistance < lastMove.yDistance 
                            && lastMove.yDistance - yDistance < Magic.GRAVITY_MIN && BlockProperties.isDownStream(from, to)
                            // Mix of gravity and base speed [careful: relates to water base speed].
                            || lastMove.yDistance < 0.0 && yDistance > -baseSpeed - Magic.GRAVITY_MAX && yDistance < lastMove.yDistance
                            && lastMove.yDistance - yDistance > Magic.GRAVITY_SPAN
                            && Math.abs(lastMove.yDistance + baseSpeed) < 0.25 * baseSpeed
                            // Falling slightly too fast in lava.
                            || data.insideMediumCount == 1 || data.insideMediumCount == 2 
                            && lastMove.yDistance < 0.0 && yDistance < lastMove.yDistance 
                            && yDistance - lastMove.yDistance > -Magic.GRAVITY_MIN && yDistance > -0.65
                            )
                    ) {
                return yDistance;
            }
        }

        // TODO: Also DOWNSTREAM !?

        return null;
    }

}
