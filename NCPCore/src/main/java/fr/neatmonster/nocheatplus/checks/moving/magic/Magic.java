package fr.neatmonster.nocheatplus.checks.moving.magic;

import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveData;

/**
 * Keeping some of the magic confined in here.
 * 
 * @author asofold
 *
 */
public class Magic {

    // Gravity.
    public static final double GRAVITY_MAX = 0.0834;
    public static final double GRAVITY_MIN = 0.0624; // TODO: Special cases go down to 0.05.
    public static final double GRAVITY_SPAN = GRAVITY_MAX - GRAVITY_MIN;
    public static final double GRAVITY_ODD = 0.05; // 19; // TODO: This should probably be min. / cleanup.
    /** Assumed minimal average decrease per move, suitable for regarding 3 moves. */
    public static final float GRAVITY_VACC = (float) (GRAVITY_MIN * 0.6);

    // Friction factor by medium (move inside of).
    public static final double FRICTION_MEDIUM_AIR = 0.98;
    /** Friction for water (default). */
    public static final double FRICTION_MEDIUM_WATER = 0.89;
    /** Friction for lava. */
    public static final double FRICTION_MEDIUM_LAVA = 0.535;

    // Horizontal speeds/modifiers.
    public static final double WALK_SPEED           = 0.221D;
    public static final double modSneak             = 0.13D / WALK_SPEED;
    //    public static final double modSprint            = 0.29 / walkSpeed; // TODO: without bunny  0.29 / practical is 0.35
    public static final double modBlock             = 0.16D / WALK_SPEED;
    public static final double modSwim              = 0.115D / WALK_SPEED;
    public static final double[] modDepthStrider    = new double[] {
        1.0,
        0.1645 / modSwim / WALK_SPEED,
        0.1995 / modSwim / WALK_SPEED,
        1.0 / modSwim, // Results in walkspeed.
    };
    public static final double modWeb               = 0.105D / WALK_SPEED; // TODO: walkingSpeed * 0.15D; <- does not work
    public static final double modIce                 = 2.5D; // 
    /** Faster moving down stream (water mainly). */
    public static final double modDownStream    = 0.19 / (WALK_SPEED * modSwim);
    /** Maximal horizontal buffer. It can be higher, but normal resetting should keep this limit. */

    public static final double hBufMax          = 1.0;
    // Vertical speeds/modifiers. 
    public static final double climbSpeed       = WALK_SPEED * 1.3; // TODO: Check if the factor is needed!  

    // Other constants.
    public static final double PAPER_DIST = 0.01;
    /**
     * Extreme move check threshold (Actual like 3.9 upwards with velocity,
     * velocity downwards may be like -1.835 max., but falling will be near 3
     * too.)
     */
    public static final double EXTREME_MOVE_DIST_VERTICAL = 4.0;
    public static final double EXTREME_MOVE_DIST_HORIZONTAL = 22.0;

    /**
     * The absolute per-tick base speed for swimming vertically.
     * 
     * @return
     */
    public static double swimBaseSpeedV() {
        // TODO: Does this have to be the dynamic walk speed (refactoring)?
        return WALK_SPEED * modSwim + 0.02;
    }

    /**
     * Test if the player is (well) within in-air falling envelope.
     * @param yDistance
     * @param lastYDist
     * @param extraGravity Extra amount to fall faster.
     * @return
     */
    public static boolean fallingEnvelope(final double yDistance, final double lastYDist, final double lastFrictionVertical, final double extraGravity) {
        if (yDistance >= lastYDist) {
            return false;
        }
        // TODO: data.lastFrictionVertical (see vDistAir).
        final double frictDist = lastYDist * lastFrictionVertical - GRAVITY_MIN;
        // TODO: Extra amount: distinguish pos/neg?
        return yDistance <= frictDist + extraGravity && yDistance > frictDist - GRAVITY_SPAN - extraGravity;
    }

    /**
     * Friction envelope testing, with a different kind of leniency (relate
     * off-amount to decreased amount), testing if 'friction' has been accounted
     * for in a sufficient but not necessarily exact way.<br>
     * In the current shape this method is meant for higher speeds rather (needs
     * a twist for low speed comparison).
     * 
     * @param thisMove
     * @param lastMove
     * @param friction
     *            Friction factor to apply.
     * @param minGravity
     *            Amount to subtract from frictDist by default.
     * @param maxOff
     *            Amount yDistance may be off the friction distance.
     * @param decreaseByOff
     *            Factor, how many times the amount being off friction distance
     *            must fit into the decrease from lastMove to thisMove.
     * @return
     */
    public static boolean enoughFrictionEnvelope(final MoveData thisMove, final MoveData lastMove, final double friction, 
            final double minGravity, final double maxOff, final double decreaseByOff) {
        // TODO: Elaborate... could have one method to test them all?
        final double frictDist = lastMove.yDistance * friction - minGravity;
        final double off = Math.abs(thisMove.yDistance - frictDist);
        return off <= maxOff && thisMove.yDistance - lastMove.yDistance <= off * decreaseByOff;
    }

