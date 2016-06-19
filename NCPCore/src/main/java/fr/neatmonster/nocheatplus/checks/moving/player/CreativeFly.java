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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.magic.LostGround;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.ModelFlying;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * A check designed for people that are allowed to fly. The complement to the "SurvivalFly", which is for people that
 * aren't allowed to fly, and therefore have tighter rules to obey.
 */
public class CreativeFly extends Check {

    private final List<String> tags = new LinkedList<String>();

    /**
     * Instantiates a new creative fly check.
     */
    public CreativeFly() {
        super(CheckType.MOVING_CREATIVEFLY);
    }

    /**
     * 
     * @param player
     * @param from
     * @param to
     * @param data
     * @param cc
     * @param time Milliseconds.
     * @return
     */
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc, final long time) {

        // Reset tags, just in case.
        tags.clear();

        // Some edge data for this move.
        final GameMode gameMode = player.getGameMode();
        final ModelFlying model = cc.getModelFlying(player, from);
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        //        if (!data.thisMove.from.extraPropertiesValid) {
        //            // TODO: Confine by model config flag or just always do [if the latter: do it in the listener]?
        //            data.thisMove.setExtraProperties(from, to);
        //        }
        thisMove.modelFlying = model;
        final PlayerMoveData lastMove = data.playerMoves.getFirstPastMove();

        // Calculate some distances.
        final double yDistance = thisMove.yDistance;
        final double hDistance = thisMove.hDistance;

        final boolean flying = gameMode == BridgeMisc.GAME_MODE_SPECTATOR || player.isFlying();
        final boolean sprinting = time <= data.timeSprinting + cc.sprintingGrace;

        // Lost ground, if set so.
        if (model.ground) {
            MovingUtil.prepareFullCheck(from, to, thisMove, Math.max(cc.yOnGround, cc.noFallyOnGround));
            if (!thisMove.from.onGroundOrResetCond) {
                if (from.isSamePos(to)) {
                    if (lastMove.toIsValid && lastMove.hDistance > 0.0 && lastMove.yDistance < -0.3 // Copy and paste from sf.
                            && LostGround.lostGroundStill(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags)) {
                        // Nothing to do.
                    }
                }
                else if (LostGround.lostGround(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags)) {
                    // Nothing to do.
                }
            }
        }

        // Horizontal distance check.
        double[] resH = hDist(player, from, to, hDistance, yDistance, sprinting, flying, lastMove, time, model, data, cc);
        double limitH = resH[0];
        double resultH = resH[1];

        // Check velocity.
        if (resultH > 0) {
            double hFreedom = data.getHorizontalFreedom();
            if (hFreedom < resultH) {
                // Use queued velocity if possible.
                hFreedom += data.useHorizontalVelocity(resultH - hFreedom);
            }
            if (hFreedom > 0.0) {
                resultH = Math.max(0.0, resultH - hFreedom);
                if (resultH <= 0.0) {
                    limitH = hDistance;
                }
                tags.add("hvel");
            }
        }
        else {
            data.clearActiveHorVel(); // TODO: test/check !
        }

        resultH *= 100.0; // Normalize to % of a block.
        if (resultH > 0.0) {
            tags.add("hdist");
        }

        // Vertical move.
        double limitV = 0.0; // Limit.
        double resultV = 0.0; // Violation (normalized to 100 * 1 block, applies if > 0.0).

        // Distinguish checking method by y-direction of the move.
        if (yDistance > 0.0) {
            // Ascend.
            double[] res = vDistAscend(from, to, yDistance, flying, thisMove, lastMove, model, data, cc);
            resultV = Math.max(resultV, res[1]);
            limitV = res[0];
        }
        else if (yDistance < 0.0) {
            // Descend.
            double[] res = vDistDescend(from, to, yDistance, flying, lastMove, model, data, cc);
            resultV = Math.max(resultV, res[1]);
            limitV = res[0];
        }
        else {
            // Keep altitude.
            double[] res = vDistZero(from, to, yDistance, flying, lastMove, model, data, cc);
            resultV = Math.max(resultV, res[1]);
            limitV = res[0];
        }

        // Velocity.
        if (resultV > 0.0 && data.getOrUseVerticalVelocity(yDistance) != null) {
            resultV = 0.0;
            tags.add("vvel");
        }

        // Add tag for maximum height check (silent set back).
        final double maximumHeight = model.maxHeight + player.getWorld().getMaxHeight();
        if (to.getY() > maximumHeight) {
            // TODO: Allow use velocity there (would need a flag to signal the actual check below)?
            tags.add("maxheight");
        }

        resultV *= 100.0; // Normalize to % of a block.
        if (resultV > 0.0) {
            tags.add("vdist");
        }

        final double result = Math.max(0.0, resultH) + Math.max(0.0, resultV);

        if (data.debug) {
            outpuDebugMove(player, hDistance, limitH, yDistance, limitV, model, tags, data);
        }

        // Violation handling.
        Location setBack = null; // Might get altered below.
        if (result > 0.0) {
            // Increment violation level.
            data.creativeFlyVL += result;

            // Execute whatever actions are associated with this check and the violation level and find out if we
            // should cancel the event.
            final ViolationData vd = new ViolationData(this, player, data.creativeFlyVL, result, cc.creativeFlyActions);
            if (vd.needsParameters()) {
                vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
                vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
                vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", TrigUtil.distance(from,  to)));
                if (!tags.isEmpty()) {
                    vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
                }
            }
            if (executeActions(vd).willCancel()) {
                // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()"
                // to allow the player to look somewhere else despite getting pulled back by NoCheatPlus.
                setBack = data.getSetBack(to);
            }
        }
        else {
            // Maximum height check (silent set back).
            if (to.getY() > maximumHeight) {
                setBack = data.getSetBack(to);
            }
            if (setBack == null) {
                // Slowly reduce the violation level with each event.
                data.creativeFlyVL *= 0.97;
            }
        }

        // Return setBack, if set.
        if (setBack != null) {
            // Check for max height of the set-back.
            if (setBack.getY() > maximumHeight) {
                // Correct the y position.
                setBack.setY(getCorrectedHeight(maximumHeight, setBack.getWorld()));
                if (data.debug) {
                    debug(player, "Maximum height exceeded by set-back, correct to: " + setBack.getY());
                }
            }
            data.sfJumpPhase = 0;
            return setBack;
        }
        else {
            // Adjust the set-back and other last distances.
            data.setSetBack(to);
            // Adjust jump phase.
            if (!thisMove.from.onGroundOrResetCond && !thisMove.to.onGroundOrResetCond) {
                data.sfJumpPhase ++;
            }
            else if (thisMove.touchedGround && !thisMove.to.onGroundOrResetCond) {
                data.sfJumpPhase = 1;
            }
            else {
                data.sfJumpPhase = 0;
            }
            return null;
        }
    }

    /**
     * 
     * @param player
     * @param from
     * @param to
     * @param hDistance
     * @param yDistance
     * @param flying
     * @param lastMove
     * @param time
     * @param model
     * @param data
     * @param cc
     * @return limitH, resultH (not normalized).
     */
    private double[] hDist(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final boolean flying, final PlayerMoveData lastMove, final long time, final ModelFlying model, final MovingData data, final MovingConfig cc) {
        // Modifiers.
        double fSpeed;

        // TODO: Make this configurable ! [Speed effect should not affect flying if not on ground.]
        if (model.applyModifiers) {
            final double speedModifier = mcAccess.getHandle().getFasterMovementAmplifier(player);
            if (speedModifier == Double.NEGATIVE_INFINITY) {
                fSpeed = 1.0;
            }
            else {
                fSpeed = 1.0 + 0.2 * (speedModifier + 1.0);
            }
            if (flying) {
                // TODO: Consider mechanics for flying backwards.
                fSpeed *= data.flySpeed / 0.1;
                if (sprinting) {
                    // TODO: Prevent for pre-1.8?
                    fSpeed *= model.hModSprint;
                    tags.add("sprint");
                }
                tags.add("flying");
            }
            else {
                // (Ignore sprinting here).
                fSpeed *= data.walkSpeed / 0.2;
            }
        }
        else {
            fSpeed = 1.0;
        }

        double limitH = model.hModSpeed / 100.0 * ModelFlying.HORIZONTAL_SPEED * fSpeed;

        if (lastMove.toIsValid) {
            // TODO: Use last friction (as well)?
            // TODO: Test/adjust more.
            double frictionDist = lastMove.hDistance * Magic.FRICTION_MEDIUM_AIR;
            limitH = Math.max(frictionDist, limitH);
            tags.add("hfrict");
        }

        // Finally, determine how far the player went beyond the set limits.
        //        double resultH = Math.max(0.0.0, hDistance - data.horizontalFreedom - limitH);
        double resultH = Math.max(0.0, hDistance - limitH);

        if (model.applyModifiers) {
            data.bunnyhopDelay--;
            if (!flying && resultH > 0 && sprinting) {
                // TODO: Flying and bunnyhop ? <- 8 blocks per second - could be a case.
                // Try to treat it as a the "bunnyhop" problem. The bunnyhop problem is that landing and immediately jumping
                // again leads to a player moving almost twice as far in that step.
                // TODO: Real modeling for that kind of moving pattern (same with sf?).
                if (data.bunnyhopDelay <= 0 && resultH < 0.4) {
                    data.bunnyhopDelay = 9;
                    resultH = 0.0;
                    tags.add("bunnyhop");
                }
            }
        }
        return new double[] {limitH, resultH};
    }


    /**
     * 
     * @param from
     * @param to
     * @param yDistance
     * @param flying
     * @param lastMove
     * @param model
     * @param data
     * @param cc
     * @return limitV, resultV (not normalized).
     */
    private double[] vDistAscend(final PlayerLocation from, final PlayerLocation to, final double yDistance, final boolean flying, final PlayerMoveData thisMove, final PlayerMoveData lastMove, final ModelFlying model, final MovingData data, final MovingConfig cc) {
        double limitV = model.vModAscendSpeed / 100.0 * ModelFlying.VERTICAL_ASCEND_SPEED; // * data.jumpAmplifier;
        double resultV = 0.0;
        if (model.applyModifiers && flying && yDistance > 0.0) {
            // Let fly speed apply with moving upwards.
            limitV *= data.flySpeed / 0.1;
        }
        else if (model.isScaleLevitationEffect() && Bridge1_9.hasLevitation()) {
            // Exclude modifiers for now.
            final double levitation = Bridge1_9.getLevitationAmplifier(from.getPlayer());
            if (levitation > 0.0) {
                // (Double checked.)
                // TODO: Perhaps do with a modifier instead, to avoid confusion.
                limitV += 0.046 * levitation; // (It ends up like 0.5 added extra for some levels of levitation, roughly.)
                tags.add("levitation:" + levitation);
            }
        }

        // TODO: Hack, move / config / something.
        // TODO: Confine more. hdist change relates to ydist change
        if (limitV == 0.0 && Bridge1_9.isGlidingWithElytra(from.getPlayer())) {
            limitV = hackLytra(yDistance, limitV, thisMove, lastMove, data);
        }

        if (model.gravity) {
            // Friction with gravity.
            if (yDistance > limitV && model.gravity && lastMove.toIsValid) { // TODO: gravity/friction?
                // (Disregard gravity.)
                // TODO: Use last friction (as well)?
                double frictionDist = lastMove.yDistance * Magic.FRICTION_MEDIUM_AIR;
                if (!flying) {
                    frictionDist -= Magic.GRAVITY_MIN;
                }
                if (frictionDist > limitV) {
                    limitV = frictionDist;
                    tags.add("vfrict_g");
                }
            }
        }

        if (model.ground) {
            // Jump lift off gain.
            // NOTE: This assumes SurvivalFly busies about moves with from.onGroundOrResetCond.
            if (yDistance > limitV && !thisMove.to.onGroundOrResetCond && !thisMove.from.onGroundOrResetCond && (
                    // Last move touched ground.
                    lastMove.toIsValid && lastMove.touchedGround && 
                    (lastMove.yDistance <= 0.0 || lastMove.to.extraPropertiesValid && lastMove.to.onGround)
                    // This move touched ground by a workaround.
                    || thisMove.touchedGroundWorkaround
                    )) {
                // Allow normal jumping.
                final double maxGain = LiftOffEnvelope.NORMAL.getMaxJumpGain(data.jumpAmplifier);
                if (maxGain > limitV) {
                    limitV = maxGain;
                    tags.add("jump_gain");
                }
            }
        }

        // Ordinary step up.
        // TODO: Might be within a 'if (model.ground)' block?
        // TODO: sfStepHeight should be a common modeling parameter?
        if (yDistance > limitV && yDistance <= cc.sfStepHeight 
                && (lastMove.toIsValid && lastMove.yDistance < 0.0 || from.isOnGroundOrResetCond() || thisMove.touchedGroundWorkaround)
                && to.isOnGround()) {
            // (Jump effect not checked yet.)
            limitV = cc.sfStepHeight;
            tags.add("step_up");
        }

        // Determine violation amount.
        resultV = Math.max(0.0, yDistance - limitV);

        // Post-violation recovery.


        return new double[] {limitV, resultV};
    }

    private double hackLytra(final double yDistance, final double limitV, final PlayerMoveData thisMove, final PlayerMoveData lastMove, final MovingData data) {
        // TODO: Further: jumpphase vs. y-distance to set-back. Problem: velocity
        // TODO: Further: record max h and descend speeds and relate to those.
        // TODO: Demand total speed to decrease.
        if (yDistance > Magic.GLIDE_DESCEND_PHASE_MIN && yDistance < 17.0 * Magic.GRAVITY_MAX
                && (
                        // Normal envelope.
                        yDistance - lastMove.yDistance < Magic.GRAVITY_MAX * 1.5
                        // Inversion (neg -> pos).
                        || lastMove.yDistance < -Magic.GRAVITY_SPAN && yDistance < Magic.GRAVITY_MAX + Magic.GRAVITY_ODD && yDistance > Magic.GRAVITY_SPAN
                        )
                && thisMove.hDistance < lastMove.hDistance
                && (lastMove.yDistance > 0.0 || lastMove.hDistance > 0.55) // Demand some speed on the transition.
                // Demand total speed to decrease somehow, unless for the very transition.
                && (thisMove.distanceSquared / lastMove.distanceSquared < 0.99
                        || lastMove.yDistance < 0.0) // Might confine the latter something to be tested.
                ) {
            if (lastMove.hDistance > 0.52) {
                // (Increasing y-distance.)
                tags.add("elytra_asc1");
                return yDistance;
            }
            else if (thisMove.hDistance > Magic.GRAVITY_MIN && yDistance < lastMove.yDistance) {
                // (Decreasing y-distance.)
                final PlayerMoveData pastMove1 = data.playerMoves.getSecondPastMove();
                if (pastMove1.toIsValid && pastMove1.to.extraPropertiesValid) {
                    // Demand this being the first one, or decreasing by a decent amount with past two moves.
                    if (
                            // First move rather decreasing.
                            pastMove1.yDistance < lastMove.yDistance 
                            // Decreasing by a reasonable (?) amount.
                            || yDistance - pastMove1.yDistance < -0.001
                            // && yDistance - lastMove.yDistance < lastMove.yDistance - pastMove1.yDistance - 0.0005 // Probably need remove.
                            ) {
                        tags.add("elytra_asc2");
                        return yDistance;
                    }
                }
            }

        }
        return limitV;
    }

    /**
     * 
     * @param from
     * @param to
     * @param yDistance
     * @param flying
     * @param lastMove
     * @param model
     * @param data
     * @param cc
     * @return limitV, resultV
     */
    private double[] vDistDescend(final PlayerLocation from, final PlayerLocation to, final double yDistance, final boolean flying, final PlayerMoveData lastMove, final ModelFlying model, final MovingData data, final MovingConfig cc) {
        double limitV = 0.0;
        double resultV = 0.0;
        // Note that 'extreme moves' are covered by the extreme move check.
        // TODO: if gravity: friction + gravity.
        // TODO: deny falling, possibly special case head-step-down - to be tested (levitation).
        // TODO: min-max envelope (elytra).
        // TODO: ordinary flying (flying: enforce maximum speed at least)
        return new double[] {limitV, resultV};
    }

    /**
     * 
     * @param from
     * @param to
     * @param yDistance
     * @param flying
     * @param lastMove
     * @param model
     * @param data
     * @param cc
     * @return limitV, resultV
     */
    private double[] vDistZero(final PlayerLocation from, final PlayerLocation to, final double yDistance, final boolean flying, final PlayerMoveData lastMove, final ModelFlying model, final MovingData data, final MovingConfig cc) {
        double limitV = 0.0;
        double resultV = 0.0;
        // TODO: Deny on enforcing mingain.
        return new double[] {limitV, resultV};
    }

    private double getCorrectedHeight(final double maximumHeight, final World world) {
        return Math.max(maximumHeight - 10.0, world.getMaxHeight());
    }

    private void outpuDebugMove(final Player player, final double hDistance, final double limitH, final double yDistance, final double limitV, final ModelFlying model, final List<String> tags, final MovingData data) {
        final PlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        StringBuilder builder = new StringBuilder(350);
        final String dHDist = lastMove.toIsValid ? " (" + StringUtil.formatDiff(hDistance, lastMove.hDistance) + ")" : "";
        final String dYDist = lastMove.toIsValid ? " (" + StringUtil.formatDiff(yDistance, lastMove.yDistance)+ ")" : "";
        builder.append("hDist: " + hDistance + dHDist + " / " + limitH + " , vDist: " + yDistance + dYDist + " / " + limitV);
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        if (lastMove.toIsValid) {
            builder.append(" , fdsq: " + StringUtil.fdec3.format(thisMove.distanceSquared / lastMove.distanceSquared));
        }
        if (data.verVelUsed != null) {
            builder.append(" , vVelUsed: " + data.verVelUsed);
        }
        builder.append(" , model: " + model.id);
        if (!tags.isEmpty()) {
            builder.append(" , tags: ");
            builder.append(StringUtil.join(tags, "+"));
        }
        builder.append(" , jumpphase: " + data.sfJumpPhase);
        thisMove.addExtraProperties(builder, " , ");
        debug(player, builder.toString());
    }

}
