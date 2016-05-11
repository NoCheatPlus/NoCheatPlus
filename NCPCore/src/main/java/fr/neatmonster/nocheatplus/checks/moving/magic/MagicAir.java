package fr.neatmonster.nocheatplus.checks.moving.magic;

import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

public class MagicAir {

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
    public static boolean venvHacks(final PlayerLocation from, final PlayerLocation to, final double yDistance, final double yDistChange, 
            final PlayerMoveData thisMove, final PlayerMoveData lastMove, final MovingData data) {
        return 
                // 0: Intended for cobweb.
                // TODO: Bounding box issue ?
                data.liftOffEnvelope == LiftOffEnvelope.NO_JUMP && data.sfJumpPhase < 60
                && (
                        lastMove.toIsValid && lastMove.yDistance < 0.0 
                        && (
                                // 2: Switch to 0 y-Dist on early jump phase.
                                yDistance == 0.0 && lastMove.yDistance < -Magic.GRAVITY_ODD / 3.0 && lastMove.yDistance > -Magic.GRAVITY_MIN
                                && data.ws.use(WRPT.W_M_SF_WEB_0V1)
                                // 2: Decrease too few.
                                || yDistChange < -Magic.GRAVITY_MIN / 3.0 && yDistChange > -Magic.GRAVITY_MAX
                                && data.ws.use(WRPT.W_M_SF_WEB_MICROGRAVITY1)
                                // 2: Keep negative y-distance (very likely a player height issue).
                                || yDistChange == 0.0 && lastMove.yDistance > -Magic.GRAVITY_MAX && lastMove.yDistance < -Magic.GRAVITY_ODD / 3.0
                                && data.ws.use(WRPT.W_M_SF_WEB_MICROGRAVITY2)
                                )
                        // 1: Keep yDist == 0.0 on first falling.
                        // TODO: Do test if hdist == 0.0 or something small can be assumed.
                        || yDistance == 0.0 && data.sfZeroVdistRepeat > 0 && data.sfZeroVdistRepeat < 10 
                        && thisMove.hDistance < 0.125 && lastMove.hDistance < 0.125
                        && to.getY() - data.getSetBackY() < 0.0 && to.getY() - data.getSetBackY() > -2.0 // Quite coarse.
                        && data.ws.use(WRPT.W_M_SF_WEB_0V2)
                        )
                // 0: Jumping on slimes, change viewing direction at the max. height.
                // TODO: Precondition bounced off or touched slime.
                // TODO: Instead of 1.35, relate to min/max jump gain?
                // TODO: Implicitly removed condition: hdist < 0.125
                || yDistance == 0.0 && data.sfZeroVdistRepeat == 1 
                && (data.isVelocityJumpPhase() || data.hasSetBack() && to.getY() - data.getSetBackY() < 1.35 && to.getY() - data.getSetBackY() > 0.0)
                && data.ws.use(WRPT.W_M_SF_SLIME_JP_2X0)
                ;
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
    private static boolean oddSlope(final PlayerLocation to, final double yDistance, final double maxJumpGain, final double yDistDiffEx, final PlayerMoveData lastMove, final MovingData data) {
        return data.sfJumpPhase == 1 //&& data.fromWasReset 
                && Math.abs(yDistDiffEx) < 2.0 * Magic.GRAVITY_SPAN 
                && lastMove.yDistance > 0.0 && yDistance < lastMove.yDistance
                && to.getY() - data.getSetBackY() <= data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier)
                && (
                        // Decrease more after lost-ground cases with more y-distance than normal lift-off.
                        lastMove.yDistance > maxJumpGain && lastMove.yDistance < 1.1 * maxJumpGain 
                        && data.ws.use(WRPT.W_M_SF_SLOPE1)
                        //&& fallingEnvelope(yDistance, lastMove.yDistance, 2.0 * GRAVITY_SPAN)
                        // Decrease more after going through liquid (but normal ground envelope).
                        || lastMove.yDistance > 0.5 * maxJumpGain && lastMove.yDistance < 0.84 * maxJumpGain
                        && lastMove.yDistance - yDistance <= Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN
                        && data.ws.use(WRPT.W_M_SF_SLOPE2)
                        );
    }