    /**
     * Test for a specific move in-air -> water, then water -> in-air.
     * 
     * @param thisMove
     *            Not strictly the latest move in MovingData.
     * @param lastMove
     *            Move before thisMove.
     * @return
     */
    static boolean splashMove(final MoveData thisMove, final MoveData lastMove) {
        // Use past move data for two moves.
        return !thisMove.touchedGround && thisMove.from.inWater && !thisMove.to.resetCond // Out of water.
                && !lastMove.touchedGround && !lastMove.from.resetCond && lastMove.to.inWater // Into water.
                && excludeStaticSpeed(thisMove) && excludeStaticSpeed(lastMove)
                ;
    }

    /**
     * Test for a specific move ground/in-air -> water, then water -> in-air.
     * 
     * @param thisMove
     *            Not strictly the latest move in MovingData.
     * @param lastMove
     *            Move before thisMove.
     * @return
     */
    static boolean splashMoveNonStrict(final MoveData thisMove, final MoveData lastMove) {
        // Use past move data for two moves.
        return !thisMove.touchedGround && thisMove.from.inWater && !thisMove.to.resetCond // Out of water.
                && !lastMove.from.resetCond && lastMove.to.inWater // Into water.
                && excludeStaticSpeed(thisMove) && excludeStaticSpeed(lastMove)
                ;
    }
    /**
     * Fully in-air move.
     * 
     * @param thisMove
     *            Not strictly the latest move in MovingData.
     * @return
     */
    static boolean inAir(final MoveData thisMove) {
        return !thisMove.touchedGround && !thisMove.from.resetCond && !thisMove.to.resetCond;
    }

    /**
     * A liquid -> liquid move. Exclude web and climbable.
     * 
     * @param thisMove
     * @return
     */
    static boolean inLiquid(final MoveData thisMove) {
        return thisMove.from.inLiquid && thisMove.to.inLiquid && excludeStaticSpeed(thisMove);
    }

    /**
     * Test if either point is in reset condition (liquid, web, ladder).
     * 
     * @param thisMove
     * @return
     */
    static boolean resetCond(final MoveData thisMove) {
        return thisMove.from.resetCond || thisMove.to.resetCond;
    }

    /**
     * Moving out of liquid, might move onto ground. Exclude web and climbable.
     * 
     * @param thisMove
     * @return
     */
    static boolean leavingLiquid(final MoveData thisMove) {
        return thisMove.from.inLiquid && !thisMove.to.inLiquid && excludeStaticSpeed(thisMove);
    }

    /**
     * Moving into liquid., might move onto ground. Exclude web and climbable.
     * 
     * @param thisMove
     * @return
     */
    static boolean intoLiquid(final MoveData thisMove) {
        return !thisMove.from.inLiquid && thisMove.to.inLiquid && excludeStaticSpeed(thisMove);
    }

    /**
     * Exclude moving from/to blocks with static (vertical) speed, such as web
     * or climbable.
     * 
     * @param thisMove
     * @return
     */
    static boolean excludeStaticSpeed(final MoveData thisMove) {
        return !thisMove.from.inWeb && !thisMove.to.inWeb
                && !thisMove.from.onClimbable && !thisMove.to.onClimbable;
    }

    /**
     * First move after set-back / teleport. Originally has been found with
     * PaperSpigot for MC 1.7.10, however it also does occur on Spigot for MC
     * 1.7.10.
     * 
     * @param thisMove
     * @param lastMove
     * @param data
     * @return
     */
    public static boolean skipPaper(final MoveData thisMove, final MoveData lastMove, final MovingData data) {
        // TODO: Confine to from at block level (offset 0)?
        final double setBackYDistance = thisMove.to.y - data.getSetBackY();
        return !lastMove.toIsValid && data.sfJumpPhase == 0 && thisMove.mightBeMultipleMoves
                && setBackYDistance > 0.0 && setBackYDistance < PAPER_DIST 
                && thisMove.yDistance > 0.0 && thisMove.yDistance < PAPER_DIST && inAir(thisMove);
    }

    /**
     * Disregarding this move, test if all the way falling after head being
     * obstructed.
     * 
     * @param data
     * @return
     */
    public static boolean fallAfterHeadObstructed(final MovingData data, int limit) {
        limit = Math.min(limit, data.moveData.size());
        for (int i = 0; i < limit; i++) {
            final MoveData move = data.moveData.get(i);
            if (!move.toIsValid || move.yDistance >= 0.0) {
                return false;
            }
            else if (move.headObstructed) {
                return true;
            }
        }
        return false;
    }

}
