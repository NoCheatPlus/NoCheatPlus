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
package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.velocity.SimpleEntry;

/**
 * Include player specific data for a move.
 * 
 * @author asofold
 *
 */
public class PlayerMoveData extends MoveData {

    //////////////////////////////////////////////////////////
    // Reset with set, could be lazily set during checking.
    //////////////////////////////////////////////////////////

    // Properties of the player.

    /**
     * Typical maximum walk speed, accounting for player capabilities. Set in
     * SurvivalFly.check.
     */
    public double walkSpeed;

    // Bounds set by checks.

    /**
     * Allowed horizontal base distance (as if moving off the spot, excluding
     * bunny/friction). Set in SurvivalFly.check.
     */
    public double hAllowedDistanceBase;

    /**
     * Allowed horizontal distance (including frictions, workarounds like bunny
     * hopping). Set in SurvivalFly.check.
     */
    public double hAllowedDistance;

    // Properties involving the environment.

    /** This move was a bunny hop. */
    public boolean bunnyHop;

    // TODO: verVel/horvel used?

    // Meta stuff.

    /**
     * The fly check that was using the current data. One of MOVING_SURVIVALFLY,
     * MOVING_CREATIVEFLY, UNKNOWN.
     */
    public CheckType flyCheck;

    /**
     * The ModelFlying instance used with this move, will be null if it doesn't
     * apply.
     */
    public ModelFlying modelFlying;

    /**
     * Due to the thresholds for moving events, there could have been other
     * (micro-) moves by the player which could not be checked. One moving event
     * is split into two moves 1: from -> loc, 2: loc -> to.
     */
    public int multiMoveCount;

    /**
     * Just the used vertical velocity. Could be overridden multiple times
     * during processing of moving checks.
     */
    public SimpleEntry  verVelUsed = null;

    @Override
    protected void resetBase() {
        // Properties of the player.
        walkSpeed = 0.2;
        // Properties involving the environment.
        bunnyHop = false;
        // Bounds set by checks.
        hAllowedDistanceBase = 0.0;
        hAllowedDistance = 0.0;
        // Meta stuff.
        flyCheck = null;
        modelFlying = null;
        multiMoveCount = 0;
        verVelUsed = null;
        // Super class last, because it'll set valid to true in the end.
        super.resetBase();
    }

}
