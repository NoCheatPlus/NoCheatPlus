package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

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
     * The absolute per-tick base speed for swimming vertically.
     * 
     * @return
     */
    static double swimBaseSpeedV() {
        // TODO: Does this have to be the dynamic walk speed (refactoring)?
        return WALK_SPEED * modSwim + 0.02;
    }

    /**
     * Vertical envelope "hacks". Directly check for certain transitions, on
     * match, skip sub-checks: vdistrel, maxphase, inAirChecks.
     * 
     * @param from
     * @param to
     * @param yDistance
     * @param data
     * @return If to skip those sub-checks.
     */
    static boolean venvHacks(final PlayerLocation from, final PlayerLocation to, final double yDistance, final double yDistChange, final MoveData lastMove, final MovingData data) {
        return 
                // 0: Intended for cobweb.
                // TODO: Bounding box issue ?
                data.liftOffEnvelope == LiftOffEnvelope.NO_JUMP && data.sfJumpPhase < 60
                && (
                        lastMove.toIsValid && lastMove.yDistance < 0.0 
                        && (
                                // 2: Switch to 0 y-Dist on early jump phase.
                                yDistance == 0.0 && lastMove.yDistance < -GRAVITY_ODD / 3.0 && lastMove.yDistance > -GRAVITY_MIN
                                // 2: Decrease too few.
                                || yDistChange < -GRAVITY_MIN / 3.0 && yDistChange > -GRAVITY_MAX
                                // 2: Keep negative y-distance (very likely a player height issue).
                                || yDistChange == 0.0 && lastMove.yDistance > -GRAVITY_MAX && lastMove.yDistance < -GRAVITY_ODD / 3.0
                                )
                                // 1: Keep yDist == 0.0 on first falling.
                                // TODO: Do test if hdist == 0.0 or something small can be assumed.
                                || yDistance == 0.0 && data.sfZeroVdist > 0 && data.sfZeroVdist < 10
                        )
                        // 0: Jumping on slimes, change viewing direction at the max. height.
                        || yDistance == 0.0 && data.sfZeroVdist == 1 
                        && (data.isVelocityJumpPhase() || data.hasSetBack() && to.getY() - data.getSetBackY() < 1.35);
    }

    /**
     * Odd decrease after lift-off.
     * @param to
     * @param yDistance
     * @param maxJumpGain
     * @param yDistDiffEx
     * @param data
     * @return
     */
    static boolean oddSlope(final PlayerLocation to, final double yDistance, final double maxJumpGain, final double yDistDiffEx, final MoveData lastMove, final MovingData data) {
        return data.sfJumpPhase == 1 //&& data.fromWasReset 
                && Math.abs(yDistDiffEx) < 2.0 * GRAVITY_SPAN 
                && lastMove.yDistance > 0.0 && yDistance < lastMove.yDistance
                && to.getY() - data.getSetBackY() <= data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier)
                && (
                        // Decrease more after lost-ground cases with more y-distance than normal lift-off.
                        lastMove.yDistance > maxJumpGain && lastMove.yDistance < 1.1 * maxJumpGain 
                        //&& fallingEnvelope(yDistance, lastMove.yDistance, 2.0 * GRAVITY_SPAN)
                        // Decrease more after going through liquid (but normal ground envelope).
                        || lastMove.yDistance > 0.5 * maxJumpGain && lastMove.yDistance < 0.84 * maxJumpGain
                        && lastMove.yDistance - yDistance <= GRAVITY_MAX + GRAVITY_SPAN  
                        );
    }

    /**
     * Test if the player is (well) within in-air falling envelope.
     * @param yDistance
     * @param lastYDist
     * @param extraGravity Extra amount to fall faster.
     * @return
     */
    static boolean fallingEnvelope(final double yDistance, final double lastYDist, final double lastFrictionVertical, final double extraGravity) {
        if (yDistance >= lastYDist) {
            return false;
        }
        // TODO: data.lastFrictionVertical (see vDistAir).
        final double frictDist = lastYDist * lastFrictionVertical - GRAVITY_MIN;
        // TODO: Extra amount: distinguish pos/neg?
        return yDistance <= frictDist + extraGravity && yDistance > frictDist - GRAVITY_SPAN - extraGravity;
    }

    /**
     * Jump after leaving the liquid near ground or jumping through liquid
     * (rather friction envelope, problematic). Needs last move data.
     * 
     * @return If the exemption condition applies.
     */
    static boolean oddLiquid(final double yDistance, final double yDistDiffEx, final double maxJumpGain, final boolean resetTo, final MoveData lastMove, final MovingData data) {
        // TODO: Relate jump phase to last/second-last move fromWasReset (needs keeping that data in classes).
        // TODO: And distinguish where JP=2 is ok?
        // TODO: Most are medium transitions with the possibility to keep/alter friction or even speed on 1st/2nd move (counting in the transition).
        // TODO: Do any belong into odd gravity? (Needs re-grouping EVERYTHING anyway.)
        if (data.sfJumpPhase != 1 && data.sfJumpPhase != 2) {
            return false;
        }
        return 
                // 0: Falling slightly too fast (velocity/special).
                yDistDiffEx < 0.0 && (
                        // 2: Friction issue (bad).
                        // TODO: Velocity jump phase isn't exact on that account, but shouldn't hurt.
                        // TODO: Water-bound or not?
                        (data.liftOffEnvelope != LiftOffEnvelope.NORMAL || data.isVelocityJumpPhase())
                        && fallingEnvelope(yDistance, lastMove.yDistance, data.lastFrictionVertical, GRAVITY_ODD / 2.0)
                        )
                        // 0: Not normal envelope.
                        // TODO: Water-bound or not?
                        || data.liftOffEnvelope != LiftOffEnvelope.NORMAL
                        && (
                                // 1: Jump or decrease falling speed after a small gain (could be bounding box?).
                                yDistDiffEx > 0.0 && yDistance > lastMove.yDistance && yDistance < 0.84 * maxJumpGain
                                && lastMove.yDistance >= -GRAVITY_MAX - GRAVITY_MIN && lastMove.yDistance < GRAVITY_MAX + GRAVITY_SPAN
                                )
                                // 0: Moving out of water somehow.
                                || (data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID || data.liftOffEnvelope == LiftOffEnvelope.LIMIT_NEAR_GROUND)
                                && (
                                        // 1: Too few decrease on first moves out of water (upwards).
                                        lastMove.yDistance > 0.0 && yDistance < lastMove.yDistance - GRAVITY_MAX && yDistDiffEx > 0.0 && yDistDiffEx < GRAVITY_MAX + GRAVITY_ODD
                                        // 1: Odd decrease of speed as if still in water, moving out of water (downwards).
                                        // TODO: data.lastFrictionVertical might not catch it (jump phase 0 -> next = air).
                                        // TODO: Could not reproduce since first time (use DebugUtil.debug(String, boolean)).
                                        || lastMove.yDistance < -2.0 * GRAVITY_MAX && data.sfJumpPhase == 1 
                                        && yDistance < -GRAVITY_MAX && yDistance > lastMove.yDistance 
                                        && Math.abs(yDistance - lastMove.yDistance * data.lastFrictionVertical) < GRAVITY_MAX 
                                        // 1: Falling too slow, keeping roughly gravity-once speed.
                                        || data.sfJumpPhase == 1
                                        && lastMove.yDistance < -GRAVITY_ODD && lastMove.yDistance > -GRAVITY_MAX - GRAVITY_MIN 
                                        && Math.abs(lastMove.yDistance - yDistance) < GRAVITY_SPAN 
                                        && (yDistance < lastMove.yDistance || yDistance < GRAVITY_MIN)
                                        // 1: Falling slightly too slow.
                                        || yDistDiffEx > 0.0 && (
                                                // 2: Falling too slow around 0 yDistance.
                                                lastMove.yDistance > -2.0 * GRAVITY_MAX - GRAVITY_ODD
                                                && yDistance < lastMove.yDistance && lastMove.yDistance - yDistance < GRAVITY_MAX
                                                && lastMove.yDistance - yDistance > GRAVITY_MIN / 4.0
                                                // 2: Moving out of liquid with velocity.
                                                || yDistance > 0.0 && data.sfJumpPhase == 1 && yDistDiffEx < 4.0 * GRAVITY_MAX
                                                && yDistance < lastMove.yDistance - GRAVITY_MAX && data.isVelocityJumpPhase()
                                                )
                                        )
                                        ; // (return)
    }

    /**
     * A condition for exemption from vdistrel (vDistAir), around where gravity
     * hits most hard, including head obstruction. This method is called with
     * varying preconditions, thus a full envelope check is necessary. Needs
     * last move data.
     * 
     * @param yDistance
     * @param yDistChange
     * @param data
     * @return If the condition applies, i.e. if to exempt.
     */
    static boolean oddGravity(final PlayerLocation from, final PlayerLocation to, final double yDistance, final double yDistChange, final double yDistDiffEx, final MoveData lastMove, final MovingData data) {
        // TODO: Identify spots only to apply with limited LiftOffEnvelope (some guards got removed before switching to that).
        // TODO: Cleanup pending.
        // Old condition (normal lift-off envelope).
        //        yDistance >= -GRAVITY_MAX - GRAVITY_SPAN 
        //        && (yDistChange < -GRAVITY_MIN && Math.abs(yDistChange) <= 2.0 * GRAVITY_MAX + GRAVITY_SPAN
        //        || from.isHeadObstructed(from.getyOnGround()) || data.fromWasReset && from.isHeadObstructed())
        return 
                // 0: Any envelope (supposedly normal) near 0 yDistance.
                yDistance > -2.0 * GRAVITY_MAX - GRAVITY_MIN && yDistance < 2.0 * GRAVITY_MAX + GRAVITY_MIN
                && (
                        // 1: Too big chunk of change, but within reasonable bounds (should be contained in some other generic case?).
                        lastMove.yDistance < 3.0 * GRAVITY_MAX + GRAVITY_MIN && yDistChange < -GRAVITY_MIN && yDistChange > -2.5 * GRAVITY_MAX -GRAVITY_MIN
                        // Transition to 0.0 yDistance.
                        || lastMove.yDistance > GRAVITY_ODD / 2.0 && lastMove.yDistance < GRAVITY_MIN && yDistance == 0.0
                        // 1: yDist inversion near 0 (almost). TODO: This actually happens near liquid, but NORMAL env!?
                        // lastYDist < Gravity max + min happens with dirty phase (slimes),. previously: max + span
                        // TODO: Can all cases be reduced to change sign with max. neg. gain of max + span ?
                        || lastMove.yDistance <= GRAVITY_MAX + GRAVITY_MIN && lastMove.yDistance > GRAVITY_ODD
                        && yDistance < GRAVITY_ODD && yDistance > -2.0 * GRAVITY_MAX - GRAVITY_ODD / 2.0
                        // 1: Head is obstructed. 
                        // TODO: Cover this in a more generic way elsewhere (<= friction envelope + obstructed).
                        || lastMove.yDistance >= 0.0 && yDistance < GRAVITY_ODD
                        && (data.thisMove.headObstructed || lastMove.headObstructed)
                        // 1: Break the block underneath.
                        || lastMove.yDistance < 0.0 && lastMove.to.extraPropertiesValid && lastMove.to.onGround
                        && yDistance >= -GRAVITY_MAX - GRAVITY_SPAN && yDistance <= GRAVITY_MIN
                        // 1: Slope with slimes (also near ground without velocityJumpPhase, rather lowjump but not always).
                        || lastMove.yDistance < -GRAVITY_MAX && yDistChange < - GRAVITY_ODD / 2.0 && yDistChange > -GRAVITY_MIN
                        // 1: Near ground (slime block).
                        || lastMove.yDistance == 0.0 && yDistance < -GRAVITY_ODD / 2.5 && yDistance > -GRAVITY_MIN && to.isOnGround(GRAVITY_MIN)
                        // 1: Start to fall after touching ground somehow (possibly too slowly).
                        || (lastMove.touchedGround || lastMove.to.resetCond) && lastMove.yDistance <= GRAVITY_MIN && lastMove.yDistance >= - GRAVITY_MAX
                        && yDistance < lastMove.yDistance - GRAVITY_SPAN && yDistance < GRAVITY_ODD && yDistance > lastMove.yDistance - GRAVITY_MAX
                        )
                        // 0: With velocity.
                        || data.isVelocityJumpPhase()
                        && (
                                // 1: Near zero inversion with slimes (rather dirty phase).
                                lastMove.yDistance > GRAVITY_ODD && lastMove.yDistance < GRAVITY_MAX + GRAVITY_MIN
                                && yDistance <= -lastMove.yDistance && yDistance > -lastMove.yDistance - GRAVITY_MAX - GRAVITY_ODD
                                // 1: Odd mini-decrease with dirty phase (slime).
                                || lastMove.yDistance < -0.204 && yDistance > -0.26
                                && yDistChange > -GRAVITY_MIN && yDistChange < -GRAVITY_ODD / 4.0
                                // 1: Lot's of decrease near zero TODO: merge later.
                                || lastMove.yDistance < -GRAVITY_ODD && lastMove.yDistance > -GRAVITY_MIN
                                && yDistance > -2.0 * GRAVITY_MAX - 2.0 * GRAVITY_MIN && yDistance < -GRAVITY_MAX
                                // 1: Odd decrease less near zero.
                                || yDistChange > -GRAVITY_MIN && yDistChange < -GRAVITY_ODD 
                                && lastMove.yDistance < 0.5 && lastMove.yDistance > 0.4
                                // 1: Small decrease after high edge.
                                // TODO: Consider min <-> span, generic.
                                || lastMove.yDistance == 0.0 && yDistance > -GRAVITY_MIN && yDistance < -GRAVITY_ODD
                                // 1: Too small but decent decrease moving up, marginal violation.
                                || yDistDiffEx > 0.0 && yDistDiffEx < 0.01
                                && yDistance > GRAVITY_MAX && yDistance < lastMove.yDistance - GRAVITY_MAX
                                )
                                // 0: Small distance to set.back. .
                                || data.hasSetBack() && Math.abs(data.getSetBackY() - from.getY()) < 1.0
                                // TODO: Consider low fall distance as well.
                                && (
                                        // 1: Near ground small decrease.
                                        lastMove.yDistance > GRAVITY_MAX && lastMove.yDistance < 3.0 * GRAVITY_MAX
                                        && yDistChange > - GRAVITY_MIN && yDistChange < -GRAVITY_ODD
                                        // 1: Bounce without velocity set.
                                        //|| lastMove.yDistance == 0.0 && yDistance > -GRAVITY_MIN && yDistance < GRAVITY_SPAN
                                        )
                                        // 0: Jump-effect-specific
                                        // TODO: Jump effect at reduced lift off envelope -> skip this?
                                        || data.jumpAmplifier > 0 && lastMove.yDistance < GRAVITY_MAX + GRAVITY_MIN / 2.0 && lastMove.yDistance > -2.0 * GRAVITY_MAX - 0.5 * GRAVITY_MIN
                                        && yDistance > -2.0 * GRAVITY_MAX - 2.0 * GRAVITY_MIN && yDistance < GRAVITY_MIN
                                        && yDistChange < -GRAVITY_SPAN
                                        // 0: Another near 0 yDistance case.
                                        // TODO: Inaugurate into some more generic envelope.
                                        || lastMove.yDistance > -GRAVITY_MAX && lastMove.yDistance < GRAVITY_MIN 
                                        && !(lastMove.touchedGround || lastMove.to.extraPropertiesValid && lastMove.to.onGroundOrResetCond)
                                        && yDistance < lastMove.yDistance - GRAVITY_MIN / 2.0 && yDistance > lastMove.yDistance - GRAVITY_MAX - 0.5 * GRAVITY_MIN
                                        // 0: Reduced jumping envelope.
                                        || data.liftOffEnvelope != LiftOffEnvelope.NORMAL
                                        && (
                                                // 1: Wild-card allow half gravity near 0 yDistance. TODO: Check for removal of included cases elsewhere.
                                                lastMove.yDistance > -10.0 * GRAVITY_ODD / 2.0 && lastMove.yDistance < 10.0 * GRAVITY_ODD
                                                && yDistance < lastMove.yDistance - GRAVITY_MIN / 2.0 && yDistance > lastMove.yDistance - GRAVITY_MAX
                                                // 1: 
                                                || lastMove.yDistance < GRAVITY_MAX + GRAVITY_SPAN && lastMove.yDistance > GRAVITY_ODD
                                                && yDistance > 0.4 * GRAVITY_ODD && yDistance - lastMove.yDistance < -GRAVITY_ODD / 2.0
                                                // 1: 
                                                || lastMove.yDistance < 0.2 && lastMove.yDistance >= 0.0 && yDistance > -0.2 && yDistance < 2.0 * GRAVITY_MAX
                                                // 1: 
                                                || lastMove.yDistance > 0.4 * GRAVITY_ODD && lastMove.yDistance < GRAVITY_MIN && yDistance == 0.0
                                                // 1: Too small decrease, right after lift off.
                                                || data.sfJumpPhase == 1 && lastMove.yDistance > -GRAVITY_ODD && lastMove.yDistance <= GRAVITY_MAX + GRAVITY_SPAN
                                                && yDistance - lastMove.yDistance < 0.0114
                                                // 1: Any leaving liquid and keeping distance once.
                                                || data.sfJumpPhase == 1 
                                                && Math.abs(yDistance) <= Magic.swimBaseSpeedV() && yDistance == lastMove.yDistance
                                                )
                                                ;
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
    private static boolean splashMove(final MoveData thisMove, final MoveData lastMove) {
        // Use past move data for two moves.
        return !thisMove.touchedGround && thisMove.from.inWater && !thisMove.to.resetCond // Out of water.
                && !lastMove.touchedGround && !lastMove.from.resetCond && lastMove.to.inWater // Into water.
                ;
    }

    /**
     * Fully in-air move.
     * @param thisMove
     *            Not strictly the latest move in MovingData.
     * @return
     */
    private static boolean inAir(final MoveData thisMove) {
        return !thisMove.touchedGround && !thisMove.from.resetCond && !thisMove.to.resetCond;
    }

    /**
     * Odd behavior with moving up or (slightly) down, not like the ordinary
     * friction mechanics, accounting for more than one past move. Only for too
     * high decrease.
     * 
     * @param yDistance
     * @param lastMove
     * @param data
     * @return
     */
    static boolean oddFriction(final double yDistance, final MoveData lastMove, final MovingData data) {
        // Use past move data for two moves.
        final MoveData pastMove1 = data.moveData.get(1);
        if (!lastMove.to.extraPropertiesValid || !pastMove1.toIsValid || !pastMove1.to.extraPropertiesValid) {
            return false;
        }
        final MoveData thisMove = data.thisMove;
        return 
                // 0: Odd speed decrease bumping into a block sideways somehow, having moved through water.
                // (These should probably be oddLiquid cases, might pull pastMove1 to vDistAir later.)
                data.sfJumpPhase == 1 
                && (data.liftOffEnvelope == LiftOffEnvelope.LIMIT_NEAR_GROUND || data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID)
                && inAir(thisMove) && splashMove(lastMove, pastMove1)
                && pastMove1.yDistance > lastMove.yDistance - GRAVITY_MAX // Some speed decrease.
                && lastMove.yDistance > yDistance + GRAVITY_MAX && lastMove.yDistance > 0.0 // Positive speed. TODO: rather > 1.0 (!).
                && (
                        // 1: Odd too high decrease, after middle move being within friction envelope.
                        yDistance > lastMove.yDistance / 5.0
                        // 1: Two times about the same decrease (e.g. near 1.0), ending up near zero distance.
                        || yDistance > -GRAVITY_MAX 
                        && Math.abs(pastMove1.yDistance - lastMove.yDistance - (lastMove.yDistance - thisMove.yDistance)) < GRAVITY_MAX
                        )
                        ;
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
    static boolean skipPaper(final MoveData thisMove, final MoveData lastMove, final MovingData data) {
        // TODO: Confine to from at block level (offset 0)?
        final double setBackYDistance = thisMove.to.y - data.getSetBackY();
        return !lastMove.toIsValid && data.sfJumpPhase == 0 && thisMove.mightBeMultipleMoves
                && setBackYDistance > 0.0 && setBackYDistance < PAPER_DIST 
                && thisMove.yDistance > 0.0 && thisMove.yDistance < PAPER_DIST && inAir(thisMove);
    }

}
