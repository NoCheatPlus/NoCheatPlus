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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.magic.LostGround;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.magic.MagicAir;
import fr.neatmonster.nocheatplus.checks.moving.magic.MagicLiquid;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.LocationData;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.workaround.WRPT;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeEnchant;
import fr.neatmonster.nocheatplus.compat.blocks.BlockChangeTracker;
import fr.neatmonster.nocheatplus.compat.blocks.BlockChangeTracker.BlockChangeEntry;
import fr.neatmonster.nocheatplus.compat.blocks.BlockChangeTracker.Direction;
import fr.neatmonster.nocheatplus.components.modifier.IAttributeAccess;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * The counterpart to the CreativeFly check. People that are not allowed to fly get checked by this. It will try to
 * identify when they are jumping, check if they aren't jumping too high or far, check if they aren't moving too fast on
 * normal ground, while sprinting, sneaking, swimming, etc.
 */
public class SurvivalFly extends Check {

    // Tags
    private static final String DOUBLE_BUNNY = "doublebunny";

    // Other.
    /** Bunny-hop delay. */
    private static final int   bunnyHopMax = 10;
    /** Divisor vs. last hDist for minimum slow down. */
    private static final double bunnyDivFriction = 160.0; // Rather in-air, blocks would differ by friction.



    // TODO: Friction by block to walk on (horizontal only, possibly to be in BlockProperties rather).

    /** To join some tags with moving check violations. */
    private final ArrayList<String> tags = new ArrayList<String>(15);
    private final ArrayList<String> justUsedWorkarounds = new ArrayList<String>();


    private final Set<String> reallySneaking = new HashSet<String>(30);

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    private final BlockChangeTracker blockChangeTracker;

    // TODO: handle
    private final AuxMoving aux = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(AuxMoving.class);

    private IGenericInstanceHandle<IAttributeAccess> attributeAccess = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(IAttributeAccess.class);