    /**
     * Jump after leaving the liquid near ground or jumping through liquid
     * (rather friction envelope, problematic). Needs last move data.
     * 
     * @return If the exemption condition applies.
     */
    private static boolean oddLiquid(final double yDistance, final double yDistDiffEx, final double maxJumpGain, final boolean resetTo, 
            final PlayerMoveData thisMove, final PlayerMoveData lastMove, final MovingData data) {
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
                        // 2: Friction issues (bad).
                        // TODO: Velocity jump phase isn't exact on that account, but shouldn't hurt.
                        // TODO: Liquid-bound or not?
                        (data.liftOffEnvelope != LiftOffEnvelope.NORMAL || data.isVelocityJumpPhase())
                        && (    // 
                                Magic.fallingEnvelope(yDistance, lastMove.yDistance, data.lastFrictionVertical, Magic.GRAVITY_ODD / 2.0)
                                // Moving out of lava with velocity.
                                // TODO: Generalize / fix friction there (max/min!?)
                                || lastMove.from.extraPropertiesValid && lastMove.from.inLava
                                && Magic.enoughFrictionEnvelope(thisMove, lastMove, Magic.FRICTION_MEDIUM_LAVA, 0.0, 2.0 * Magic.GRAVITY_MAX, 4.0)
                                )
                        )
                // 0: Not normal envelope.
                // TODO: Water-bound or not?
                || data.liftOffEnvelope != LiftOffEnvelope.NORMAL
                && (
                        // 1: Jump or decrease falling speed after a small gain (could be bounding box?).
                        yDistDiffEx > 0.0 && yDistance > lastMove.yDistance && yDistance < 0.84 * maxJumpGain
                        && lastMove.yDistance >= -Magic.GRAVITY_MAX - Magic.GRAVITY_MIN && lastMove.yDistance < Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN
                        )
                // 0: Moving out of water somehow.
                || (data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID || data.liftOffEnvelope == LiftOffEnvelope.LIMIT_NEAR_GROUND)
                && (
                        // 1: Too few decrease on first moves out of water (upwards).
                        lastMove.yDistance > 0.0 && yDistance < lastMove.yDistance - Magic.GRAVITY_MAX && yDistDiffEx > 0.0 && yDistDiffEx < Magic.GRAVITY_MAX + Magic.GRAVITY_ODD
                        // 1: Odd decrease of speed as if still in water, moving out of water (downwards).
                        // TODO: data.lastFrictionVertical might not catch it (jump phase 0 -> next = air).
                        // TODO: Could not reproduce since first time (use DebugUtil.debug(String, boolean)).
                        || lastMove.yDistance < -2.0 * Magic.GRAVITY_MAX && data.sfJumpPhase == 1 
                        && yDistance < -Magic.GRAVITY_MAX && yDistance > lastMove.yDistance 
                        && Math.abs(yDistance - lastMove.yDistance * data.lastFrictionVertical) < Magic.GRAVITY_MAX 
                        // 1: Falling too slow, keeping roughly gravity-once speed.
                        || data.sfJumpPhase == 1
                        && lastMove.yDistance < -Magic.GRAVITY_ODD && lastMove.yDistance > -Magic.GRAVITY_MAX - Magic.GRAVITY_MIN 
                        && Math.abs(lastMove.yDistance - yDistance) < Magic.GRAVITY_SPAN 
                        && (yDistance < lastMove.yDistance || yDistance < Magic.GRAVITY_MIN)
                        // 1: Falling slightly too slow.
                        || yDistDiffEx > 0.0 && (
                                // 2: Falling too slow around 0 yDistance.
                                lastMove.yDistance > -2.0 * Magic.GRAVITY_MAX - Magic.GRAVITY_ODD
                                && yDistance < lastMove.yDistance && lastMove.yDistance - yDistance < Magic.GRAVITY_MAX
                                && lastMove.yDistance - yDistance > Magic.GRAVITY_MIN / 4.0
                                // 2: Moving out of liquid with velocity.
                                || yDistance > 0.0 && data.sfJumpPhase == 1 && yDistDiffEx < 4.0 * Magic.GRAVITY_MAX
                                && yDistance < lastMove.yDistance - Magic.GRAVITY_MAX && data.isVelocityJumpPhase()
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
    private static boolean oddGravity(final PlayerLocation from, final PlayerLocation to, 
            final double yDistance, final double yDistChange, final double yDistDiffEx, 
            final PlayerMoveData thisMove, final PlayerMoveData lastMove, final MovingData data) {
        // TODO: Identify spots only to apply with limited LiftOffEnvelope (some guards got removed before switching to that).
        // TODO: Cleanup pending.
        // Old condition (normal lift-off envelope).
        //        yDistance >= -GRAVITY_MAX - GRAVITY_SPAN 
        //        && (yDistChange < -GRAVITY_MIN && Math.abs(yDistChange) <= 2.0 * GRAVITY_MAX + GRAVITY_SPAN
        //        || from.isHeadObstructed(from.getyOnGround()) || data.fromWasReset && from.isHeadObstructed())
        return 
                // 0: Any envelope (supposedly normal) near 0 yDistance.
                yDistance > -2.0 * Magic.GRAVITY_MAX - Magic.GRAVITY_MIN && yDistance < 2.0 * Magic.GRAVITY_MAX + Magic.GRAVITY_MIN
                && (
                        // 1: Too big chunk of change, but within reasonable bounds (should be contained in some other generic case?).
                        lastMove.yDistance < 3.0 * Magic.GRAVITY_MAX + Magic.GRAVITY_MIN && yDistChange < -Magic.GRAVITY_MIN && yDistChange > -2.5 * Magic.GRAVITY_MAX -Magic.GRAVITY_MIN
                        // Transition to 0.0 yDistance, ascending.
                        || lastMove.yDistance > Magic.GRAVITY_ODD / 2.0 && lastMove.yDistance < Magic.GRAVITY_MIN && yDistance == 0.0
                        // 1: yDist inversion near 0 (almost). TODO: This actually happens near liquid, but NORMAL env!?
                        // lastYDist < Gravity max + min happens with dirty phase (slimes),. previously: max + span
                        // TODO: Can all cases be reduced to change sign with max. neg. gain of max + span ?
                        || lastMove.yDistance <= Magic.GRAVITY_MAX + Magic.GRAVITY_MIN && lastMove.yDistance > Magic.GRAVITY_ODD
                        && yDistance < Magic.GRAVITY_ODD && yDistance > -2.0 * Magic.GRAVITY_MAX - Magic.GRAVITY_ODD / 2.0
                        // 1: Head is obstructed. 
                        // TODO: Cover this in a more generic way elsewhere (<= friction envelope + obstructed).
                        || lastMove.yDistance >= 0.0 && yDistance < Magic.GRAVITY_ODD
                        && (thisMove.headObstructed || lastMove.headObstructed)
                        // 1: Break the block underneath.
                        || lastMove.yDistance < 0.0 && lastMove.to.extraPropertiesValid && lastMove.to.onGround
                        && yDistance >= -Magic.GRAVITY_MAX - Magic.GRAVITY_SPAN && yDistance <= Magic.GRAVITY_MIN
                        // 1: Slope with slimes (also near ground without velocityJumpPhase, rather lowjump but not always).
                        || lastMove.yDistance < -Magic.GRAVITY_MAX && yDistChange < - Magic.GRAVITY_ODD / 2.0 && yDistChange > -Magic.GRAVITY_MIN
                        // 1: Near ground (slime block).
                        || lastMove.yDistance == 0.0 && yDistance < -Magic.GRAVITY_ODD / 2.5 && yDistance > -Magic.GRAVITY_MIN && to.isOnGround(Magic.GRAVITY_MIN)
                        // 1: Start to fall after touching ground somehow (possibly too slowly).
                        || (lastMove.touchedGround || lastMove.to.resetCond) && lastMove.yDistance <= Magic.GRAVITY_MIN && lastMove.yDistance >= - Magic.GRAVITY_MAX
                        && yDistance < lastMove.yDistance - Magic.GRAVITY_SPAN && yDistance < Magic.GRAVITY_ODD && yDistance > lastMove.yDistance - Magic.GRAVITY_MAX
                        )
                // 0: With velocity.
                || data.isVelocityJumpPhase()
                && (
                        // 1: Near zero inversion with slimes (rather dirty phase).
                        lastMove.yDistance > Magic.GRAVITY_ODD && lastMove.yDistance < Magic.GRAVITY_MAX + Magic.GRAVITY_MIN
                        && yDistance <= -lastMove.yDistance && yDistance > -lastMove.yDistance - Magic.GRAVITY_MAX - Magic.GRAVITY_ODD
                        // 1: Odd mini-decrease with dirty phase (slime).
                        || lastMove.yDistance < -0.204 && yDistance > -0.26
                        && yDistChange > -Magic.GRAVITY_MIN && yDistChange < -Magic.GRAVITY_ODD / 4.0
                        // 1: Lot's of decrease near zero TODO: merge later.
                        || lastMove.yDistance < -Magic.GRAVITY_ODD && lastMove.yDistance > -Magic.GRAVITY_MIN
                        && yDistance > -2.0 * Magic.GRAVITY_MAX - 2.0 * Magic.GRAVITY_MIN && yDistance < -Magic.GRAVITY_MAX
                        // 1: Odd decrease less near zero.
                        || yDistChange > -Magic.GRAVITY_MIN && yDistChange < -Magic.GRAVITY_ODD 
                        && lastMove.yDistance < 0.5 && lastMove.yDistance > 0.4
                        // 1: Small decrease after high edge.
                        // TODO: Consider min <-> span, generic.
                        || lastMove.yDistance == 0.0 && yDistance > -Magic.GRAVITY_MIN && yDistance < -Magic.GRAVITY_ODD
                        // 1: Too small but decent decrease moving up, marginal violation.
                        || yDistDiffEx > 0.0 && yDistDiffEx < 0.01
                        && yDistance > Magic.GRAVITY_MAX && yDistance < lastMove.yDistance - Magic.GRAVITY_MAX
                        )
                // 0: Small distance to set.back. .
                || data.hasSetBack() && Math.abs(data.getSetBackY() - from.getY()) < 1.0
                // TODO: Consider low fall distance as well.
                && (
                        // 1: Near ground small decrease.
                        lastMove.yDistance > Magic.GRAVITY_MAX && lastMove.yDistance < 3.0 * Magic.GRAVITY_MAX
                        && yDistChange > - Magic.GRAVITY_MIN && yDistChange < -Magic.GRAVITY_ODD
                        // 1: Bounce without velocity set. TODO: wat?
                        //|| lastMove.yDistance == 0.0 && yDistance > -GRAVITY_MIN && yDistance < GRAVITY_SPAN
                        // 1: Bounce with carpet.
                        || yDistance < 0.006
                        )
                // 0: Jump-effect-specific
                // TODO: Jump effect at reduced lift off envelope -> skip this?
                || data.jumpAmplifier > 0 && lastMove.yDistance < Magic.GRAVITY_MAX + Magic.GRAVITY_MIN / 2.0 && lastMove.yDistance > -2.0 * Magic.GRAVITY_MAX - 0.5 * Magic.GRAVITY_MIN
                && yDistance > -2.0 * Magic.GRAVITY_MAX - 2.0 * Magic.GRAVITY_MIN && yDistance < Magic.GRAVITY_MIN
                && yDistChange < -Magic.GRAVITY_SPAN
                // 0: Another near 0 yDistance case.
                // TODO: Inaugurate into some more generic envelope.
                || lastMove.yDistance > -Magic.GRAVITY_MAX && lastMove.yDistance < Magic.GRAVITY_MIN 
                && !(lastMove.touchedGround || lastMove.to.extraPropertiesValid && lastMove.to.onGroundOrResetCond)
                && yDistance < lastMove.yDistance - Magic.GRAVITY_MIN / 2.0 && yDistance > lastMove.yDistance - Magic.GRAVITY_MAX - 0.5 * Magic.GRAVITY_MIN
                // 0: Reduced jumping envelope.
                || data.liftOffEnvelope != LiftOffEnvelope.NORMAL
                && (
                        // 1: Wild-card allow half gravity near 0 yDistance. TODO: Check for removal of included cases elsewhere.
                        lastMove.yDistance > -10.0 * Magic.GRAVITY_ODD / 2.0 && lastMove.yDistance < 10.0 * Magic.GRAVITY_ODD
                        && yDistance < lastMove.yDistance - Magic.GRAVITY_MIN / 2.0 && yDistance > lastMove.yDistance - Magic.GRAVITY_MAX
                        // 1: 
                        || lastMove.yDistance < Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN && lastMove.yDistance > Magic.GRAVITY_ODD
                        && yDistance > 0.4 * Magic.GRAVITY_ODD && yDistance - lastMove.yDistance < -Magic.GRAVITY_ODD / 2.0
                        // 1: 
                        || lastMove.yDistance < 0.2 && lastMove.yDistance >= 0.0 && yDistance > -0.2 && yDistance < 2.0 * Magic.GRAVITY_MAX
                        // 1: 
                        || lastMove.yDistance > 0.4 * Magic.GRAVITY_ODD && lastMove.yDistance < Magic.GRAVITY_MIN && yDistance == 0.0
                        // 1: Too small decrease, right after lift off.
                        || data.sfJumpPhase == 1 && lastMove.yDistance > -Magic.GRAVITY_ODD && lastMove.yDistance <= Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN
                        && yDistance - lastMove.yDistance < 0.0114
                        // 1: Any leaving liquid and keeping distance once.
                        || data.sfJumpPhase == 1 
                        && Math.abs(yDistance) <= Magic.swimBaseSpeedV() && yDistance == lastMove.yDistance
                        )
                ;
    }

    /**
     * Odd behavior with moving up or (slightly) down, not like the ordinary
     * friction mechanics, accounting for more than one past move. Needs
     * lastMove to be valid.
     * 
     * @param yDistance
     * @param lastMove
     * @param data
     * @return
     */
    private static boolean oddFriction(final double yDistance, final double yDistDiffEx, final PlayerMoveData lastMove, final MovingData data) {
        // Use past move data for two moves.
        final PlayerMoveData pastMove1 = data.playerMoves.getSecondPastMove();
        if (!lastMove.to.extraPropertiesValid || !pastMove1.toIsValid || !pastMove1.to.extraPropertiesValid) {
            return false;
        }
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        return 
                // 0: First move into air, moving out of liquid.
                // (These should probably be oddLiquid cases, might pull pastMove1 to vDistAir later.)
                (data.liftOffEnvelope == LiftOffEnvelope.LIMIT_NEAR_GROUND || data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID)
                && data.sfJumpPhase == 1 && Magic.inAir(thisMove)
                && (
                        // 1: Towards ascending rather.
                        pastMove1.yDistance > lastMove.yDistance - Magic.GRAVITY_MAX
                        && lastMove.yDistance > yDistance + Magic.GRAVITY_MAX && lastMove.yDistance > 0.0 // Positive speed. TODO: rather > 1.0 (!).
                        && (
                                // 2: Odd speed decrease bumping into a block sideways somehow, having moved through water.
                                yDistDiffEx < 0.0 && Magic.splashMove(lastMove, pastMove1)
                                && (
                                        // 3: Odd too high decrease, after middle move being within friction envelope.
                                        yDistance > lastMove.yDistance / 5.0
                                        // 3: Two times about the same decrease (e.g. near 1.0), ending up near zero distance.
                                        || yDistance > -Magic.GRAVITY_MAX 
                                        && Math.abs(pastMove1.yDistance - lastMove.yDistance - (lastMove.yDistance - thisMove.yDistance)) < Magic.GRAVITY_MAX
                                        )
                                // 2: Almost keep speed (gravity only), moving out of lava with (high) velocity.
                                // (Needs jump phase == 1, to confine decrease from pastMove1 to lastMove.)
                                // TODO: Never seems to apply.
                                // TODO: Might explicitly demand (lava) friction decrease from pastMove1 to lastMove.
                                || Magic.inLiquid(pastMove1) && Magic.leavingLiquid(lastMove) && lastMove.yDistance > 4.0 * Magic.GRAVITY_MAX
                                // TODO: Store applicable or used friction in MoveData and use enoughFrictionEnvelope?
                                && yDistance < lastMove.yDistance - Magic.GRAVITY_MAX 
                                && yDistance > lastMove.yDistance - 2.0 * Magic.GRAVITY_MAX
                                && Math.abs(lastMove.yDistance - pastMove1.yDistance) > 4.0 * Magic.GRAVITY_MAX

                                )
                        // 1: Less 'strict' speed increase, descending rather.
                        || pastMove1.yDistance < 0.0
                        // Actual speed decrease due to water.
                        && lastMove.yDistance - Magic.GRAVITY_MAX < yDistance && yDistance < 0.7 * lastMove.yDistance
                        && Math.abs(pastMove1.yDistance + lastMove.yDistance) > 2.5
                        // (Actually splashMove or aw-ww-wa-aa)
                        && (Magic.splashMove(lastMove, pastMove1) && pastMove1.yDistance > lastMove.yDistance
                                // Allow more decrease if moving through more solid water.
                                || Magic.inLiquid(pastMove1) && Magic.leavingLiquid(lastMove) && pastMove1.yDistance *.7 > lastMove.yDistance)
                        // 1: Strong decrease after rough keeping speed (hold space bar, with velocity, descending).
                        || yDistance < - 0.5 // Arbitrary, actually observed was around 2.
                        && pastMove1.yDistance < yDistance && lastMove.yDistance < yDistance
                        && Math.abs(pastMove1.yDistance - lastMove.yDistance) < Magic.GRAVITY_ODD
                        && yDistance < lastMove.yDistance * 0.67 && yDistance > lastMove.yDistance * data.lastFrictionVertical - Magic.GRAVITY_MIN
                        && (Magic.splashMoveNonStrict(lastMove, pastMove1) || Magic.inLiquid(pastMove1) && Magic.leavingLiquid(lastMove))
                        )
                // 0: Odd normal envelope set.
                // TODO: Replace special case with splash move in SurvivalFly.check by a new style workaround.
                || data.liftOffEnvelope == LiftOffEnvelope.NORMAL && data.sfJumpPhase == 1 && Magic.inAir(thisMove)
                //                                && data.isVelocityJumpPhase()
                // Velocity very fast into water above.
                && (Magic.splashMoveNonStrict(lastMove, pastMove1) || Magic.inLiquid(pastMove1) && Magic.leavingLiquid(lastMove))
                && yDistance < lastMove.yDistance - Magic.GRAVITY_MAX 
                && yDistance > lastMove.yDistance - 2.0 * Magic.GRAVITY_MAX
                && (Math.abs(lastMove.yDistance - pastMove1.yDistance) > 4.0 * Magic.GRAVITY_MAX
                        || pastMove1.yDistance > 3.0 && lastMove.yDistance > 3.0 && Math.abs(lastMove.yDistance - pastMove1.yDistance) < 2.0 * Magic.GRAVITY_MAX)
                ;
    }

    /**
     * Odd behavior with/after wearing elytra. End rod is not in either hand,
     * elytra is equipped (not checked in here).
     * 
     * @param yDistance
     * @param yDistDiffEx
     * @param lastMove
     * @param data
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean oddElytra(final double yDistance, final double yDistDiffEx, final PlayerMoveData lastMove, final MovingData data) {
        // Organize cases differently here, at the cost of reaching some nesting level, in order to see if it's better to overview.
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final PlayerMoveData pastMove1 = data.playerMoves.getSecondPastMove(); // Checked below, if needed.
        // Both descending moves.
        if (thisMove.yDistance < 0.0 && lastMove.yDistance < 0.0) {
            // Falling too slow.
            if (yDistDiffEx > 0.0) {
                final double yDistChange = thisMove.yDistance - lastMove.yDistance;
                // Increase falling speed somehow.
                if (yDistChange < 0.0) {
                    // pastMove1 valid, decreasing speed envelope like above.
                    if (pastMove1.toIsValid && pastMove1.yDistance < 0.0) {
                        final double lastYDistChange = lastMove.yDistance - pastMove1.yDistance;
                        // Increase falling speed from past to last.
                        if (lastYDistChange < 0.0) {
                            // Relate sum of decrease to gravity somehow. 
                            // TODO: Inaugurate by the one below?
                            if (Math.abs(yDistChange + lastYDistChange) > Magic.GRAVITY_ODD / 2.0) {
                                // TODO: Might further test for a workaround count down or relate to total gain / jump phase.
                                return true;
                            }
                        }
                    }
                }
                // Independently of increasing/decreasing.
                // Gliding possibly.
                if (Magic.glideVerticalGainEnvelope(thisMove.yDistance, lastMove.yDistance)) {
                    // Restrict to early falling, or higher speeds.
                    // (Further restrictions hardly grip, observed: jump phase > 40, yDistance at -0.214)
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Several types of odd in-air moves, mostly with gravity near maximum,
     * friction, medium change. Needs lastMove.toIsValid.
     * 
     * @param from
     * @param to
     * @param yDistance
     * @param yDistChange
     * @param yDistDiffEx
     * @param maxJumpGain
     * @param resetTo
     * @param lastMove
     * @param data
     * @param cc
     * @return true if a workaround applies.
     */
    public static boolean oddJunction(final PlayerLocation from, final PlayerLocation to,
            final double yDistance, final double yDistChange, final double yDistDiffEx, 
            final double maxJumpGain, final boolean resetTo,
            final PlayerMoveData thisMove, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc) {
        // TODO: Cleanup/reduce signature (accept thisMove.yDistance etc.).
        if (MagicAir.oddLiquid(yDistance, yDistDiffEx, maxJumpGain, resetTo, thisMove, lastMove, data)) {
            // Jump after leaving the liquid near ground.
            return true;
        }
        if (MagicAir.oddGravity(from, to, yDistance, yDistChange, yDistDiffEx, thisMove, lastMove, data)) {
            // Starting to fall / gravity effects.
            return true;
        }
        if ((yDistDiffEx > 0.0 || yDistance >= 0.0) && MagicAir.oddSlope(to, yDistance, maxJumpGain, yDistDiffEx, lastMove, data)) {
            // Odd decrease after lift-off.
            return true;
        }
        if (MagicAir.oddFriction(yDistance, yDistDiffEx, lastMove, data)) {
            // Odd behavior with moving up or (slightly) down, accounting for more than one past move.
            return true;
        }
        //        if (Bridge1_9.isWearingElytra(from.getPlayer()) && MagicAir.oddElytra(yDistance, yDistDiffEx, lastMove, data)) {
        //            // Odd behavior with/after wearing elytra.
        //            return true;
        //        }
        return false;
    }

}
