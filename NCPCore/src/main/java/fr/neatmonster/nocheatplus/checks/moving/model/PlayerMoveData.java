package fr.neatmonster.nocheatplus.checks.moving.model;

import fr.neatmonster.nocheatplus.checks.CheckType;

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
     * (micro-) moves by the player which could not be checked.
     */
    public boolean mightBeMultipleMoves;

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
        flyCheck = CheckType.UNKNOWN;
        modelFlying = null;
        mightBeMultipleMoves = false;
        // Super class last, because it'll set valid to true in the end.
        super.resetBase();
    }

}