    /**
     * Instantiates a new survival fly check.
     */
    public SurvivalFly() {
        super(CheckType.MOVING_SURVIVALFLY);
        blockChangeTracker = NCPAPIProvider.getNoCheatPlusAPI().getBlockChangeTracker();
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     * @param isSamePos 
     * @return the location
     */
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final boolean mightBeMultipleMoves, final MovingData data, final MovingConfig cc, final long now) {
        tags.clear();
        if (data.debug) {
            justUsedWorkarounds.clear();
            data.ws.setJustUsedIds(justUsedWorkarounds);
        }
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final PlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        final boolean isSamePos = from.isSamePos(to);

        // Calculate some distances.
        final double xDistance, yDistance, zDistance, hDistance;
        final boolean hasHdist;
        if (isSamePos) {
            // TODO: Could run a completely different check here (roughly none :p).
            xDistance = yDistance = zDistance = hDistance = 0.0;
            hasHdist = false;
        } else {
            xDistance = to.getX() - from.getX();
            yDistance = thisMove.yDistance;
            zDistance = to.getZ() - from.getZ();
            if (xDistance == 0.0 && zDistance == 0.0) {
                hDistance = 0.0;
                hasHdist = false;
            } else {
                hasHdist = true;
                hDistance = thisMove.hDistance;
            }
        }

        // Recover from data removal (somewhat random insertion point).
        if (data.liftOffEnvelope == LiftOffEnvelope.UNKNOWN) {
            data.adjustMediumProperties(from);
        }

        // Set some flags.
        final boolean fromOnGround = thisMove.from.onGround;
        final boolean toOnGround = thisMove.to.onGround;
        final boolean resetTo = toOnGround || to.isResetCond();

        // Determine if the player is actually sprinting.
        final boolean sprinting;
        if (data.lostSprintCount > 0) {
            // Sprint got toggled off, though the client is still (legitimately) moving at sprinting speed.
            // NOTE: This could extend the "sprinting grace" period, theoretically, until on ground.
            if (resetTo && (fromOnGround || from.isResetCond()) || hDistance <= Magic.WALK_SPEED) {
                // Invalidate.
                data.lostSprintCount = 0;
                tags.add("invalidate_lostsprint");
                if (now <= data.timeSprinting + cc.sprintingGrace) {
                    sprinting = true;
                } else {
                    sprinting = false;
                }
            }
            else {
                tags.add("lostsprint");
                sprinting = true;
                if (data.lostSprintCount < 3 && toOnGround || to.isResetCond()) {
                    data.lostSprintCount = 0;
                }
                else {
                    data.lostSprintCount --;
                }
            }
        }
        else if (now <= data.timeSprinting + cc.sprintingGrace) {
            // Within grace period for hunger level being too low for sprinting on server side (latency).
            if (now != data.timeSprinting) {
                tags.add("sprintgrace");
            }
            sprinting = true;
        }
        else {
            sprinting = false;
        }

        // Use the player-specific walk speed.
        // TODO: Might get from listener.
        // TODO: Use in lostground?
        thisMove.walkSpeed = Magic.WALK_SPEED * ((double) data.walkSpeed / 0.2);

        setNextFriction(thisMove, data, cc);

        /////////////////////////////////
        // Mixed checks (lost ground).
        /////////////////////////////////


        final boolean resetFrom;
        if (fromOnGround || from.isResetCond()) {
            resetFrom = true;
        }
        // TODO: Extra workarounds for toOnGround (step-up is a case with to on ground)?
        else if (isSamePos) {
            // TODO: This isn't correct, needs redesign.
            if (lastMove.toIsValid && lastMove.hDistance > 0.0 && lastMove.yDistance < -0.3) {
                // Note that to is not on ground either.
                resetFrom = LostGround.lostGroundStill(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags);
            } else {
                resetFrom = false;
            }
        }
        else {
            // "Lost ground" workaround.
            // TODO: More refined conditions possible ?
            // TODO: Consider if (!resetTo) ?
            // Check lost-ground workarounds.
            resetFrom = LostGround.lostGround(player, from, to, hDistance, yDistance, sprinting, lastMove, data, cc, tags);
            // Note: if not setting resetFrom, other places have to check assumeGround...
        }

        if (thisMove.touchedGround && !thisMove.from.onGround && !thisMove.to.onGround) {
            // Lost ground workaround has just been applied, check resetting of the dirty flag.
            // TODO: Always/never reset with any ground touched?
            data.resetVelocityJumpPhase();
        }

        // Renew the "dirty"-flag (in-air phase affected by velocity).
        if (data.isVelocityJumpPhase() || data.resetVelocityJumpPhase()) {
            // (Reset is done after checks run.) 
            tags.add("dirty");
        }

        // Check if head is obstructed.
        //if (!resetFrom || !resetTo) {
        thisMove.headObstructed = (yDistance > 0.0 ? from.isHeadObstructed(yDistance) : from.isHeadObstructed()) 
                //                    || to.isHeadObstructed() // Best not have this one.
                ;
        //}
        
        // HACK: Force sfNoLowJump by a flag.
        // TODO: Might remove that flag, as the issue for trying this has been resolved differently (F_HEIGHT8_1).
        // TODO: Consider setting on ground_height always?
        if ((from.getBlockFlags() & BlockProperties.F_ALLOW_LOWJUMP) != 0) {
            // TODO: Specialize - test for foot region?
            data.sfNoLowJump = true;
        }

        //////////////////////
        // Horizontal move.
        //////////////////////

        // TODO: Account for lift-off medium / if in air [i.e. account for medium + friction]?

        // Alter some data / flags.
        data.bunnyhopDelay--; // TODO: Design to do the changing at the bottom? [if change: check limits in bunnyHop(...)]

        // Set flag for swimming with the flowing direction of liquid.
        thisMove.downStream = hDistance > thisMove.walkSpeed * Magic.modSwim && thisMove.from.inLiquid && from.isDownStream(xDistance, zDistance);

        // Handle ice.
        // TODO: Re-model ice stuff and other (e.g. general thing: ground-modifier + reset conditions).
        if (thisMove.from.onIce || thisMove.to.onIce) {
            data.sfOnIce = 20;
        }
        else if (data.sfOnIce > 0) {
            // TODO: Here some friction might apply, could become a general thing with bunny and other.
            // TODO: Other reset conditions.
            data.sfOnIce--;
        }

        // TODO: Remove these local variables ?
        double hAllowedDistance = 0.0, hDistanceAboveLimit = 0.0, hFreedom = 0.0;
        if (hasHdist) {
            // Check allowed vs. taken horizontal distance.
            // Get the allowed distance.
            hAllowedDistance = setAllowedhDist(player, sprinting, thisMove, data, cc, false);
            // Judge if horizontal speed is above limit.
            hDistanceAboveLimit = hDistance - hAllowedDistance;

            // Velocity, buffers and after failure checks.
            if (hDistanceAboveLimit > 0) {
                // TODO: Move more of the workarounds (buffer, bunny, ...) into this method.
                final double[] res = hDistAfterFailure(player, from, to, hAllowedDistance, hDistanceAboveLimit, sprinting, thisMove, lastMove, data, cc, false);
                hAllowedDistance = res[0];
                hDistanceAboveLimit = res[1];
                hFreedom = res[2];
            }
            else {
                data.clearActiveHorVel();
                hFreedom = 0.0;
                if (resetFrom && data.bunnyhopDelay <= 6) {
                    data.bunnyhopDelay = 0;
                }
            }
            
            // hacc (if enabled, always update)
            final double fcmhv = Math.max(1.0, thisMove.hDistance / thisMove.hAllowedDistanceBase);
            data.combinedMediumHCount ++;
            data.combinedMediumHValue += fcmhv;
            // TODO: Balance, where to check / use (...).
            if (data.combinedMediumHCount > 30) { // TODO: Adjust whatever way.
                final double fcmh = data.combinedMediumHValue / (double) data.combinedMediumHCount;
                final double limitFCMH;
                // TODO: with buffer use, might want to skip.
                if (data.liftOffEnvelope == LiftOffEnvelope.NORMAL) {
                    limitFCMH = 1.34;
                }
                else if (data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID 
                        || data.liftOffEnvelope == LiftOffEnvelope.LIMIT_NEAR_GROUND) {
                    // limitFCMH = 1.05; // Seems to work on 1.10
                    limitFCMH = 1.1; // 1.8.8 in-water moves with jumping near/on surface. 1.2 is max factor for one move (!).
                    // TODO: Version+context dependent setting and/or confine by in-water moves, whatever.
                }
                else {
                    limitFCMH = 1.0;
                }
                if (fcmh > limitFCMH && !data.isVelocityJumpPhase()) { // TODO: Configurable / adjust by medium type.
                    hDistanceAboveLimit = hDistance * (fcmh - limitFCMH);
                    tags.add("hacc");
                    // Reset for now.
                    data.combinedMediumHCount = 0;
                    data.combinedMediumHValue = 0.0;
                }
                else {
                    // TODO: Other cases (1.0, between, ...)?
                    data.combinedMediumHCount = 1;
                    data.combinedMediumHValue = fcmhv;
                }
            }

            // Prevent players from walking on a liquid in a too simple way.
            // TODO: Find something more effective against more smart methods (limitjump helps already).
            // TODO: yDistance == 0D <- should there not be a tolerance +- or 0...x ?
            // TODO: Complete re-modeling.
            if (hDistanceAboveLimit <= 0D && hDistance > 0.1D && yDistance == 0D && !toOnGround && !fromOnGround 
                    && lastMove.toIsValid && lastMove.yDistance == 0D 
                    && BlockProperties.isLiquid(to.getTypeId()) && BlockProperties.isLiquid(from.getTypeId())
                    && !from.isHeadObstructed() && !to.isHeadObstructed() // TODO: Might decrease margin here.
                    ) {
                // TODO: Relative hdistance.
                // TODO: Might check actual bounds (collidesBlock). Might implement + use BlockProperties.getCorrectedBounds or getSomeHeight.
                hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hDistance);
                tags.add("waterwalk");
            }

            // Prevent players from sprinting if they're moving backwards (allow buffers to cover up !?).
            if (sprinting && data.lostSprintCount == 0 && !cc.assumeSprint && hDistance > thisMove.walkSpeed && !data.hasActiveHorVel()) {
                // (Ignore some cases, in order to prevent false positives.)
                // TODO: speed effects ?
                if (TrigUtil.isMovingBackwards(xDistance, zDistance, from.getYaw()) && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPRINTING)) {
                    // (Might have to account for speeding permissions.)
                    // TODO: hDistance is too harsh?
                    hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hDistance);
                    tags.add("sprintback"); // Might add it anyway.
                }
            }
        } else {
            /*
             * TODO: Consider to log and/or remember when this was last time
             * cleared [add time distance to tags/log on violations].
             */
            data.clearActiveHorVel();
            thisMove.hAllowedDistanceBase = 0.0;
            thisMove.hAllowedDistance = 0.0;
            // TODO: Other properties should be set as well?
        }


        //////////////////////////
        // Vertical move.
        //////////////////////////

        // Calculate the vertical speed limit based on the current jump phase.
        double vAllowedDistance = 0, vDistanceAboveLimit = 0;
        // Distinguish certain media.
        if (yDistance >= 0.0 && yDistance <= cc.sfStepHeight && toOnGround && fromOnGround ) {
            // Wild-card allow step height from ground to ground.
            // TODO: Which of (fromOnGround || data.noFallAssumeGround || lastMove.toIsValid && lastMove.yDistance < 0.0)?
            vAllowedDistance = cc.sfStepHeight;
        }
        else if (from.isInWeb()) {
            // TODO: Further confine conditions.
            final double[] res = vDistWeb(player, thisMove, toOnGround, hDistanceAboveLimit, now,data,cc);
            vAllowedDistance = res[0];
            vDistanceAboveLimit = res[1];
            if (res[0] == Double.MIN_VALUE && res[1] == Double.MIN_VALUE) {
                // Silent set-back.
                if (data.debug) {
                    tags.add("silentsbcobweb");
                    outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
                    data.ws.setJustUsedIds(null);
                }
                return data.getSetBack(to);
            }
        }
        else if (from.isOnClimbable()) {
            // Ladder types.
            vDistanceAboveLimit = vDistClimbable(player, from, to, fromOnGround, toOnGround, 
                    thisMove, lastMove, yDistance, data);
        }
        else if (thisMove.from.inLiquid) { // && (Math.abs(yDistance) > 0.2 || to.isInLiquid())) {
            // Swimming...
            final double[] res = vDistLiquid(from, to, toOnGround, yDistance, lastMove, data);
            vAllowedDistance = res[0];
            vDistanceAboveLimit = res[1];
            if (vDistanceAboveLimit <= 0.0 && yDistance > 0.0 && Math.abs(yDistance) > Magic.swimBaseSpeedV()) {
                data.setFrictionJumpPhase();
            }
        }
        else {
            final double[] res = vDistAir(now, player, from, fromOnGround, resetFrom, to, toOnGround, resetTo, hDistanceAboveLimit, yDistance, mightBeMultipleMoves, lastMove, data, cc);
            vAllowedDistance = res[0];
            vDistanceAboveLimit = res[1];
        }

        // Post-check recovery.
        if (vDistanceAboveLimit > 0.0 && Math.abs(yDistance) <= 1.015 && cc.blockChangeTrackerPush) {
            // TODO: Better place for checking for push [redesign for intermediate result objects?].
            // Vertical push/pull.
            double[] pushResult = getPushResultVertical(yDistance, from, to, data);
            if (pushResult != null) {
                vAllowedDistance = pushResult[0];
                vDistanceAboveLimit = pushResult[1];
            }
        }
        // Push/pull sideways.
        // TODO: Slightly itchy: regard x and z separately (Better in another spot).

        // TODO: on ground -> on ground improvements.

        // Debug output.
        final int tagsLength;
        if (data.debug) {
            outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
            tagsLength = tags.size();
            data.ws.setJustUsedIds(null);
        } else {
            tagsLength = 0; // JIT vs. IDE.
        }

        ///////////////////////
        // Handle violations.
        ///////////////////////

        final double result = (Math.max(hDistanceAboveLimit, 0D) + Math.max(vDistanceAboveLimit, 0D)) * 100D;
        if (result > 0D) {
            final Location vLoc = handleViolation(now, result, player, from, to, data, cc);
            if (vLoc != null) {
                return vLoc;
            }
        }
        else {
            // Slowly reduce the level with each event, if violations have not recently happened.
            if (now - data.sfVLTime > cc.survivalFlyVLFreeze) {
                data.survivalFlyVL *= 0.95D;
            }

            // Finally check horizontal buffer regain.
            if (hDistanceAboveLimit < 0.0  && result <= 0.0 && !isSamePos && data.sfHorizontalBuffer < cc.hBufMax) {
                // TODO: max min other conditions ?
                hBufRegain(hDistance, Math.min(0.2, Math.abs(hDistanceAboveLimit)), data, cc);
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////////
        //  Set data for normal move or violation without cancel (cancel would have returned above).
        //////////////////////////////////////////////////////////////////////////////////////////////

        // Check LiftOffEnvelope.
        // TODO: Web before liquid? Climbable?
        // TODO: isNextToGround(0.15, 0.4) allows a little much (yMargin), but reduces false positives.
        // TODO: nextToGround: Shortcut with block-flags ?
        final LiftOffEnvelope oldLiftOffEnvelope = data.liftOffEnvelope;
        if (to.isInLiquid()) {
            if (fromOnGround && !toOnGround 
                    && data.liftOffEnvelope == LiftOffEnvelope.NORMAL
                    && data.sfJumpPhase <= 0  && !thisMove.from.inLiquid) {
                // KEEP
            }
            else if (to.isNextToGround(0.15, 0.4)) {
                // Consent with ground.
                data.liftOffEnvelope = LiftOffEnvelope.LIMIT_NEAR_GROUND;
            } 
            else {
                // TODO: Distinguish strong limit from normal.
                data.liftOffEnvelope = LiftOffEnvelope.LIMIT_LIQUID;
            }
        }
        else if (thisMove.to.inWeb) {
            data.liftOffEnvelope = LiftOffEnvelope.NO_JUMP; // TODO: Test.
        }
        else if (resetTo) {
            // TODO: This might allow jumping on vines etc., but should do for the moment.
            data.liftOffEnvelope = LiftOffEnvelope.NORMAL;
        }
        else if (thisMove.from.inLiquid) {
            if (!resetTo 
                    && data.liftOffEnvelope == LiftOffEnvelope.NORMAL
                    && data.sfJumpPhase <= 0) {
                // KEEP
            }
            else if (to.isNextToGround(0.15, 0.4)) {
                // TODO: Problematic: y-distance slope can be low jump.
                data.liftOffEnvelope = LiftOffEnvelope.LIMIT_NEAR_GROUND;
            }
            else {
                // TODO: Distinguish strong limit.
                data.liftOffEnvelope = LiftOffEnvelope.LIMIT_LIQUID;
            }
        }
        else if (thisMove.from.inWeb) {
            data.liftOffEnvelope = LiftOffEnvelope.NO_JUMP; // TODO: Test.
        }
        else if (resetFrom || thisMove.touchedGround) {
            // TODO: Where exactly to put noFallAssumeGround ?
            data.liftOffEnvelope = LiftOffEnvelope.NORMAL;
        }
        else {
            // Keep medium.
            // TODO: Is above stairs ?
        }
        // Count how long one is moving inside of a medium.
        if (oldLiftOffEnvelope != data.liftOffEnvelope) {
            data.insideMediumCount = 0;
            data.combinedMediumHCount = 0;
            data.combinedMediumHValue = 0.0;
        }
        else if (!resetFrom || !resetTo) {
            data.insideMediumCount = 0;
        } else {
            data.insideMediumCount ++;
        }

        // Apply reset conditions.
        boolean inAir = false; // Hack.
        if (resetTo) {
            // The player has moved onto ground.
            if (toOnGround) {
                // Reset bunny-hop-delay.
                if (data.bunnyhopDelay > 0 && yDistance > 0.0 && to.getY() > data.getSetBackY() + 0.12 && !from.isResetCond() && !to.isResetCond()) {
                    data.bunnyhopDelay = 0;
                    tags.add("resetbunny");
                }
            }
            // Reset data.
            data.setSetBack(to);
            data.sfJumpPhase = 0;
            data.clearAccounting();
            data.sfNoLowJump = false;
            if (data.sfLowJump && resetFrom) {
                // Prevent reset if coming from air (purpose of the flag).
                data.sfLowJump = false;
            }
            if (hFreedom <= 0.0 && data.verVelUsed == null) {
                data.resetVelocityJumpPhase();
            }
        }
        else if (resetFrom) {
            // The player moved from ground.
            data.setSetBack(from);
            data.sfJumpPhase = 1; // This event is already in air.
            data.clearAccounting();
            data.sfLowJump = false;
            // not resetting nolowjump (?)...
            // Don't reset velocity phase unless moving into resetcond.
            //            if (hFreedom <= 0.0 && data.verVelUsed == null && (!data.noFallAssumeGround || fromOnGround)) {
            //                data.resetVelocityJumpPhase();
            //            }
        }
        else {
            data.sfJumpPhase ++;
            if (to.getY() < 0.0 && cc.sfSetBackPolicyVoid) {
                data.setSetBack(to);
            }
            inAir = true;
        }

        if (inAir) {
            // Adjust in-air counters.
            if (yDistance == 0.0) {
                data.sfZeroVdistRepeat ++;
            } else {
                data.sfZeroVdistRepeat = 0;
            }
        }
        else {
            data.sfZeroVdistRepeat = 0;
            data.ws.resetConditions(WRPT.G_RESET_NOTINAIR);
        }

        // Horizontal velocity invalidation.
        if (hDistance <= (cc.velocityStrictInvalidation ? thisMove.hAllowedDistanceBase : thisMove.hAllowedDistanceBase / 2.0)) {
            // TODO: Should there be other side conditions?
            // Invalidate used horizontal velocity.
            // debug(player, "*** INVALIDATE ON SPEED");
            data.clearActiveHorVel();
        }

        // Update unused velocity tracking.
        // TODO: Hide and seek with API.
        // TODO: Pull down tick / timing data (perhaps add an API object for millis + source + tick + sequence count (+ source of sequence count).
        if (data.debug) {
            // TODO: Only update, if velocity is queued at all.
            data.getVerticalVelocityTracker().updateBlockedState(TickTask.getTick(), 
                    // Assume blocked with being in web/water, despite not entirely correct.
                    thisMove.headObstructed || thisMove.from.resetCond,
                    // (Similar here.)
                    thisMove.touchedGround || thisMove.to.resetCond);
            // TODO: TEST: Check unused velocity here too. (Should have more efficient process, pre-conditions for checking.)
            UnusedVelocity.checkUnusedVelocity(player, type, data, cc);
        }

        // Adjust data.
        data.lastFrictionHorizontal = data.nextFrictionHorizontal;
        data.lastFrictionVertical = data.nextFrictionVertical;

        // Log tags added after violation handling.
        if (data.debug && tags.size() > tagsLength) {
            logPostViolationTags(player);
        }
        return null;
    }

    /**
     * Check for push/pull by pistons, alter data appropriately (blockChangeId).
     * 
     * @param yDistance
     * @param from
     * @param to
     * @param data
     * @return
     */
    private double[] getPushResultVertical(final double yDistance, final PlayerLocation from, final PlayerLocation to, final MovingData data) {
        /*
         * TODO: Once horizontal push is allowed too, a maxIdEntry has to be
         * passed as argument and data.updateBlockChangeReference has to be
         * called after processing all pushing. Return the new maxEntry if
         * updated, or the old one.
         */
        // TODO: Allow push up to 1.0 (or 0.65 something) even beyond block borders, IF COVERED [adapt PlayerLocation].
        // TODO: Might have to allow pushing up to a distance of 1.0 if covered.
        // TODO: Cleanup todo.
        // Push (/pull) up.
        if (yDistance > 0.0 && (yDistance <= 1.0 
                // Extra condition for full blocks: slightly more possible.
                // Extreme case: 1.51 blocks up (details pending).
                || yDistance <= 1.015 && to.getY() - to.getBlockY() < 0.015)) {
            // TODO: Other conditions? [some will be in passable later].
            final BlockChangeEntry entryYPos = from.getBlockChangeIdPush(blockChangeTracker, data.blockChangeRef, Direction.Y_POS, Math.min(yDistance, 1.0));
            if (entryYPos != null) {
                data.updateBlockChangeReference(entryYPos, to);
                tags.add("push_y_pos");
                final double maxDistYPos = yDistance; //1.0 - (from.getY() - from.getBlockY()); // TODO: Margin ?
                return new double[]{maxDistYPos, 0.0};
            }
        }
        // Push (/pull) down.
        else if (yDistance < 0.0 && yDistance >= -1.0) {
            // TODO: Other conditions? [some will be in passable later].
            final BlockChangeEntry entryYNeg = from.getBlockChangeIdPush(blockChangeTracker, data.blockChangeRef, Direction.Y_NEG, -yDistance);
            if (entryYNeg != null) {
                data.updateBlockChangeReference(entryYNeg, to);
                tags.add("push_y_neg");
                final double maxDistYNeg = yDistance; // from.getY() - from.getBlockY(); // TODO: Margin ?
                return new double[]{maxDistYNeg, 0.0};
            }
        }
        // Nothing found.
        return null;
    }

    /**
     * Set data.nextFriction according to media.
     * @param from
     * @param to
     * @param data
     * @param cc
     */
    private void setNextFriction(final PlayerMoveData thisMove, final MovingData data, final MovingConfig cc) {
        // NOTE: Other methods might still override nextFriction to 1.0 due to burst/lift-off envelope.
        // TODO: Other media / medium transitions / friction by block.
        final LocationData from = thisMove.from;
        final LocationData to = thisMove.to;
        if (from.inWeb || to.inWeb) {
            data.nextFrictionHorizontal = data.nextFrictionVertical = 0.0;
        }
        else if (from.onClimbable || to.onClimbable) {
            // TODO: Not sure about horizontal (!).
            data.nextFrictionHorizontal = data.nextFrictionVertical = 0.0;
        }
        else if (from.inLiquid) {
            // TODO: Exact conditions ?!
            if (from.inLava) {
                data.nextFrictionHorizontal = data.nextFrictionVertical = Magic.FRICTION_MEDIUM_LAVA;
            }
            else {
                data.nextFrictionHorizontal = data.nextFrictionVertical = Magic.FRICTION_MEDIUM_WATER;
            }
        }
        // TODO: consider setting minimum friction last (air), do add ground friction.
        else if (!from.onGround && !to.onGround) {
            data.nextFrictionHorizontal = data.nextFrictionVertical = Magic.FRICTION_MEDIUM_AIR;
        }
        else {
            data.nextFrictionHorizontal = 0.0; // TODO: Friction for walking on blocks (!).
            data.nextFrictionVertical = Magic.FRICTION_MEDIUM_AIR;
        }

    }

    /**
     * Set hAllowedDistanceBase and hAllowedDistance in thisMove. Not exact,
     * check permissions as far as necessary, if flag is set to check them.
     * 
     * @param player
     * @param sprinting
     * @param thisMove
     * @param data
     * @param cc
     * @param checkPermissions
     *            If to check permissions, allowing to speed up a little bit.
     *            Only set to true after having failed with it set to false.
     * @return Allowed distance.
     */
    private double setAllowedhDist(final Player player, final boolean sprinting, final PlayerMoveData thisMove, final MovingData data, final MovingConfig cc, boolean checkPermissions)
    {
        // TODO: Optimize for double checking?
        final PlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        double hAllowedDistance = 0D;

        final boolean sfDirty = data.isVelocityJumpPhase();
        double friction = data.lastFrictionHorizontal; // Friction to use with this move.
        // TODO: sfDirty: Better friction/envelope-based.
        boolean useBaseModifiers = false;
        if (thisMove.from.inWeb) {
            data.sfOnIce = 0;
            // TODO: if (from.isOnIce()) <- makes it even slower !
            // Does include sprinting by now (would need other accounting methods).
            hAllowedDistance = Magic.modWeb * thisMove.walkSpeed * cc.survivalFlyWalkingSpeed / 100D;
            friction = 0.0; // Ensure friction can't be used to speed.
        }
        else if (thisMove.from.inLiquid && thisMove.to.inLiquid) {
            // Check all liquids (lava might demand even slower speed though).
            // TODO: Test how to go with only checking from (less dolphins).
            // TODO: Sneaking and blocking applies to when in water !
            hAllowedDistance = Magic.modSwim * thisMove.walkSpeed * cc.survivalFlySwimmingSpeed / 100D;
            if (thisMove.from.inWater || !thisMove.from.inLava) { // (We don't really have other liquids, though.)
                final int level = BridgeEnchant.getDepthStriderLevel(player);
                if (level > 0) {
                    // The hard way.
                    hAllowedDistance *= Magic.modDepthStrider[level];
                    // Modifiers: Most speed seems to be reached on ground, but couldn't nail down.
                    useBaseModifiers = true;
                }
            }
            // (Friction is used as is.)
        }
        // TODO: !sfDirty is very coarse, should use friction instead.
        else if (!sfDirty && thisMove.from.onGround && player.isSneaking() && reallySneaking.contains(player.getName()) && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SNEAKING))) {
            hAllowedDistance = Magic.modSneak * thisMove.walkSpeed * cc.survivalFlySneakingSpeed / 100D;
            friction = 0.0; // Ensure friction can't be used to speed.
            // TODO: Attribute modifiers can count in here, e.g. +0.5 (+ 50% doesn't seem to pose a problem, neither speed effect 2).
        }
        // TODO: !sfDirty is very coarse, should use friction instead.
        else if (!sfDirty && thisMove.from.onGround && player.isBlocking() && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))) {
            hAllowedDistance = Magic.modBlock * thisMove.walkSpeed * cc.survivalFlyBlockingSpeed / 100D;
            friction = 0.0; // Ensure friction can't be used to speed.
        }
        else {
            useBaseModifiers = true;
            if (sprinting) {
                hAllowedDistance = thisMove.walkSpeed * cc.survivalFlySprintingSpeed / 100D;
            }
            else {
                hAllowedDistance = thisMove.walkSpeed * cc.survivalFlyWalkingSpeed / 100D;
            }
            // Ensure friction can't be used to speed.
            // TODO: Model bunny hop as a one time peak + friction. Allow medium based friction.
            friction = 0.0;
        }
        // Apply modifiers (sprinting, attributes, ...).
        if (useBaseModifiers) {
            if (sprinting) {
                hAllowedDistance *= data.multSprinting;
            }
            // Note: Attributes count in slowness potions, thus leaving out isn't possible.
            final double attrMod = attributeAccess.getHandle().getSpeedAttributeMultiplier(player);
            if (attrMod == Double.MAX_VALUE) {
                // TODO: Slowness potion.
                // Count in speed potions.
                final double speedAmplifier = mcAccess.getHandle().getFasterMovementAmplifier(player);
                if (speedAmplifier != Double.NEGATIVE_INFINITY) {
                    hAllowedDistance *= 1.0D + 0.2D * (speedAmplifier + 1);
                }
            }
            else {
                hAllowedDistance *= attrMod;
                // TODO: Consider getting modifiers from items, calculate with classic means (or iterate over all modifiers).
                // Hack for allow sprint-jumping with slowness.
                if (sprinting && hAllowedDistance < 0.29 && cc.sfSlownessSprintHack && player.hasPotionEffect(PotionEffectType.SLOW)) {
                    // TODO: Should restrict further by yDistance, ground and other (jumping only).
                    // TODO: Restrict to not in water (depth strider)?
                    hAllowedDistance = slownessSprintHack(player, hAllowedDistance);
                }
            }
        }
        // TODO: Reset friction on too big change of direction?

        // Account for flowing liquids (only if needed).
        // Assume: If in liquids this would be placed right here.
        if (thisMove.downStream) {
            hAllowedDistance *= Magic.modDownStream;
        }

        // If the player is on ice, give them a higher maximum speed.
        if (data.sfOnIce > 0) {
            hAllowedDistance *= Magic.modIce;
        }

        // Speeding bypass permission (can be combined with other bypasses).
        if (checkPermissions && player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPEEDING)) {
            hAllowedDistance *= cc.survivalFlySpeedingSpeed / 100D;
        }

        // Base speed is set.
        thisMove.hAllowedDistanceBase = hAllowedDistance;

        // Friction mechanics (next move).
        if (thisMove.hDistance <= hAllowedDistance) {
            // Move is within lift-off/burst envelope, allow next time.
            // TODO: This probably is the wrong place (+ bunny, + buffer)?
            data.nextFrictionHorizontal = 1.0;
        }

        // Friction or not (this move).
        if (lastMove.toIsValid && friction > 0.0) {
            // Consider friction.
            // TODO: Invalidation mechanics.
            // TODO: Friction model for high speeds?
            hAllowedDistance = Math.max(hAllowedDistance, lastMove.hDistance * friction);
        }
        //        if (hAllowedDistance < thisMove.hDistance) {
        //            // After failure recovery.
        //            if (lastMove.toIsValid) {
        //                final double hDistDiff = thisMove.hDistance - lastMove.hDistance;
        //                // Elytra.
        //                if (hDistDiff < Magic.GLIDE_HORIZONTAL_GAIN_MAX
        //                        && Magic.inAir(thisMove) && Bridge1_9.isWearingElytra(player)) {
        //                    // (Abrupt hdist stops aren't covered yet anyway.)
        //                    hAllowedDistance = thisMove.hDistance;
        //                    data.nextFrictionHorizontal = Magic.FRICTION_MEDIUM_AIR;
        //                }
        //            }
        //        }
        thisMove.hAllowedDistance = hAllowedDistance;
        return thisMove.hAllowedDistance;
    }

    /**
     * Return a 'corrected' allowed horizontal speed. Call only if the player
     * has a SLOW effect.
     * 
     * @param player
     * @param hAllowedDistance
     * @return
     */
    private double slownessSprintHack(final Player player, final double hAllowedDistance) {
        // TODO: Certainly wrong for items with speed modifier (see above: calculate the classic way?).
        // Simple: up to high levels they can stay close, with a couple of hops until max base speed. 
        return 0.29;
    }

    /**
     * Access method from outside.
     * @param player
     * @return
     */
    public boolean isReallySneaking(final Player player) {
        return reallySneaking.contains(player.getName());
    }

    /**
     * Core y-distance checks for in-air movement (may include air -> other).
     * @return
     */
    private double[] vDistAir(final long now, final Player player, final PlayerLocation from, final boolean fromOnGround, final boolean resetFrom, final PlayerLocation to, final boolean toOnGround, final boolean resetTo, final double hDistance, final double yDistance, final boolean mightBeMultipleMoves, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc) {
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        // Y-distance for normal jumping, like in air.
        double vAllowedDistance = 0.0;
        double vDistanceAboveLimit = 0.0;

        // Change seen from last yDistance.
        final double yDistChange = lastMove.toIsValid ? yDistance - lastMove.yDistance :  Double.MAX_VALUE;

        // Hacks.
        final boolean envelopeHack;
        if (!resetFrom && !resetTo && MagicAir.venvHacks(from, to, yDistance, yDistChange, thisMove, lastMove, data)) {
            envelopeHack = true;
            tags.add("hack_venv");
        }
        else {
            envelopeHack = false;
        }

        // Relative distance (friction, lift-off).
        // Estimate expected yDistance.
        // TODO: Friction might need same treatment as with horizontal (medium transitions: data.lastFrictionVertical).
        // TODO: lostground_pyramid(yDist < 0.0) -> step up (yDist 0.5). Needs better last-move modeling.
        // TODO: lostground_edgedesc(yDist <0.0) -> Bunny (yDist > .72, e_jump). Needs better last-move modeling.
        // TODO: air->ground...small-range-tp...air-air+vDist==0.0 (might work around with fromWasReset?).
        // TODO: bunny after vDist<0.0... vdistsb. Might need set-back detection. [solved with setFrictionJumpPhase?]
        // TODO: Other edge cases?
        // TODO: Cleanup pending.
        final boolean strictVdistRel;
        final double maxJumpGain = data.liftOffEnvelope.getMaxJumpGain(data.jumpAmplifier);
        final double jumpGainMargin = 0.005; // TODO: Model differently, workarounds where needed. 0.05 interferes with max height vs. velocity (<= 0.47 gain).
        // TODO: Add/set 'allow starting to fall' first (data reset / from ground on if no speed).
        if (lastMove.toIsValid && Magic.fallingEnvelope(yDistance, lastMove.yDistance, data.lastFrictionVertical, 0.0)) {
            // Less headache: Always allow falling. 
            // TODO: Base should be data.lastFrictionVertical? Problem: "not set" detection?
            vAllowedDistance = lastMove.yDistance * data.lastFrictionVertical - Magic.GRAVITY_MIN; // Upper bound.
            strictVdistRel = true;
        }
        else if (resetFrom || thisMove.touchedGroundWorkaround) {
            // TODO: More concise conditions? Some workaround may allow more.
            if (toOnGround) {
                // Hack for boats (coarse: allows minecarts too).
                if (yDistance > cc.sfStepHeight && yDistance - cc.sfStepHeight < 0.00000003 && to.isOnGroundDueToStandingOnAnEntity()) {
                    vAllowedDistance = yDistance;
                } else  {
                    vAllowedDistance = Math.max(cc.sfStepHeight, maxJumpGain + jumpGainMargin);
                }
            }
            else {
                // Code duplication with the absolute limit below.
                if (yDistance < 0.0 || yDistance > cc.sfStepHeight || !tags.contains("lostground_couldstep")) {
                    vAllowedDistance = maxJumpGain + jumpGainMargin;
                }
                else {
                    // lostground_couldstep
                    // TODO: Other conditions / envelopes?
                    vAllowedDistance = yDistance;
                }
            }
            strictVdistRel = false;
        }
        else if (lastMove.toIsValid) {
            if (lastMove.yDistance >= -Math.max(Magic.GRAVITY_MAX / 2.0, 1.3 * Math.abs(yDistance)) && lastMove.yDistance <= 0.0 
                    && (lastMove.touchedGround || lastMove.to.extraPropertiesValid && lastMove.to.resetCond)) {
                if (resetTo) { // TODO: Might have to use max if resetto.
                    vAllowedDistance = cc.sfStepHeight;
                }
                else {
                    // TODO: Needs more precise confinement + setting set back or distance to ground or estYDist.
                    vAllowedDistance = maxJumpGain + jumpGainMargin;
                }
                strictVdistRel = false;
            }
            else {
                // Friction.
                // TODO: data.lastFrictionVertical (see above).
                vAllowedDistance = lastMove.yDistance * data.lastFrictionVertical - Magic.GRAVITY_MIN; // Upper bound.
                strictVdistRel = true;
            }
        }
        else {
            // Teleport/join/respawn.
            vAllowedDistance = vAllowedDistanceNoData(thisMove, lastMove, maxJumpGain, jumpGainMargin, data, cc);
            strictVdistRel = false;
        }
        // Compare yDistance to expected, use velocity on violation.
        // TODO: Quick detect valid envelope and move workaround code into a method.
        // TODO: data.noFallAssumeGround  needs more precise flags (refactor to per move data objects, store 123)
        boolean vDistRelVL = false;
        // Difference from vAllowedDistance to yDistance.
        final double yDistDiffEx = yDistance - vAllowedDistance;
        if (envelopeHack || yDistDiffEx <= 0.0 && yDistDiffEx > -Magic.GRAVITY_SPAN) {
            // (Clearly accepted envelopes first.)
            vDistRelVL = false;
            //vAllowedDistance = yDistance;
        }
        else if (yDistDiffEx > 0.0) { // Upper bound violation.
            // && (yDistance > 0.0 || (!resetTo && !data.noFallAssumeGround))
            if (yDistance <= 0.0 && (resetTo || thisMove.touchedGround)) {
                // Allow falling shorter than expected, if onto ground.
                // Note resetFrom should usually mean that allowed dist is > 0 ?
            }
            else if (lastMove.toIsValid) {
                // TODO: Sort in workarounds to methods, unless extremely frequent.
                if (yDistance < 0.0 && lastMove.yDistance < 0.0 && yDistChange > -Magic.GRAVITY_MAX
                        && (from.isOnGround(Math.abs(yDistance) + 0.001) || BlockProperties.isLiquid(to.getTypeId(to.getBlockX(), Location.locToBlock(to.getY() - 0.5), to.getBlockZ())))) {
                    // Pretty coarse workaround, should instead do a proper modeling for from.getDistanceToGround.
                    // (OR loc... needs different model, distanceToGround, proper set-back, moveHitGround)
                    // TODO: Slightly too short move onto the same level as snow (0.75), but into air (yDistance > -0.5).
                    // TODO: Better on-ground model (adapt to actual client code).
                }
                //                else if (yDistance < 0.0 && yDistChange > 0.0 && tags.contains("lostground_edgedesc")) {
                //                    // Falling less far than expected by hitting an edge.
                //                }
                else if (yDistDiffEx < Magic.GRAVITY_MIN / 2.0 && data.sfJumpPhase == 1 //&& data.fromWasReset
                        // TODO: Test with demanding && (data.noFallAssumeGround || data.liftOffEnvelope != LiftOffEnvelope.NORMAL)
                        && to.getY() - data.getSetBackY() <= data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier)
                        && lastMove.yDistance <= maxJumpGain && yDistance > -Magic.GRAVITY_MAX  && yDistance < lastMove.yDistance
                        && lastMove.yDistance - yDistance > Magic.GRAVITY_ODD / 3.0) {
                    // Special jump (water/edges/assume-ground), too small decrease.
                }
                else if (yDistDiffEx < Magic.GRAVITY_MIN && data.sfJumpPhase == 1 
                        && data.liftOffEnvelope != LiftOffEnvelope.NORMAL 
                        && lastMove.from.extraPropertiesValid && lastMove.from.inLiquid
                        && lastMove.yDistance < -Magic.GRAVITY_ODD / 2.0 && lastMove.yDistance > -Magic.GRAVITY_MAX - Magic.GRAVITY_SPAN
                        && yDistance < lastMove.yDistance - 0.001) {
                    // Odd decrease with water.
                }
                else if (lastMove.toIsValid && MagicAir.oddJunction(from, to, yDistance, yDistChange, yDistDiffEx, maxJumpGain, resetTo, thisMove, lastMove, data, cc)) {
                    // Several types of odd in-air moves, mostly with gravity near maximum, friction, medium change.
                }
                else {
                    // Violation.
                    vDistRelVL = true;
                }
            }
            else {
                // Violation.
                vDistRelVL = true;
            }
        } // else: yDistDiffEx <= 0.0
        else if (yDistance >= 0.0) { // Moved too short.
            if (!strictVdistRel || Math.abs(yDistDiffEx) <= Magic.GRAVITY_SPAN || vAllowedDistance <= 0.2) {
                // Allow jumping less high unless within "strict envelope".
                // TODO: Extreme anti-jump effects, perhaps.
            }
            else if (yDistance > 0.0 && lastMove.toIsValid && lastMove.yDistance > yDistance
                    && lastMove.yDistance - yDistance <= lastMove.yDistance / 4.0
                    && data.isVelocityJumpPhase()
                    ) {
                // Too strong decrease with velocity.
                // TODO: Observed when moving off water, might be confined by that.
            }
            else if (thisMove.headObstructed || lastMove.toIsValid && lastMove.headObstructed && lastMove.yDistance >= 0.0) {
                // Head is blocked, thus a shorter move.
            }
            else if (lastMove.toIsValid && MagicAir.oddJunction(from, to, yDistance, yDistChange, yDistDiffEx, maxJumpGain, resetTo, thisMove, lastMove, data, cc)) {
                // Several types of odd in-air moves, mostly with gravity near maximum, friction, medium change.
            }
            else {
                vDistRelVL = true;
            }
            // else: Allow moving up less. Note: possibility of low jump.
        } else { // if (yDistance < 0.0) // Rather too fast falling.
            if (yDistance < -3.0 && lastMove.yDistance < -3.0 && Math.abs(yDistDiffEx) < 5.0 * Magic.GRAVITY_MAX) {
                // Disregard not falling faster at some point (our constants don't match 100%).
            }
            else if (resetTo && (yDistDiffEx > -Magic.GRAVITY_SPAN || !fromOnGround && !thisMove.touchedGround && yDistChange >= 0.0)) {
                // Moving onto ground allows a shorter move.
                // TODO: Any lost-ground cases? 
            }
            else if (yDistance > lastMove.yDistance - Magic.GRAVITY_MAX - Magic.GRAVITY_SPAN && (resetTo || thisMove.touchedGround)) {
                // Mirrored case for yDistance > yAllowedDistance, hitting ground.
                // TODO: Needs more efficient structure.
            }
            else if (resetFrom && yDistance >= -0.5 && (yDistance > -0.31 || (resetTo || to.isAboveStairs()) && (lastMove.yDistance < 0.0))) {
                // Stairs and other cases moving off ground or ground-to-ground.
                // TODO: Margins !?
            }
            else if (data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID 
                    && data.sfJumpPhase == 1 && lastMove.toIsValid
                    && lastMove.from.inLiquid && !(lastMove.to.extraPropertiesValid && lastMove.to.inLiquid)
                    && !resetFrom && resetTo // TODO: There might be other cases (possibly wrong bounding box).
                    && lastMove.yDistance > 0.0 && lastMove.yDistance < 0.5 * Magic.GRAVITY_ODD
                    && yDistance < 0.0 && Math.abs(Math.abs(yDistance) - lastMove.yDistance) < Magic.GRAVITY_SPAN / 2.0
                    ) {
                // LIMIT_LIQUID, vDist inversion (!).
            }
            else if (yDistance <= 0.0 && yDistance > -Magic.GRAVITY_MAX - Magic.GRAVITY_SPAN 
                    && (thisMove.headObstructed || lastMove.toIsValid && lastMove.headObstructed && lastMove.yDistance >= 0.0)) {
                // Head was blocked, thus faster decrease than expected.
            }
            else if (lastMove.toIsValid && MagicAir.oddJunction(from, to, yDistance, yDistChange, yDistDiffEx, maxJumpGain, resetTo, thisMove, lastMove, data, cc)) {
                // Several types of odd in-air moves, mostly with gravity near maximum, friction, medium change.
            }
            else {
                // Violation.
                vDistRelVL = true;
            }
            // else Accept small aberrations !?
        }

        if (vDistRelVL) {
            if (data.getOrUseVerticalVelocity(yDistance) == null) {
                vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance - vAllowedDistance));
                tags.add("vdistrel");
            }
        }

        // Absolute y-distance to set back.
        if (yDistance > 0.0 && !data.isVelocityJumpPhase()) {
            // TODO: Maintain a value in data, adjusting to velocity?
            // TODO: LIMIT_JUMP
            final double vAllowedAbsoluteDistance = data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier);
            final double totalVDistViolation =  to.getY() - data.getSetBackY() - vAllowedAbsoluteDistance;
            if (totalVDistViolation > 0.0) {
                // Skip actually stepping up.
                if ((fromOnGround || thisMove.touchedGroundWorkaround || lastMove.touchedGround) 
                        && toOnGround && yDistance <= cc.sfStepHeight) {
                    // Ignore: Legitimate step.
                }
                // Skip if the player could step up by lostground_couldstep.
                else if (yDistance <= cc.sfStepHeight && thisMove.touchedGroundWorkaround && tags.contains("lostground_couldstep")) {
                    // Ignore: Envelope already checked.
                }
                // Teleport to in-air (PaperSpigot 1.7.10).
                else if (Magic.skipPaper(thisMove, lastMove, data)) {
                    // Tag already set above.
                }
                // Attempt to use velocity.
                else if (data.getOrUseVerticalVelocity(yDistance) == null) {
                    // Violation.
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, totalVDistViolation);
                    tags.add("vdistsb");
                }
            }
        }

        if (data.sfLowJump) {
            tags.add("lowjump");
        }

        // More in air checks.
        // TODO: move into the in air checking above !?
        if (!envelopeHack && !resetFrom && !resetTo) {
            // "On-air" checks (vertical, already use velocity if needed).
            vDistanceAboveLimit = Math.max(vDistanceAboveLimit, inAirChecks(now, from, to, hDistance, yDistance, lastMove, data, cc));
        }

        // Block 'step' with yDistance between step height and minJumpGain (vdistrel and vdistsb should catch the rest).
        // TODO: Model other cases of unexpectedly low 'jumping', such as using too few velocity?
        // (Actual step cheats are probably better detected by generalized patterns.)
        if (vDistanceAboveLimit <= 0D 
                && yDistance > cc.sfStepHeight && yDistance < data.liftOffEnvelope.getMinJumpGain(data.jumpAmplifier) 
                && !thisMove.headObstructed && !thisMove.from.resetCond && !thisMove.to.resetCond
                && (thisMove.from.onGround || thisMove.touchedGroundWorkaround) && thisMove.to.onGround 
                ) {
            // Exclude a lost-ground case.
            if (thisMove.touchedGroundWorkaround && lastMove.toIsValid && lastMove.yDistance <= 0.0
                    && yDistance + Math.abs(lastMove.yDistance) <= 2.0 * (maxJumpGain + 0.1)) {
                // TODO: Review: still needed?
            }
            else {
                // Potential violation.
                if (!player.hasPermission(Permissions.MOVING_SURVIVALFLY_STEP) && data.getOrUseVerticalVelocity(yDistance) == null) {
                    vDistanceAboveLimit = yDistance - cc.sfStepHeight;
                    tags.add("step");
                }
            }
        }

        // Air-stay-time.
        // TODO: max-phase only when from is not reset !?
        final int maxJumpPhase = data.liftOffEnvelope.getMaxJumpPhase(data.jumpAmplifier);
        if (!envelopeHack && data.sfJumpPhase > maxJumpPhase && !data.isVelocityJumpPhase()) {
            if (yDistance < 0) {
                // Ignore falling, and let accounting deal with it.
            }
            else if (resetFrom) {
                // Ignore bunny etc.
            }
            else {
                // Violation (Too high jumping or step).
                if (data.getOrUseVerticalVelocity(yDistance) == null) {
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.max(yDistance, 0.15));
                    tags.add("maxphase");
                }
            }
        }
        return new double[]{vAllowedDistance, vDistanceAboveLimit};
    }

    /**
     * vAllowedDistance with data having been reset after teleport/join/respawn.
     * 
     * @param thisMove
     * @param lastMove
     * @param maxJumpGain
     * @param jumpGainMargin
     * @param data
     * @param cc
     * @return
     */
    private double vAllowedDistanceNoData(final PlayerMoveData thisMove, final PlayerMoveData lastMove, 
            final double maxJumpGain, final double jumpGainMargin, 
            final MovingData data, final MovingConfig cc) {
        if (lastMove.valid) {
            tags.add("data_reset");
        }
        else {
            tags.add("data_missing");
        }
        double vAllowedDistance;
        if (thisMove.yDistance > -(Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN) && thisMove.yDistance < 0.0) {
            // Allow falling.
            vAllowedDistance = thisMove.yDistance;
        }
        else if (thisMove.from.onGround) {
            // Allow jumping.
            vAllowedDistance = maxJumpGain + jumpGainMargin;
            if (thisMove.to.onGround) {
                vAllowedDistance = Math.max(cc.sfStepHeight, vAllowedDistance);
            }
        }
        else if (Magic.skipPaper(thisMove, lastMove, data)) {
            // Double arithmetics, moving up after join/teleport/respawn.
            vAllowedDistance = Magic.PAPER_DIST;
            tags.add("skip_paper");
        }
        else {
            // Allow 0 y-distance once.
            vAllowedDistance = 0.0;
        }
        return vAllowedDistance;
    }

    /**
     * Extended in-air checks for vertical move: y-direction changes and accounting.
     * 
     * @param now
     * @param yDistance
     * @param data
     * @param cc
     * @return
     */
    private double inAirChecks(final long now, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc) {
        double vDistanceAboveLimit = 0;

        // y direction change detection.
        // TODO: Consider using accounting for y-change detection. <- Nope :).
        final boolean yDirChange = lastMove.toIsValid && lastMove.yDistance != yDistance && (yDistance <= 0.0 && lastMove.yDistance >= 0.0 || yDistance >= 0.0 && lastMove.yDistance <= 0.0 ); 
        if (yDirChange) {
            // yDirChange uses velocity if needed.
            vDistanceAboveLimit = yDirChange(from, to, yDistance, vDistanceAboveLimit, lastMove, data);
        }

        // Accounting support.
        if (cc.survivalFlyAccountingV) {
            // Currently only for "air" phases.
            // Vertical.
            if (yDirChange && lastMove.yDistance > 0) { // lastMove.toIsValid is checked above. 
                // Change to descending phase.
                data.vDistAcc.clear();
                // Allow adding 0.
                data.vDistAcc.add((float) yDistance);
            }
            else if (data.verVelUsed == null) { // Only skip if just used.
                // Here yDistance can be negative and positive.
                //                if (yDistance != 0.0) {
                data.vDistAcc.add((float) yDistance);
                final double accAboveLimit = verticalAccounting(yDistance, data.vDistAcc ,tags, "vacc" + (data.isVelocityJumpPhase() ? "dirty" : ""));
                if (accAboveLimit > vDistanceAboveLimit) {
                    if (data.getOrUseVerticalVelocity(yDistance) == null) {
                        vDistanceAboveLimit = accAboveLimit;
                    }
                }
                //                }
            }
            else {
                // TODO: Just to exclude source of error, might be redundant.
                data.vDistAcc.clear();
            }
        }
        return vDistanceAboveLimit;
    }

    /**
     * Demand that with time the values decrease.<br>
     * The ActionAccumulator instance must have 3 buckets, bucket 1 is checked against
     * bucket 2, 0 is ignored. [Vertical accounting: applies to both falling and jumping]<br>
     * NOTE: This just checks and adds to tags, no change to acc.
     * 
     * @param yDistance
     * @param acc
     * @param tags
     * @param tag Tag to be added in case of a violation of this sub-check.
     * @return A violation value > 0.001, to be interpreted like a moving violation.
     */
    private static final double verticalAccounting(final double yDistance, final ActionAccumulator acc, final ArrayList<String> tags, final String tag) {
        // TODO: Add air friction and do it per move anyway !?
        final int count0 = acc.bucketCount(0);
        if (count0 > 0) {
            final int count1 = acc.bucketCount(1);
            if (count1 > 0) {
                final int cap = acc.bucketCapacity();
                final float sc0;
                if (count0 == cap) {
                    sc0 = acc.bucketScore(0);
                } else {
                    // Catch extreme changes quick.
                    sc0 = acc.bucketScore(0) * (float) cap / (float) count0 - Magic.GRAVITY_VACC * (float) (cap - count0);
                }
                final float sc1 = acc.bucketScore(1);
                if (sc0 > sc1 - 3.0 * Magic.GRAVITY_VACC) {
                    // TODO: Velocity downwards fails here !!!
                    if (yDistance <= -1.05 && sc1 < -8.0 && sc0 < -8.0) { // (aDiff < Math.abs(yDistance) || sc2 < - 10.0f)) {
                        // High falling speeds may pass.
                        tags.add(tag + "grace");
                        return 0.0;
                    }
                    tags.add(tag);
                    return sc0 - (sc1 - 3.0 * Magic.GRAVITY_VACC);
                }
            }
        }
        return 0.0;
    }

    /**
     * Check on change of y direction. Needs last move data.
     * 
     * @param yDistance
     * @param vDistanceAboveLimit
     * @return vDistanceAboveLimit
     */
    private double yDirChange(final PlayerLocation from, final PlayerLocation to, final double yDistance, double vDistanceAboveLimit, final PlayerMoveData lastMove, final MovingData data) {
        // TODO: Does this account for velocity in a sufficient way?
        if (yDistance > 0) {
            // TODO: Clear active vertical velocity here ?
            // TODO: Demand consuming queued velocity for valid change (!).
            // Increase
            if (lastMove.touchedGround || lastMove.to.extraPropertiesValid && lastMove.to.resetCond) {
                tags.add("ychinc");
            }
            else {
                // Moving upwards after falling without having touched the ground.
                if (data.bunnyhopDelay < 9 && !((lastMove.touchedGround || lastMove.from.onGroundOrResetCond) && lastMove.yDistance == 0D) && data.getOrUseVerticalVelocity(yDistance) == null) {
                    // TODO: adjust limit for bunny-hop.
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance));
                    tags.add("ychincfly");
                }
                else {
                    tags.add("ychincair");
                }
            }
        }
        else {
            // Decrease
            tags.add("ychdec");
            // Detect low jumping.
            // TODO: sfDirty: Account for actual velocity (demands consuming queued for dir-change(!))!
            if (!data.sfLowJump && !data.sfNoLowJump && data.liftOffEnvelope == LiftOffEnvelope.NORMAL &&
                    lastMove.toIsValid && lastMove.yDistance > 0.0 && !data.isVelocityJumpPhase()) {
                final double setBackYDistance = from.getY() - data.getSetBackY();
                if (setBackYDistance > 0.0) {
                    // Only count it if the player has actually been jumping (higher than setback).
                    final Player player = from.getPlayer();
                    // Estimate of minimal jump height.
                    double estimate = 1.15;
                    if (data.jumpAmplifier > 0) {
                        // TODO: Could skip this.
                        estimate += 0.5 * aux.getJumpAmplifier(player);
                    }
                    if (setBackYDistance < estimate) {
                        // Low jump, further check if there might have been a reason for low jumping.
                        if (data.playerMoves.getCurrentMove().headObstructed || yDistance <= 0.0 
                                && lastMove.headObstructed && lastMove.yDistance >= 0.0) {
                            // Exempt.
                            tags.add("nolowjump_ceil");
                        }
                        else {
                            tags.add("lowjump_set");
                            data.sfLowJump = true;
                        }
                    }
                }
            } // (Low jump.)
        }
        return vDistanceAboveLimit;
    }

    /**
     * After-failure checks for horizontal distance.
     * 
     * buffers and velocity, also re-check hDist with permissions, if needed.
     * 
     * 
     * @param player
     * @param from
     * @param to
     * @param hAllowedDistance
     * @param hDistanceAboveLimit
     * @param sprinting
     * @param thisMove
     * @param lastMove
     * @param data
     * @param cc
     * @param skipPermChecks
     * @return hAllowedDistance, hDistanceAboveLimit, hFreedom
     */
    private double[] hDistAfterFailure(final Player player, final PlayerLocation from, final PlayerLocation to, double hAllowedDistance, double hDistanceAboveLimit, final boolean sprinting, final PlayerMoveData thisMove, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc, final boolean skipPermChecks) {

        // TODO: Still not entirely sure about this checking order.
        // TODO: Would quick returns make sense for hDistanceAfterFailure == 0.0?

        // Test bunny early, because it applies often and destroys as little as possible.
        hDistanceAboveLimit = bunnyHop(from, to, hAllowedDistance, hDistanceAboveLimit, sprinting, thisMove, lastMove, data, cc);

        // After failure permission checks ( + speed modifier + sneaking + blocking + speeding) and velocity (!).
        if (hDistanceAboveLimit > 0.0 && !skipPermChecks) {
            // TODO: Most cases these will not apply. Consider redesign to do these last or checking right away and skip here on some conditions.
            hAllowedDistance = setAllowedhDist(player, sprinting, thisMove, data, cc, true);
            hDistanceAboveLimit = thisMove.hDistance - hAllowedDistance;
            tags.add("permchecks");
        }

        // Check velocity.
        double hFreedom = 0.0; // Horizontal velocity used.
        if (hDistanceAboveLimit > 0.0) {
            // (hDistanceAboveLimit > 0.0)
            hFreedom = data.getHorizontalFreedom();
            if (hFreedom < hDistanceAboveLimit) {
                // Use queued velocity if possible.
                hFreedom += data.useHorizontalVelocity(hDistanceAboveLimit - hFreedom);
            }
            if (hFreedom > 0.0) {
                tags.add("hvel");
                hDistanceAboveLimit = Math.max(0.0, hDistanceAboveLimit - hFreedom);
            }
        }

        // After failure bunny (2nd).
        if (hDistanceAboveLimit > 0) {
            // (Could distinguish tags from above call).
            hDistanceAboveLimit = bunnyHop(from, to, hAllowedDistance, hDistanceAboveLimit, sprinting, thisMove, lastMove, data, cc);
        }

        // Horizontal buffer.
        // TODO: Consider to confine use to "not in air" and similar.
        if (hDistanceAboveLimit > 0.0 && data.sfHorizontalBuffer > 0.0) {
            // Handle buffer only if moving too far.
            // Consume buffer.
            tags.add("hbufuse");
            final double amount = Math.min(data.sfHorizontalBuffer, hDistanceAboveLimit);
            hDistanceAboveLimit -= amount;
            // Ensure we never end up below zero.
            data.sfHorizontalBuffer = Math.max(0.0, data.sfHorizontalBuffer - amount);
        }

        // Add the hspeed tag on violation.
        if (hDistanceAboveLimit > 0.0) {
            tags.add("hspeed");
        }
        return new double[]{hAllowedDistance, hDistanceAboveLimit, hFreedom};
    }

    /**
     * Test bunny hop / bunny fly. Does modify data only if 0.0 is returned.
     * @param from
     * @param to
     * @param hDistance
     * @param hAllowedDistance
     * @param hDistanceAboveLimit
     * @param yDistance
     * @param sprinting
     * @param data
     * @return hDistanceAboveLimit
     */
    private double bunnyHop(final PlayerLocation from, final PlayerLocation to, final double hAllowedDistance, double hDistanceAboveLimit, final boolean sprinting, final PlayerMoveData thisMove, final PlayerMoveData lastMove, final MovingData data, final MovingConfig cc) {
        // Check "bunny fly" here, to not fall over sprint resetting on the way.
        boolean allowHop = true;
        boolean double_bunny = false;

        // Pull down.
        final double hDistance = thisMove.hDistance;
        final double yDistance = thisMove.yDistance;
        // TODO: hDistanceBaseRef: Distinguish where to exclude sprint modifier?
        final double hDistanceBaseRef = thisMove.hAllowedDistanceBase;

        // TODO: A more state-machine like modeling (hop, slope, states, low-edge).

        // Fly phase.
        // TODO: Check which conditions might need resetting at lower speed (!).
        // Friction phase.
        if (lastMove.toIsValid && data.bunnyhopDelay > 0 && hDistance > hDistanceBaseRef) {
            // (lastHDist may be reset on commands.)
            allowHop = false; // Magic!
            final int hopTime = bunnyHopMax - data.bunnyhopDelay;

            // Increase buffer if hDistance is decreasing properly.
            if (lastMove.hDistance > hDistance) {
                final double hDistDiff = lastMove.hDistance - hDistance;

                // Bunny slope (downwards, directly after hop but before friction).
                if (data.bunnyhopDelay == bunnyHopMax - 1) {
                    // Ensure relative speed decrease vs. hop is met somehow.
                    if (hDistDiff >= 0.66 * (lastMove.hDistance - hDistanceBaseRef)) {
                        tags.add("bunnyslope");
                        hDistanceAboveLimit = 0.0;
                    }
                }
                else if (
                        hDistDiff >= lastMove.hDistance / bunnyDivFriction || hDistDiff >= hDistanceAboveLimit / 33.3 || 
                        hDistDiff >= (hDistance - hDistanceBaseRef) * (1.0 - Magic.FRICTION_MEDIUM_AIR)
                        ) {
                    // TODO: Confine friction by medium ?
                    // TODO: Also calculate an absolute (minimal) speed decrease over the whole time, at least max - count?
                    tags.add("bunnyfriction");
                    //if (hDistanceAboveLimit <= someThreshold) { // To be covered by bunnyslope.
                    // Speed must decrease by "a lot" at first, then by some minimal amount per event.
                    // TODO: Confine buffer to only be used during low jump phase !?
                    //if (!(data.toWasReset && thisMove.from.onGround && thisMove.to.onGround)) { // FISHY

                    // Allow the move.
                    hDistanceAboveLimit = 0.0;
                    if (data.bunnyhopDelay == 1 && !thisMove.to.onGround && !to.isResetCond()) {
                        // ... one move between toonground and liftoff remains for hbuf ...
                        data.bunnyhopDelay ++;
                        tags.add("bunnyfly(keep)");
                    }
                    else {
                        tags.add("bunnyfly(" + data.bunnyhopDelay +")");
                    }
                    //}
                    //}
                }
            }

            // 2x horizontal speed increase detection.
            if (!allowHop && hDistance - lastMove.hDistance >= hDistanceBaseRef * 0.5 && hopTime == 1) {
                if (lastMove.yDistance >= -Magic.GRAVITY_MAX / 2.0 && lastMove.yDistance <= 0.0 && yDistance >= 0.4 
                        && lastMove.touchedGround) {
                    // TODO: Confine to increasing set back y ?
                    tags.add(DOUBLE_BUNNY);
                    allowHop = double_bunny = true;
                }
            }

            // Allow hop for special cases.
            if (!allowHop && (thisMove.from.onGround || thisMove.touchedGroundWorkaround)) {
                // TODO: Better reset delay in this case ?
                if (data.bunnyhopDelay <= 6) {
                    // TODO: Confine further ?
                    tags.add("ediblebunny");
                    allowHop = true;
                }
                else if (lastMove.yDistance < 0.0 && thisMove.to.onGround && thisMove.yDistance == 0.0 
                        && Magic.fallAfterHeadObstructed(data, 2) 
                        && lastMove.hDistance > lastMove.hAllowedDistanceBase && lastMove.hDistance < 1.34 * lastMove.hAllowedDistanceBase
                        && thisMove.hDistance > lastMove.hDistance * 1.24
                        && thisMove.hDistance < lastMove.hDistance * 1.34
                        || yDistance >= 0.0 && thisMove.headObstructed // || to.isHeadObstructed()
                        ) {
                    // TODO: headObstructed: check always and set a flag in data + consider regain buffer?
                    tags.add("headbangbunny");
                    allowHop = true;
                    // TODO: Magic.
                    if (data.combinedMediumHValue / (double) data.combinedMediumHCount < 1.5) {
                        // TODO: Reset to 1 and min(allowed, actual) rather.
                        data.combinedMediumHCount = 0;
                        data.combinedMediumHValue = 0.0;
                        tags.add("bunny_no_hacc");
                    }
                }
            }

        }

        // Check hop (singular peak up to roughly two times the allowed distance).
        // TODO: Needs better modeling.
        if (allowHop && hDistance >= hDistanceBaseRef
                && (hDistance > (((!lastMove.toIsValid || lastMove.hDistance == 0.0 && lastMove.yDistance == 0.0) ? 1.11 : 1.314)) * hDistanceBaseRef) 
                && hDistance < 2.15 * hDistanceBaseRef
                // TODO: Walk speed (static or not) is not a good reference, switch to need normal/base speed instead.
                || (yDistance > from.getyOnGround() || hDistance < 2.6 * hDistanceBaseRef) && lastMove.toIsValid && hDistance > 1.314 * lastMove.hDistance && hDistance < 2.15 * lastMove.hDistance
                ) { // if (sprinting) {
            // TODO: Test bunny spike over all sorts of speeds + attributes.
            // TODO: Allow slightly higher speed on lost ground?
            // TODO: LiftOffEnvelope.allowBunny ?
            if (data.liftOffEnvelope == LiftOffEnvelope.NORMAL
                    && (!data.sfLowJump || data.sfNoLowJump)
                    // 0: Y-distance envelope.
                    && yDistance >= 0.0
                    && (
                            // 1: Normal jumping.
                            yDistance > 0.0 
                            && yDistance > data.liftOffEnvelope.getMinJumpGain(data.jumpAmplifier) - Magic.GRAVITY_SPAN
                            // 1: Too short with head obstructed.
                            || thisMove.headObstructed || lastMove.toIsValid && lastMove.headObstructed && lastMove.yDistance <= 0.0
                            // 1: Hop without y distance increase at moderate h-speed.
                            // TODO: 2nd below: demand next move to jump. Relate to stored past moves. 
                            || (cc.sfGroundHop || yDistance == 0.0 && !lastMove.touchedGroundWorkaround && !lastMove.from.onGround)
                            && hDistanceBaseRef > 0.0 && hDistance / hDistanceBaseRef < 1.35
                            )
                    // 0: Ground + jump phase conditions.
                    && (
                            // 1: Ordinary/obvious lift-off.
                            data.sfJumpPhase == 0 && thisMove.from.onGround 
                            // 1: Touched ground somehow.
                            || data.sfJumpPhase <= 1 && (thisMove.touchedGroundWorkaround || 
                                    lastMove.touchedGround && !lastMove.bunnyHop) 
                            // 1: Double bunny.
                            || double_bunny)
                    // 0: Don't allow bunny to run out of liquid.
                    && !from.isResetCond() && !to.isResetCond() // TODO: !to.isResetCond() should be reviewed.
                    ) {
                // TODO: Jump effect might allow more strictness. 
                // TODO: Expected minimum gain depends on last speed (!).
                // TODO: Speed effect affects hDistanceAboveLimit?
                data.bunnyhopDelay = bunnyHopMax;
                hDistanceAboveLimit = 0D;
                thisMove.bunnyHop = true;
                tags.add("bunnyhop");
            }
            else {
                tags.add("bunnyenv");
            }
        }

        return hDistanceAboveLimit;
    }

    /**
     * Legitimate move: increase horizontal buffer somehow.
     * @param hDistance
     * @param amount Positive amount.
     * @param data
     */
    private void hBufRegain(final double hDistance, final double amount, 
            final MovingData data, final MovingConfig cc) {
        /*
         * TODO: Consider different concepts: 
         * 			- full resetting with harder conditions.
         * 			- maximum regain amount.
         * 			- reset or regain only every x blocks h distance.
         */
        // TODO: Confine general conditions for buffer regain further (regain in air, whatever)?
        data.sfHorizontalBuffer = Math.min(cc.hBufMax, data.sfHorizontalBuffer + amount);
    }

    /**
     * Inside liquids vertical speed checking. setFrictionJumpPhase must be set
     * externally.
     * 
     * @param from
     * @param to
     * @param toOnGround
     * @param yDistance
     * @param data
     * @return vAllowedDistance, vDistanceAboveLimit
     */
    private double[] vDistLiquid(final PlayerLocation from, final PlayerLocation to, final boolean toOnGround, final double yDistance, final PlayerMoveData lastMove, final MovingData data) {
        data.sfNoLowJump = true;

        // Expected envelopes.
        final double baseSpeed = Magic.swimBaseSpeedV(); // TODO: Lava?
        final double yDistAbs = Math.abs(yDistance);

        // TODO: Later also cover things like a sudden stop.

        // Minimal speed.
        if (yDistAbs <= baseSpeed) {
            return new double[]{baseSpeed, 0.0};
        }

        // Friction envelope (allow any kind of slow down).
        final double frictDist = lastMove.toIsValid ? Math.abs(lastMove.yDistance) * data.lastFrictionVertical : baseSpeed; // Bounds differ with sign.
        if (lastMove.toIsValid) {
            if (lastMove.yDistance < 0.0 && yDistance < 0.0 && yDistAbs < frictDist + Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN) {
                return new double[]{-frictDist - Magic.GRAVITY_MAX - Magic.GRAVITY_SPAN, 0.0};
            }
            if (lastMove.yDistance > 0.0 && yDistance > 0.0 && yDistance < frictDist - Magic.GRAVITY_MIN) {
                return new double[]{frictDist - Magic.GRAVITY_MIN, 0.0};
            }
            // ("== 0.0" is covered by the minimal speed check above.)
        }

        // Workarounds for special cases.
        final Double wRes = MagicLiquid.liquidWorkarounds(from, to, baseSpeed, frictDist, lastMove, data);
        if (wRes != null) {
            return new double[]{wRes, 0.0};
        }

        // Try to use velocity for compensation.
        if (data.getOrUseVerticalVelocity(yDistance) != null) {
            return new double[]{yDistance, 0.0};
        }

        // At this point a violation.
        tags.add(yDistance < 0.0 ? "swimdown" : "swimup");
        final double vl1 = yDistAbs - baseSpeed;
        final double vl2 = Math.abs(yDistAbs - frictDist - (yDistance < 0.0 ? Magic.GRAVITY_MAX + Magic.GRAVITY_SPAN : Magic.GRAVITY_MIN));
        if (vl1 <= vl2) {
            return new double[]{yDistance < 0.0 ? -baseSpeed : baseSpeed, vl1};
        } else {
            return new double[]{yDistance < 0.0 ? -frictDist - Magic.GRAVITY_MAX - Magic.GRAVITY_SPAN : frictDist - Magic.GRAVITY_MIN, vl2};
        }
    }

    /**
     * On-climbable vertical distance checking.
     * @param from
     * @param fromOnGround
     * @param toOnGround
     * @param lastMove 
     * @param thisMove 
     * @param yDistance
     * @param data
     * @return vDistanceAboveLimit
     */
    private double vDistClimbable(final Player player, final PlayerLocation from, final PlayerLocation to,
            final boolean fromOnGround, final boolean toOnGround, 
            final PlayerMoveData thisMove, final PlayerMoveData lastMove, 
            final double yDistance, final MovingData data) {
        double vDistanceAboveLimit = 0.0;
        data.sfNoLowJump = true;

        // Clear active horizontal velocity.
        data.clearActiveHorVel();
        // TODO: Might not be able to ignore vertical velocity if moving off climbable (!).

        // TODO: bring in in-medium accounting.
        //    	// TODO: make these extra checks to the jumpphase thing ?
        //    	if (fromOnGround) vAllowedDistance = climbSpeed + 0.3;
        //    	else vAllowedDistance = climbSpeed;
        //    	vDistanceAboveLimit = Math.abs(yDistance) - vAllowedDistance;
        //    	if (vDistanceAboveLimit > 0) tags.add("vclimb");
        final double jumpHeight = 1.35 + (data.jumpAmplifier > 0 ? (0.6 + data.jumpAmplifier - 1.0) : 0.0);
        // TODO: ladders are ground !
        // TODO: yDistance < 0.0 ?
        final double maxSpeed = yDistance < 0.0 ? Magic.climbSpeedDescend : Magic.climbSpeedAscend;
        if (Math.abs(yDistance) > maxSpeed) {
            if (from.isOnGround(jumpHeight, 0D, 0D, BlockProperties.F_CLIMBABLE)) {
                if (yDistance > data.liftOffEnvelope.getMaxJumpGain(data.jumpAmplifier)+ 0.1) {
                    tags.add("climbstep");
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance) - maxSpeed);
                }
            } else {
                tags.add("climbspeed");
                vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance) - maxSpeed);
            }
        }
        if (yDistance > 0) {
            if (!data.playerMoves.getCurrentMove().touchedGround) {
                // Check if player may climb up.
                // (This does exclude ladders.)
                if (!from.canClimbUp(jumpHeight)) {
                    tags.add("climbdetached");
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, yDistance);
                }
            }
        }
        // Do allow friction with velocity.
        if (vDistanceAboveLimit > 0.0 && thisMove.yDistance > 0.0 
                && lastMove.yDistance - (Magic.GRAVITY_MAX + Magic.GRAVITY_MIN) / 2.0 > thisMove.yDistance) {
            // TODO: Actual friction or limit by absolute y-distance?
            // TODO: Looks like it's only a problem when on ground?
            vDistanceAboveLimit = 0.0;
            tags.add("vfrict_climb");
        }
        // Do allow vertical velocity.
        // TODO: Looks like less velocity is used here (normal hitting 0.361 of 0.462).
        if (vDistanceAboveLimit > 0.0 && data.getOrUseVerticalVelocity(yDistance) != null) {
            vDistanceAboveLimit = 0.0;
        }
        return vDistanceAboveLimit;
    }

    /**
     * In-web vertical distance checking.
     * @param player
     * @param from
     * @param to
     * @param toOnGround
     * @param hDistanceAboveLimit
     * @param yDistance
     * @param now
     * @param data
     * @param cc
     * @return vAllowedDistance, vDistanceAboveLimit
     */
    private double[] vDistWeb(final Player player, final PlayerMoveData thisMove, final boolean toOnGround, final double hDistanceAboveLimit, final long now, final MovingData data, final MovingConfig cc) {
        final double yDistance = thisMove.yDistance;
        double vAllowedDistance = 0.0;
        double vDistanceAboveLimit = 0.0;
        data.sfNoLowJump = true;
        data.jumpAmplifier = 0; // TODO: later maybe fetch.
        // Very simple: force players to descend or stay.
        if (yDistance >= 0.0) {
            if (toOnGround && yDistance <= 0.5) {
                // Step up. Note: Does not take into account jump effect on purpose.
                vAllowedDistance = yDistance;
                if (yDistance > 0.0) {
                    tags.add("web_step");
                }
            }
            else {
                // TODO: Could prevent not moving down if not on ground (or on ladder or in liquid?).
                vAllowedDistance = thisMove.from.onGround ? 0.1D : 0;
            }
            vDistanceAboveLimit = yDistance - vAllowedDistance;
        }
        else {
            // Descending in web.
            // TODO: Implement something (at least for being in web with the feet or block above)?
        }
        if (cc.survivalFlyCobwebHack && vDistanceAboveLimit > 0.0 && hDistanceAboveLimit <= 0.0) {
            // TODO: Seemed fixed at first by CB/MC, but still does occur due to jumping. 
            if (hackCobweb(player, data, thisMove, now, vDistanceAboveLimit)) {
                return new double[]{Double.MIN_VALUE, Double.MIN_VALUE};
            }
        }
        // TODO: Prevent too fast moving down ?
        if (vDistanceAboveLimit > 0.0) {
            tags.add("vweb");
        }
        return new double[]{vAllowedDistance, vDistanceAboveLimit};
    }

    /**
     * Violation handling put here to have less code for the frequent processing of check.
     * @param now
     * @param result
     * @param player
     * @param from
     * @param to
     * @param data
     * @param cc
     * @return
     */
    private Location handleViolation(final long now, final double result, final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc)
    {
        // Increment violation level.
        data.survivalFlyVL += result;
        data.sfVLTime = now;
        final ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, result, cc.survivalFlyActions);
        if (vd.needsParameters()) {
            vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", from.getX(), from.getY(), from.getZ()));
            vd.setParameter(ParameterName.LOCATION_TO, String.format(Locale.US, "%.2f, %.2f, %.2f", to.getX(), to.getY(), to.getZ()));
            vd.setParameter(ParameterName.DISTANCE, String.format(Locale.US, "%.2f", TrigUtil.distance(from, to)));
            vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
        }
        // Some resetting is done in MovingListener.
        if (executeActions(vd).willCancel()) {
            // Set-back + view direction of to (more smooth).
            return data.getSetBack(to);
        }
        else {
            data.clearAccounting();
            data.sfJumpPhase = 0;
            // Cancelled by other plugin, or no cancel set by configuration.
            return null;
        }
    }

    /**
     * Hover violations have to be handled in this check, because they are handled as SurvivalFly violations (needs executeActions).
     * @param player
     * @param loc
     * @param cc
     * @param data
     */
    public final void handleHoverViolation(final Player player, final Location loc, final MovingConfig cc, final MovingData data) {
        data.survivalFlyVL += cc.sfHoverViolation;

        // TODO: Extra options for set-back / kick, like vl?
        data.sfVLTime = System.currentTimeMillis();
        final ViolationData vd = new ViolationData(this, player, data.survivalFlyVL, cc.sfHoverViolation, cc.survivalFlyActions);
        if (vd.needsParameters()) {
            vd.setParameter(ParameterName.LOCATION_FROM, String.format(Locale.US, "%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ()));
            vd.setParameter(ParameterName.LOCATION_TO, "(HOVER)");
            vd.setParameter(ParameterName.DISTANCE, "0.0(HOVER)");
            vd.setParameter(ParameterName.TAGS, "hover");
        }
        if (executeActions(vd).willCancel()) {
            // Set-back or kick.
            if (data.hasSetBack()) {
                final Location newTo = data.getSetBack(loc);
                data.prepareSetBack(newTo);
                player.teleport(newTo, TeleportCause.PLUGIN);
            }
            else {
                // Solve by extra actions ? Special case (probably never happens)?
                player.kickPlayer("Hovering?");
            }
        }
        else {
            // Ignore.
        }
    }

    /**
     * Allow accumulating some vls and silently set the player back.
     * 
     * @param player
     * @param data
     * @param cc
     * @param to
     * @param now
     * @param vDistanceAboveLimit
     * @return If to silently set back.
     */
    private boolean hackCobweb(final Player player, final MovingData data, 
            final PlayerMoveData thisMove, final long now, final double vDistanceAboveLimit) {
        if (now - data.sfCobwebTime > 3000) {
            data.sfCobwebTime = now;
            data.sfCobwebVL = vDistanceAboveLimit * 100D;
        } else {
            data.sfCobwebVL += vDistanceAboveLimit * 100D;
        }
        if (data.sfCobwebVL < 550) { // Totally random !
            // Silently set back.
            if (!data.hasSetBack()) { // TODO: Assume redundant.
                data.setSetBack(player.getLocation(useLoc)); // ? check moment of call.
                useLoc.setWorld(null);
            }
            data.sfJumpPhase = 0;
            return true;
        } else {
            return false;
        }
    }

    /**
     * This is set with PlayerToggleSneak, to be able to distinguish players that are really sneaking from players that are set sneaking by a plugin. 
     * @param player + ")"
     * @param sneaking
     */
    public void setReallySneaking(final Player player, final boolean sneaking) {
        if (sneaking) reallySneaking.add(player.getName());
        else reallySneaking.remove(player.getName());
    }

    /**
     * Debug output.
     * @param player
     * @param to
     * @param data
     * @param cc
     * @param hDistance
     * @param hAllowedDistance
     * @param hFreedom
     * @param yDistance
     * @param vAllowedDistance
     * @param fromOnGround
     * @param resetFrom
     * @param toOnGround
     * @param resetTo
     */
    private void outputDebug(final Player player, final PlayerLocation to, final MovingData data, final MovingConfig cc, 
            final double hDistance, final double hAllowedDistance, final double hFreedom, final double yDistance, final double vAllowedDistance,
            final boolean fromOnGround, final boolean resetFrom, final boolean toOnGround, final boolean resetTo) {
        // TODO: Show player name once (!)
        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final PlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        final StringBuilder builder = new StringBuilder(500);
        builder.append(CheckUtils.getLogMessagePrefix(player, type));
        final String hBuf = (data.sfHorizontalBuffer < 1.0 ? ((" hbuf=" + StringUtil.fdec3.format(data.sfHorizontalBuffer))) : "");
        final String lostSprint = (data.lostSprintCount > 0 ? (" lostSprint=" + data.lostSprintCount) : "");
        final String hVelUsed = hFreedom > 0 ? " hVelUsed=" + StringUtil.fdec3.format(hFreedom) : "";
        builder.append("\nonground: " + (thisMove.headObstructed ? "(head obstr.) " : "") + (thisMove.touchedGroundWorkaround ? "(touched ground) " : "") + (fromOnGround ? "onground -> " : (resetFrom ? "resetcond -> " : "--- -> ")) + (toOnGround ? "onground" : (resetTo ? "resetcond" : "---")) + ", jumpphase: " + data.sfJumpPhase + ", liftoff: " + data.liftOffEnvelope.name() + "(" + data.insideMediumCount + ")");
        final String dHDist = lastMove.toIsValid ? " (" + StringUtil.formatDiff(hDistance, lastMove.hDistance) + ")" : "";
        final String dYDist = lastMove.toIsValid ? " (" + StringUtil.formatDiff(yDistance, lastMove.yDistance)+ ")" : "";
        builder.append("\n" + " hDist: " + StringUtil.fdec3.format(hDistance) + dHDist + " / " +  StringUtil.fdec3.format(hAllowedDistance) + hBuf + lostSprint + hVelUsed + " , vDist: " + StringUtil.fdec3.format(yDistance) + dYDist + " / " + StringUtil.fdec3.format(vAllowedDistance) + " , sby=" + (data.hasSetBack() ? (data.getSetBackY() + " (" + StringUtil.fdec3.format(to.getY() - data.getSetBackY()) + " / " + data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier) + ")") : "?"));
        if (lastMove.toIsValid) {
            builder.append(" , fdsq: " + StringUtil.fdec3.format(thisMove.distanceSquared / lastMove.distanceSquared));
        }
        if (data.verVelUsed != null) {
            builder.append(" , vVelUsed: " + data.verVelUsed + " ");
        }
        data.addVerticalVelocity(builder);
        //		if (data.horizontalVelocityCounter > 0 || data.horizontalFreedom >= 0.001) {
        //			builder.append("\n" + player.getName() + " horizontal freedom: " +  StringUtil.fdec3.format(data.horizontalFreedom) + " (counter=" + data.horizontalVelocityCounter +"/used="+data.horizontalVelocityUsed);
        //		}
        data.addHorizontalVelocity(builder);
        if (!resetFrom && !resetTo) {
            if (cc.survivalFlyAccountingV && data.vDistAcc.count() > data.vDistAcc.bucketCapacity()) builder.append("\n" + " vacc=" + data.vDistAcc.toInformalString());
        }
        if (player.isSleeping()) {
            tags.add("sleeping");
        }
        if (player.getFoodLevel() <= 5 && player.isSprinting()) {
            // Exception: does not take into account latency.
            tags.add("lowfoodsprint");
        }
        if (Bridge1_9.isWearingElytra(player)) {
            // Just wearing (not isGliding).
            tags.add("elytra_off");
        }
        if (!tags.isEmpty()) {
            builder.append("\n" + " tags: " + StringUtil.join(tags, "+"));
        }
        if (!justUsedWorkarounds.isEmpty()) {
            builder.append("\n" + " workarounds: " + StringUtil.join(justUsedWorkarounds, "+"));
        }
        builder.append("\n");
        //		builder.append(data.stats.getStatsStr(false));
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, builder.toString());
    }

    private void logPostViolationTags(final Player player) {
        debug(player, "SurvivalFly Post violation handling tag update:\n" + StringUtil.join(tags, "+"));
    }

}
