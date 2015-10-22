package fr.neatmonster.nocheatplus.checks.moving;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.compat.BridgeEnchant;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ActionAccumulator;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * The counterpart to the CreativeFly check. People that are not allowed to fly get checked by this. It will try to
 * identify when they are jumping, check if they aren't jumping too high or far, check if they aren't moving too fast on
 * normal ground, while sprinting, sneaking, swimming, etc.
 */
public class SurvivalFly extends Check {

    // Tags
    private static final String DOUBLE_BUNNY = "doublebunny";

    // Horizontal speeds/modifiers.
    public static final double walkSpeed            = 0.221D;

    public static final double modSneak             = 0.13D / walkSpeed;
    //    public static final double modSprint            = 0.29D / walkSpeed; // TODO: without bunny  0.29 / practical is 0.35

    public static final double modBlock             = 0.16D / walkSpeed;
    public static final double modSwim              = 0.115D / walkSpeed;
    public static final double[] modDepthStrider    = new double[] {
        1.0,
        0.1645 / modSwim / walkSpeed,
        0.1995 / modSwim / walkSpeed,
        1.0 / modSwim, // Results in walkspeed.
    };

    public static final double modWeb               = 0.105D / walkSpeed; // TODO: walkingSpeed * 0.15D; <- does not work

    public static final double modIce                 = 2.5D; // 

    /** Faster moving down stream (water mainly). */
    public static final double modDownStream	= 0.19 / (walkSpeed * modSwim);

    /** Maximal horizontal buffer. It can be higher, but normal resetting should keep this limit. */
    public static final double hBufMax			= 1.0;

    // Vertical speeds/modifiers. 
    public static final double climbSpeed		= walkSpeed * 1.3; // TODO: Check if the factor is needed!  

    // Other.
    /** Bunny-hop delay. */
    private static final int   bunnyHopMax = 10;
    /** Divisor vs. last hDist for minimum slow down. */
    private static final double bunnyDivFriction = 160.0; // Rather in-air, blocks would differ by friction.

    // Gravity.
    public static final double GRAVITY_MAX = 0.0834;
    public static final double GRAVITY_MIN = 0.0624; // TODO: Special cases go down to 0.05.
    public static final double GRAVITY_SPAN = GRAVITY_MAX - GRAVITY_MIN;
    public static final double GRAVITY_ODD = 0.05; // 19; // TODO: This should probably be min. / cleanup.
    /** Assumed minimal average decrease per move, suitable for regarding 3 moves. */
    public static final float GRAVITY_VACC = (float) (GRAVITY_MIN * 0.6);

    // Friction factor by medium.
    public static final double FRICTION_MEDIUM_AIR = 0.98; // TODO: Check
    public static final double FRICTION_MEDIUM_LIQUID = 0.89; // Rough estimate for horizontal move sprint-jump into water.

    // TODO: Friction by block to walk on (horizontal only, possibly to be in BlockProperties rather).

    /** To join some tags with moving check violations. */
    private final ArrayList<String> tags = new ArrayList<String>(15);


    private final Set<String> reallySneaking = new HashSet<String>(30);

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Instantiates a new survival fly check.
     */
    public SurvivalFly() {
        super(CheckType.MOVING_SURVIVALFLY);
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
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final boolean isSamePos, final MovingData data, final MovingConfig cc, final long now) {
        tags.clear();

        // Calculate some distances.
        final double xDistance, yDistance, zDistance, hDistance;
        final boolean hasHdist;
        if (isSamePos) {
            // TODO: Could run a completely different check here (roughly none :p).
            xDistance = yDistance = zDistance = hDistance = 0.0;
            hasHdist = false;
        } else {
            xDistance = to.getX() - from.getX();
            yDistance = to.getY() - from.getY();
            zDistance = to.getZ() - from.getZ();
            if (xDistance == 0.0 && zDistance == 0.0) {
                hDistance = 0.0;
                hasHdist = false;
            } else {
                hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);
                hasHdist = true;
            }
        }

        // Ensure we have a set-back location set, plus allow moving from upwards with respawn/login.
        if (!data.hasSetBack()) {
            data.setSetBack(from);
        }
        else if (data.joinOrRespawn && from.getY() > data.getSetBackY() && 
                TrigUtil.isSamePos(from.getX(), from.getZ(), data.getSetBackX(), data.getSetBackZ()) &&
                (from.isOnGround() || from.isResetCond())) {
            // TODO: Move most to a method?
            // TODO: Is a margin needed for from.isOnGround()? [bukkitapionly]
            if (data.debug) {
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " SurvivalFly\nAdjust set-back after join/respawn: " + from.getLocation());
            }
            data.setSetBack(from);
            data.resetPositions(from);
        }

        // Set some flags.
        final boolean fromOnGround = from.isOnGround();
        final boolean toOnGround = to.isOnGround();
        final boolean resetTo = toOnGround || to.isResetCond();

        // Determine if the player is actually sprinting.
        final boolean sprinting;
        if (data.lostSprintCount > 0) {
            // Sprint got toggled off, though the client is still (legitimately) moving at sprinting speed.
            // NOTE: This could extend the "sprinting grace" period, theoretically, until on ground.
            if (resetTo && (fromOnGround || from.isResetCond()) || hDistance <= walkSpeed) {
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
                if (data.lostSprintCount < 3 && to.isOnGround() || to.isResetCond()) {
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
        final double walkSpeed = SurvivalFly.walkSpeed * ((double) data.walkSpeed / 0.2);

        setNextFriction(from, to, data, cc);

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
            if (data.lastHDist != Double.MAX_VALUE && data.lastHDist > 0.0 && data.lastYDist < -0.3) {
                // Note that to is not on ground either.
                resetFrom = lostGroundStill(player, from, to, hDistance, yDistance, sprinting, data, cc);
            } else {
                resetFrom = false;
            }
        }
        else {
            // "Lost ground" workaround.
            // TODO: More refined conditions possible ?
            // TODO: Consider if (!resetTo) ?
            // Check lost-ground workarounds.
            resetFrom = lostGround(player, from, to, hDistance, yDistance, sprinting, data, cc);
            // Note: if not setting resetFrom, other places have to check assumeGround...
        }

        if (data.noFallAssumeGround) {
            // Lost ground workaround has just been applied, check resetting of the dirty flag.
            data.resetVelocityJumpPhase();
        }

        // Renew the "dirty"-flag (in-air phase affected by velocity).
        if (data.isVelocityJumpPhase() || data.resetVelocityJumpPhase()) {
            // (Reset is done after checks run.) 
            tags.add("dirty");
        }

        //////////////////////
        // Horizontal move.
        //////////////////////

        // TODO: Account for lift-off medium / if in air [i.e. account for medium + friction]?

        // Alter some data / flags.
        data.bunnyhopDelay--; // TODO: Design to do the changing at the bottom? [if change: check limits in bunnyHop(...)]

        // Set flag for swimming with the flowing direction of liquid.
        final boolean downStream = hDistance > walkSpeed * modSwim && from.isInLiquid() && from.isDownStream(xDistance, zDistance);

        // Handle ice.
        // TODO: Re-model ice stuff and other (e.g. general thing: ground-modifier + reset conditions).
        if (from.isOnIce() || to.isOnIce()) {
            data.sfOnIce = 20;
        }
        else if (data.sfOnIce > 0) {
            // TODO: Here some friction might apply, could become a general thing with bunny and other.
            // TODO: Other reset conditions.
            data.sfOnIce--;
        }

        double hAllowedDistance = 0.0, hDistanceAboveLimit = 0.0, hFreedom = 0.0;
        if (hasHdist) {
            // Check allowed vs. taken horizontal distance.
            // Get the allowed distance.
            hAllowedDistance = getAllowedhDist(player, from, to, sprinting, downStream, hDistance, walkSpeed, data, cc, false);

            // Judge if horizontal speed is above limit.
            hDistanceAboveLimit = hDistance - hAllowedDistance;

            // Velocity, buffers and after failure checks.
            if (hDistanceAboveLimit > 0) {
                // TODO: Move more of the workarounds (buffer, bunny, ...) into this method.
                final double[] res = hDistAfterFailure(player, from, to, walkSpeed, hAllowedDistance, hDistance, hDistanceAboveLimit, yDistance, sprinting, downStream, data, cc, false);
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

            // Prevent players from walking on a liquid in a too simple way.
            // TODO: Find something more effective against more smart methods (limitjump helps already).
            // TODO: yDistance == 0D <- should there not be a tolerance +- or 0...x ?
            // TODO: Complete re-modeling.
            if (hDistanceAboveLimit <= 0D && hDistance > 0.1D && yDistance == 0D && data.lastYDist == 0D && !toOnGround && !fromOnGround 
                    && BlockProperties.isLiquid(to.getTypeId()) && BlockProperties.isLiquid(from.getTypeId())
                    && !from.isHeadObstructed() && !to.isHeadObstructed() // TODO: Might decrease margin here.
                    ) {
                // TODO: Relative hdistance.
                // TODO: Might check actual bounds (collidesBlock). Might implement + use BlockProperties.getCorrectedBounds or getSomeHeight.
                hDistanceAboveLimit = Math.max(hDistanceAboveLimit, hDistance);
                tags.add("waterwalk");
            }

            // Prevent players from sprinting if they're moving backwards (allow buffers to cover up !?).
            if (sprinting && data.lostSprintCount == 0 && !cc.assumeSprint && hDistance > walkSpeed && !data.hasActiveHorVel()) {
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
        }


        //////////////////////////
        // Vertical move.
        //////////////////////////

        // Calculate the vertical speed limit based on the current jump phase.
        double vAllowedDistance = 0, vDistanceAboveLimit = 0;
        // Distinguish certain media.
        if (yDistance >= 0.0 && yDistance <= cc.sfStepHeight && toOnGround && fromOnGround ) {
            // Wild-card allow step height from ground to ground.
            // TODO: Which of (fromOnGround || data.noFallAssumeGround || data.lastYDist < 0.0)?
            vAllowedDistance = cc.sfStepHeight;
        }
        else if (from.isInWeb()) {
            // TODO: Further confine conditions.
            final double[] res = vDistWeb(player, from, to, toOnGround, hDistanceAboveLimit, yDistance, now,data,cc);
            vAllowedDistance = res[0];
            vDistanceAboveLimit = res[1];
            if (res[0] == Double.MIN_VALUE && res[1] == Double.MIN_VALUE) {
                // Silent set-back.
                if (data.debug) {
                    tags.add("silentsbcobweb");
                    outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
                }
                return data.getSetBack(to);
            }
        }
        else if (from.isOnClimbable()) {
            // Ladder types.
            vDistanceAboveLimit = vDistClimbable(player, from, fromOnGround, toOnGround, yDistance, data);
        }
        else if (from.isInLiquid()) { // && (Math.abs(yDistance) > 0.2 || to.isInLiquid())) {
            // Swimming...
            final double[] res = vDistLiquid(from, to, toOnGround, yDistance, data);
            vAllowedDistance = res[0];
            vDistanceAboveLimit = res[1];
            if (vDistanceAboveLimit <= 0.0 && yDistance > 0.0 && Math.abs(yDistance) > swimBaseSpeedV()) {
                data.setFrictionJumpPhase();
            }
        }
        else {
            final double[] res = vDistAir(now, player, from, fromOnGround, resetFrom, to, toOnGround, resetTo, hDistanceAboveLimit, yDistance, data, cc);
            vAllowedDistance = res[0];
            vDistanceAboveLimit = res[1];
        }

        // TODO: on ground -> on ground improvements

        // Debug output.
        final int tagsLength;
        if (data.debug) {
            outputDebug(player, to, data, cc, hDistance, hAllowedDistance, hFreedom, yDistance, vAllowedDistance, fromOnGround, resetFrom, toOnGround, resetTo);
            tagsLength = tags.size();
        } else {
            tagsLength = 0; // JIT vs. IDE.
        }

        // Handle violations.
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
            if (hDistanceAboveLimit < 0.0  && result <= 0.0 && !isSamePos && data.sfHorizontalBuffer < hBufMax) {
                // TODO: max min other conditions ?
                hBufRegain(hDistance, Math.min(0.2, Math.abs(hDistanceAboveLimit)), data);
            }
        }

        //  Set data for normal move or violation without cancel (cancel would have returned above)

        // Check lift-off medium.
        // TODO: Web before liquid? Climbable?
        // TODO: Web might be NO_JUMP !
        // TODO: isNextToGround(0.15, 0.4) allows a little much (yMargin), but reduces false positives.
        // TODO: nextToGround: Shortcut with block-flags ?
        if (to.isInLiquid()) {
            if (fromOnGround && !toOnGround 
                    && data.liftOffEnvelope == LiftOffEnvelope.NORMAL
                    && data.sfJumpPhase <= 0  && !from.isInLiquid()) {
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
        else if (to.isInWeb()) {
            data.liftOffEnvelope = LiftOffEnvelope.NO_JUMP; // TODO: Test.
        }
        else if (resetTo) {
            // TODO: This might allow jumping on vines etc., but should do for the moment.
            data.liftOffEnvelope = LiftOffEnvelope.NORMAL;
        }
        else if (from.isInLiquid()) {
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
        else if (from.isInWeb()) {
            data.liftOffEnvelope = LiftOffEnvelope.NO_JUMP; // TODO: Test.
        }
        else if (resetFrom || data.noFallAssumeGround) {
            // TODO: Where exactly to put noFallAssumeGround ?
            data.liftOffEnvelope = LiftOffEnvelope.NORMAL;
        }
        else {
            // Keep medium.
            // TODO: Is above stairs ?
        }

        //        // Invalidation of vertical velocity.
        //        // TODO: This invalidation is wrong in case of already jumped higher (can not be repaired?).
        //        if (yDistance <= 0 && data.sfLastYDist > 0 && data.sfLastYDist != Double.MAX_VALUE 
        //                && data.invalidateVerVelGrace(cc.velocityGraceTicks, false)) {
        //            // (Only prevent counting further up, leaves the freedom.)
        //            tags.add("cap_vvel"); // TODO: Test / validate by logs.
        //        }

        // Apply reset conditions.
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
            data.sfJumpPhase++;
            if (to.getY() < 0.0 && cc.sfSetBackPolicyVoid) {
                data.setSetBack(to);
            }
        }

        // Horizontal velocity invalidation.
        if (hDistance <= (cc.velocityStrictInvalidation ? hAllowedDistance : hAllowedDistance / 2.0)) {
            // TODO: Should there be other side conditions?
            // Invalidate used horizontal velocity.
            //        	NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "*** INVALIDATE ON SPEED");
            data.clearActiveHorVel();
            //          if (data.horizontalVelocityUsed > cc.velocityGraceTicks) {
            //        	data.horizontalFreedom = 0;
            //        	data.horizontalVelocityCounter = 0;
            //        	data.horizontalVelocityUsed = 0;
            //        }
        }
        // Adjust data.
        data.lastHDist = hDistance;
        data.lastYDist = yDistance;
        data.toWasReset = resetTo || data.noFallAssumeGround;
        data.fromWasReset = resetFrom || data.noFallAssumeGround;
        data.lastFrictionHorizontal = data.nextFrictionHorizontal;
        data.lastFrictionVertical = data.nextFrictionVertical;
        if (data.debug && tags.size() > tagsLength) {
            logPostViolationTags(player);
        }
        return null;
    }

    /**
     * Set data.nextFriction according to media.
     * @param from
     * @param to
     * @param data
     * @param cc
     */
    private void setNextFriction(final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {
        // NOTE: Other methods might still override nextFriction to 1.0 due to burst/lift-off envelope.
        // TODO: Other media / medium transitions / friction by block.
        if (from.isInWeb() || to.isInWeb()) {
            data.nextFrictionHorizontal = data.nextFrictionVertical = 0.0;
        }
        else if (from.isOnClimbable() || to.isOnClimbable()) {
            // TODO: Not sure about horizontal (!).
            data.nextFrictionHorizontal = data.nextFrictionVertical = 0.0;
        }
        else if (from.isInLiquid() && to.isInLiquid()) {
            // TODO: Lava ?
            data.nextFrictionHorizontal = data.nextFrictionVertical = FRICTION_MEDIUM_LIQUID;
        }
        // TODO: consider setting minimum friction last (air), do add ground friction.
        else if (!from.isOnGround() && ! to.isOnGround()) {
            data.nextFrictionHorizontal = data.nextFrictionVertical = FRICTION_MEDIUM_AIR;
        }
        else {
            // TODO: Friction for walking on blocks (!).
        }

    }

    /**
     * Return hAllowedDistance, not exact, check permissions as far as
     * necessary, if flag is set to check them.
     * 
     * @param player
     * @param sprinting
     * @param hDistance
     * @param hAllowedDistance
     * @param data
     * @param cc
     * @param checkPermissions If to check permissions, allowing to speed up a little bit. Only set to true after having failed with it set to false.
     * @return
     */
    private double getAllowedhDist(final Player player, final PlayerLocation from, final PlayerLocation to, final boolean sprinting, final boolean downStream, final double hDistance, final double walkSpeed, final MovingData data, final MovingConfig cc, boolean checkPermissions)
    {
        // TODO: Optimize for double checking?
        double hAllowedDistance = 0D;

        final boolean sfDirty = data.isVelocityJumpPhase();
        double friction = data.lastFrictionHorizontal; // Friction to use with this move.
        // TODO: sfDirty: Better friction/envelope-based.
        if (from.isInWeb()) {
            data.sfOnIce = 0;
            // TODO: if (from.isOnIce()) <- makes it even slower !
            // Does include sprinting by now (would need other accounting methods).
            hAllowedDistance = modWeb * walkSpeed * cc.survivalFlyWalkingSpeed / 100D;
            friction = 0.0; // Ensure friction can't be used to speed.
        }
        else if (from.isInLiquid() && to.isInLiquid()) {
            // Check all liquids (lava might demand even slower speed though).
            // TODO: Test how to go with only checking from (less dolphins).
            // TODO: Sneaking and blocking applies to when in water !
            hAllowedDistance = modSwim * walkSpeed * cc.survivalFlySwimmingSpeed / 100D;
            final int level = BridgeEnchant.getDepthStriderLevel(player);
            if (level > 0) {
                // The hard way.
                hAllowedDistance *= modDepthStrider[level];
                if (sprinting) {
                    hAllowedDistance *= data.multSprinting;
                }
            }
            // (Friction is used as is.)
        }
        else if (!sfDirty && from.isOnGround() && player.isSneaking() && reallySneaking.contains(player.getName()) && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_SNEAKING))) {
            hAllowedDistance = modSneak * walkSpeed * cc.survivalFlySneakingSpeed / 100D;
            friction = 0.0; // Ensure friction can't be used to speed.
            // TODO: Attribute modifiers can count in here, e.g. +0.5 (+ 50% doesn't seem to pose a problem, neither speed effect 2).
        }
        else if (!sfDirty && from.isOnGround() && player.isBlocking() && (!checkPermissions || !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))) {
            hAllowedDistance = modBlock * walkSpeed * cc.survivalFlyBlockingSpeed / 100D;
            friction = 0.0; // Ensure friction can't be used to speed.
        }
        else {
            if (sprinting) {
                hAllowedDistance = walkSpeed * data.multSprinting * cc.survivalFlySprintingSpeed / 100D;
            }
            else {
                hAllowedDistance = walkSpeed * cc.survivalFlyWalkingSpeed / 100D;
            }
            // Count in speed changes (attributes, speed potion).
            // Note: Attributes count in slowness potions, thus leaving out isn't possible.
            final double attrMod = mcAccess.getSpeedAttributeMultiplier(player);
            if (attrMod == Double.MAX_VALUE) {
                // Count in speed potions.
                final double speedAmplifier = mcAccess.getFasterMovementAmplifier(player);
                if (speedAmplifier != Double.NEGATIVE_INFINITY) {
                    hAllowedDistance *= 1.0D + 0.2D * (speedAmplifier + 1);
                }
            } else {
                hAllowedDistance *= attrMod;
            }
            // Ensure friction can't be used to speed.
            // TODO: Model bunny hop as a one time peak + friction. Allow medium based friction.
            friction = 0.0;
        }
        // TODO: Reset friction on too big change of direction?

        // Account for flowing liquids (only if needed).
        // Assume: If in liquids this would be placed right here.
        if (downStream) {
            hAllowedDistance *= modDownStream;
        }

        // If the player is on ice, give them a higher maximum speed.
        if (data.sfOnIce > 0) {
            hAllowedDistance *= modIce;
        }

        // Speeding bypass permission (can be combined with other bypasses).
        if (checkPermissions && player.hasPermission(Permissions.MOVING_SURVIVALFLY_SPEEDING)) {
            hAllowedDistance *= cc.survivalFlySpeedingSpeed / 100D;
        }

        // Friction mechanics (next move).
        if (hDistance <= hAllowedDistance) {
            // Move is within lift-off/burst envelope, allow next time.
            // TODO: This probably is the wrong place (+ bunny, + buffer)?
            data.nextFrictionHorizontal = 1.0;
        }

        // Friction or not (this move).
        if (data.lastHDist != Double.MAX_VALUE && friction > 0.0) {
            // Consider friction.
            // TODO: Invalidation mechanics.
            // TODO: Friction model for high speeds?
            return Math.max(hAllowedDistance, data.lastHDist * friction);
        } else {
            return hAllowedDistance;
        }
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
    private boolean lostGround(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
        // TODO: Regroup with appropriate conditions (toOnGround first?).
        // TODO: Some workarounds allow step height (0.6 on MC 1.8).
        // TODO: yDistance limit does not seem to be appropriate.
        if (yDistance >= -0.7 && yDistance <= Math.max(cc.sfStepHeight, LiftOffEnvelope.NORMAL.getMaxJumpGain(data.jumpAmplifier) + 0.174)) { // MovingUtil.estimateJumpLiftOff(player, data, 0.174))) {
            // "Mild" Ascending / descending.
            //Ascending
            if (yDistance >= 0) {
                if (lostGroundAscend(player, from, to, hDistance, yDistance, sprinting, data, cc)) {
                    return true;
                }
            }
            // Descending.
            if (yDistance <= 0) {
                if (lostGroundDescend(player, from, to, hDistance, yDistance, sprinting, data, cc)) {
                    return true;	
                }
            }
        }
        else if (yDistance < -0.7) {
            // Clearly descending.
            // TODO: Might want to remove this one.
            if (hDistance <= 0.5) {
                if (lostGroundFastDescend(player, from, to, hDistance, yDistance, sprinting, data, cc)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Core y-distance checks for in-air movement (may include air -> other).
     * @return
     */
    private double[] vDistAir(final long now, final Player player, final PlayerLocation from, final boolean fromOnGround, final boolean resetFrom, final PlayerLocation to, final boolean toOnGround, final boolean resetTo, final double hDistance, final double yDistance, final MovingData data, final MovingConfig cc) {
        // Y-distance for normal jumping, like in air.
        double vAllowedDistance = 0.0;
        double vDistanceAboveLimit = 0.0;

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
        if (fallingEnvelope(yDistance, data.lastYDist, 0.0)) {
            // Less headache: Always allow falling. 
            vAllowedDistance = data.lastYDist * FRICTION_MEDIUM_AIR - GRAVITY_MIN; // Upper bound.
            strictVdistRel = true;
        }
        else if (resetFrom) {
            // TODO: More concise conditions? Some workaround may allow more.
            if (toOnGround) { // TODO: Might have to use max if resetto.
                vAllowedDistance = cc.sfStepHeight;
            } else {
                // Code duplication with the absolute limit below.
                if (yDistance < 0.0 || yDistance > cc.sfStepHeight || !tags.contains("lostground_couldstep")) {
                    vAllowedDistance = maxJumpGain + jumpGainMargin;
                } else {
                    // lostground_couldstep
                    vAllowedDistance = yDistance;
                }
            }
            strictVdistRel = false;
        }
        else if (data.lastYDist != Double.MAX_VALUE) {
            if (data.lastYDist >= -GRAVITY_MAX / 2.0 && data.lastYDist <= 0.0 && data.fromWasReset) {
                if (resetTo) { // TODO: Might have to use max if resetto.
                    vAllowedDistance = cc.sfStepHeight;
                }
                else {
                    // TODO: Needs more precise confinement + setting set back or distance to ground or estYDist.
                    vAllowedDistance = maxJumpGain + jumpGainMargin;
                }
                strictVdistRel = false;
            } else {
                vAllowedDistance = data.lastYDist * FRICTION_MEDIUM_AIR - GRAVITY_MIN; // Upper bound.
                strictVdistRel = true;
            }
        } else {
            // Problematic point (thinking of "ncp remove ...").
            // Ensure: data.resetLastDistances() must be used on teleport/join etc.
            vAllowedDistance = yDistance;
            strictVdistRel = true;
            tags.add("data_missing");
        }
        // Compare yDistance to expected, use velocity on violation.
        // TODO: data.noFallAssumeGround  needs more precise flags (refactor to per move data objects, store 123)
        boolean vDistRelVL = false;
        // Change seen from last yDistance.
        final double yDistChange = data.lastYDist == Double.MAX_VALUE ? Double.MAX_VALUE : yDistance - data.lastYDist;
        // Difference from vAllowedDistance to yDistance.
        final double yDistDiffEx = yDistance - vAllowedDistance;
        if (yDistDiffEx > 0.0) { // Upper bound violation.
            // && (yDistance > 0.0 || (!resetTo && !data.noFallAssumeGround))
            if (yDistance <= 0.0 && (resetTo || data.noFallAssumeGround)) {
                // Allow falling shorter than expected, if onto ground.
                // Note resetFrom should usually mean that allowed dist is > 0 ?
            }
            else if (yDistance < 0.0 && data.lastYDist < 0.0 && yDistChange > -GRAVITY_MAX
                    && (from.isOnGround(Math.abs(yDistance) + 0.001) || BlockProperties.isLiquid(to.getTypeId(to.getBlockX(), Location.locToBlock(to.getY() - 0.5), to.getBlockZ())))) {
                // Pretty coarse workaround, should instead do a proper modeling for from.getDistanceToGround.
                // (OR loc... needs different model, distanceToGround, proper set-back, moveHitGround)
                // TODO: Slightly too short move onto the same level as snow (0.75), but into air (yDistance > -0.5).
                // TODO: Better on-ground model (adapt to actual client code).
            }
            //                else if (yDistance < 0.0 && yDistChange > 0.0 && tags.contains("lostground_edgedesc")) {
            //                    // Falling less far than expected by hitting an edge.
            //                }
            else if (yDistDiffEx < GRAVITY_MIN / 2.0 && data.sfJumpPhase == 1 //&& data.fromWasReset
                    // TODO: Test with demanding && (data.noFallAssumeGround || data.liftOffEnvelope != LiftOffEnvelope.NORMAL)
                    && to.getY() - data.getSetBackY() <= data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier)
                    && data.lastYDist <= maxJumpGain && yDistance > -GRAVITY_MAX  && yDistance < data.lastYDist
                    && data.lastYDist - yDistance > GRAVITY_ODD / 3.0) {
                // Special jump (water/edges/assume-ground), too small decrease.
            }
            else if (yDistDiffEx < GRAVITY_MIN && data.sfJumpPhase == 1 
                    && data.liftOffEnvelope != LiftOffEnvelope.NORMAL && data.fromWasReset
                    && data.lastYDist < -GRAVITY_ODD / 2.0 && data.lastYDist > -GRAVITY_MAX - GRAVITY_SPAN
                    && yDistance < data.lastYDist - 0.001) {
                // Odd decrease with water.
            }
            else if (oddLiquid(yDistance, yDistDiffEx, maxJumpGain, resetTo, data)) {
                // Jump after leaving the liquid near ground.
            }
            else if (oddGravity(from, to, yDistance, yDistChange, data)) {
                // Starting to fall / gravity effects.
            }
            else if (oddSlope(to, yDistance, maxJumpGain, yDistDiffEx, data)) {
                // Odd decrease after lift-off.
            }
            else {
                // Violation.
                vDistRelVL = true;
            }
        } // else: yDistDiffEx <= 0.0
        else if (yDistance >= 0.0) {
            if (!strictVdistRel || Math.abs(yDistDiffEx) <= GRAVITY_SPAN || vAllowedDistance <= 0.2) {
                // Allow jumping less high unless within "strict envelope".
                // TODO: Extreme anti-jump effects, perhaps.
            }
            else if (oddGravity(from, to, yDistance, yDistChange, data)) {
                // Starting to fall.
            }
            else if (oddSlope(to, yDistance, maxJumpGain, yDistDiffEx, data)) {
                // Odd decrease after lift-off.
            }
            else if (oddLiquid(yDistance, yDistDiffEx, maxJumpGain, resetTo, data)) {
                // Jump after leaving the liquid near ground.
            }
            else if (yDistance > 0.0 && data.lastYDist != Double.MAX_VALUE && data.lastYDist > yDistance
                    && data.lastYDist - yDistance <= data.lastYDist / 4.0
                    && data.isVelocityJumpPhase()
                    ) {
                // Too strong decrease with velocity.
                // TODO: Observed when moving off water, might be confined by that.
            }
            else {
                vDistRelVL = true;
            }
            // else: Allow moving up less. Note: possibility of low jump.
        } else { // if (yDistance < 0.0) // Rather too fast falling.
            if (Math.abs(yDistDiffEx) > GRAVITY_SPAN) {
                if (yDistance < -3.0 && data.lastYDist < -3.0 && Math.abs(yDistDiffEx) < 5.0 * GRAVITY_MAX) {
                    // Disregard not falling faster at some point (our constants don't match 100%).
                }
                else if (resetTo && (yDistDiffEx > -GRAVITY_SPAN || !fromOnGround && !data.noFallAssumeGround && yDistChange >= 0.0)) {
                    // Moving onto ground allows a shorter move.
                    // TODO: Any lost-ground cases? 
                }
                else if (yDistance > data.lastYDist - GRAVITY_MAX - GRAVITY_SPAN && (resetTo || data.noFallAssumeGround)) {
                    // Mirrored case for yDistance > yAllowedDistance, hitting ground.
                    // TODO: Needs more efficient structure.
                }
                else if (resetFrom && yDistance >= -0.5 && (yDistance > -0.31 || (resetTo || to.isAboveStairs()) && (data.lastYDist < 0.0))) {
                    // Stairs and other cases moving off ground or ground-to-ground.
                    // TODO: Margins !?
                }
                else if (data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID 
                        && data.sfJumpPhase == 1 
                        && data.fromWasReset && !data.toWasReset && !resetFrom && resetTo // TODO: There might be other cases (possibly wrong bounding box).
                        && data.lastYDist > 0.0 && data.lastYDist < 0.5 * GRAVITY_ODD // TODO: There might be cases with < 0.2 !
                        && yDistance < 0.0 && Math.abs(Math.abs(yDistance) - data.lastYDist) < GRAVITY_SPAN / 2.0
                        ) {
                    // LIMIT_LIQUID, vDist inversion (!).
                }
                else if (oddGravity(from, to, yDistance, yDistChange, data)) {
                    // Starting to fall.
                }
                else {
                    // Violation.
                    vDistRelVL = true;
                }
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
        if (yDistance > 0.0 && !data.isVelocityJumpPhase() 
                && !((fromOnGround || data.noFallAssumeGround) && toOnGround && yDistance <= cc.sfStepHeight)) {
            // TODO: Maintain a value in data, adjusting to velocity?
            // TODO: LIMIT_JUMP
            final double vAllowedAbsoluteDistance = data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier);
            final double totalVDistViolation =  to.getY() - data.getSetBackY() - vAllowedAbsoluteDistance;
            if (totalVDistViolation > 0.0) {
                // Skip if the player could step up.
                if (yDistance > cc.sfStepHeight || !tags.contains("lostground_couldstep")) {
                    if (data.getOrUseVerticalVelocity(yDistance) == null) {
                        vDistanceAboveLimit = Math.max(vDistanceAboveLimit, totalVDistViolation);
                        tags.add("vdistsb");
                    }
                }
            }
        }

        // More in air checks.
        // TODO: move into the in air checking above !?
        if (!resetFrom && !resetTo) {
            // "On-air" checks (vertical, already use velocity if needed).
            vDistanceAboveLimit = Math.max(vDistanceAboveLimit, inAirChecks(now, from, to, hDistance, yDistance, data, cc));
        }

        // Simple-step blocker.
        // TODO: Complex step blocker: distance to set-back + low jump + accounting info
        if ((resetFrom || data.noFallAssumeGround) && resetTo && vDistanceAboveLimit <= 0D && 
                yDistance > cc.sfStepHeight && yDistance > maxJumpGain + 0.1) {
            boolean step = true;
            // Exclude a lost-ground case.
            if (data.noFallAssumeGround && data.lastYDist != Double.MAX_VALUE && data.lastYDist <= 0.0 && yDistance > 0.0 && 
                    yDistance + Math.abs(data.lastYDist) <= 2.0 * (maxJumpGain + 0.1)) {
                step = false;
            }
            // Check bypass permission last.
            if (step && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_STEP)) {
                final double vl = Math.max(vDistanceAboveLimit, Math.abs(from.isOnClimbable() ? yDistance : yDistance - (maxJumpGain + 0.1))); // Could adjust if on ladders etc.
                if (vl > 0.0 && data.getOrUseVerticalVelocity(yDistance) == null) {
                    vDistanceAboveLimit = vl;
                    tags.add("step");
                }
            }
        }

        // Air-stay-time.
        // TODO: max-phase only when from is not reset !?
        final int maxJumpPhase = data.liftOffEnvelope.getMaxJumpPhase(data.jumpAmplifier);
        if (data.sfJumpPhase > maxJumpPhase && !data.isVelocityJumpPhase()) {
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
     * Odd decrease after lift-off.
     * @param to
     * @param yDistance
     * @param maxJumpGain
     * @param yDistDiffEx
     * @param data
     * @return
     */
    private static boolean oddSlope(final PlayerLocation to, final double yDistance, final double maxJumpGain, final double yDistDiffEx, final MovingData data) {
        return data.sfJumpPhase == 1 //&& data.fromWasReset 
                && Math.abs(yDistDiffEx) < 2.0 * GRAVITY_SPAN 
                && data.lastYDist != Double.MAX_VALUE && data.lastYDist > 0.0 && yDistance < data.lastYDist
                && to.getY() - data.getSetBackY() <= data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier)
                && (
                        // Decrease more after lost-ground cases with more y-distance than normal lift-off.
                        data.lastYDist > maxJumpGain && data.lastYDist < 1.1 * maxJumpGain 
                        //&& fallingEnvelope(yDistance, data.lastYDist, 2.0 * GRAVITY_SPAN)
                        // Decrease more after going through liquid (but normal ground envelope).
                        || data.lastYDist > 0.5 * maxJumpGain && data.lastYDist < 0.84 * maxJumpGain
                        && data.lastYDist - yDistance <= GRAVITY_MAX + GRAVITY_SPAN  
                        );
    }

    /**
     * Test if the player is (well) within in-air falling envelope.
     * @param yDistance
     * @param lastYDist
     * @param extraGravity Extra amount to fall faster.
     * @return
     */
    private static boolean fallingEnvelope(final double yDistance, final double lastYDist, final double extraGravity) {
        if (lastYDist == Double.MAX_VALUE || yDistance >= lastYDist) {
            return false;
        }
        final double frictDist = lastYDist * FRICTION_MEDIUM_AIR - GRAVITY_MIN;
        return yDistance <= frictDist + extraGravity && yDistance > frictDist - GRAVITY_SPAN - extraGravity;
    }

    /**
     * Jump after leaving the liquid near ground or jumping through liquid (rather friction envelope, problematic).
     * @return If the exemption condition applies.
     */
    private static boolean oddLiquid(final double yDistance, final double yDistDiffEx, final double maxJumpGain, final boolean resetTo, final MovingData data) {
        // TODO: Relate jump phase to last/second-last move fromWasReset (needs keeping that data in classes).
        // TODO: And distinguish where JP=2 is ok?
        // TODO: Do any belong into odd gravity? (Needs re-grouping EVERYTHING anyway.)
        return (data.sfJumpPhase == 1 || data.sfJumpPhase == 2)
                && (
                        // Jump or decrease falling speed after a small gain (could be bounding box?).
                        yDistDiffEx > 0.0 && data.liftOffEnvelope != LiftOffEnvelope.NORMAL
                        && data.lastYDist >= -GRAVITY_MAX - GRAVITY_MIN && data.lastYDist < GRAVITY_MAX + GRAVITY_SPAN
                        && yDistance > data.lastYDist && yDistance < 0.84 * maxJumpGain
                        // Falling slightly too fast. Actually a friction envelope (bad).
                        // TODO: Velocity jump phase isn't exact on that account, but shouldn't hurt.
                        || yDistDiffEx < 0.0 && (data.liftOffEnvelope != LiftOffEnvelope.NORMAL || data.isVelocityJumpPhase())
                        && fallingEnvelope(yDistance, data.lastYDist, GRAVITY_ODD / 2.0)
                        // Falling slightly too slow.
                        || yDistDiffEx > 0.0 && data.liftOffEnvelope == LiftOffEnvelope.LIMIT_LIQUID
                        && data.lastYDist != Double.MAX_VALUE && data.lastYDist > -2.0 * GRAVITY_MAX - GRAVITY_ODD
                        && yDistance < data.lastYDist && data.lastYDist - yDistance < GRAVITY_MAX
                        && data.lastYDist - yDistance > GRAVITY_MIN / 4.0
                        // Falling too slow around 0 yDistance.
                        );
    }

    /**
     * A condition for exemption from vdistrel (vDistAir), around where gravity
     * hits most hard, including head obstruction. This method is called with
     * varying pre-conditions, thus a full envelope check is necessary.
     * 
     * @param yDistance
     * @param yDistChange
     * @param data
     * @return If the condition applies, i.e. if to exempt.
     */
    private static boolean oddGravity(final PlayerLocation from, final PlayerLocation to, final double yDistance, final double yDistChange, final MovingData data) {
        // TODO: Identify spots only to apply with limited LiftOffEnvelope (some guards got removed before switching to that).
        // TODO: Cleanup pending.
        // Old condition (normal lift-off envelope).
        //        yDistance >= -GRAVITY_MAX - GRAVITY_SPAN 
        //        && (yDistChange < -GRAVITY_MIN && Math.abs(yDistChange) <= 2.0 * GRAVITY_MAX + GRAVITY_SPAN
        //        || from.isHeadObstructed(from.getyOnGround()) || data.fromWasReset && from.isHeadObstructed())
        return 
                // Any envelope (supposedly normal) near 0 yDistance.
                yDistance > -2.0 * GRAVITY_MAX - GRAVITY_MIN && yDistance < 2.0 * GRAVITY_MAX + GRAVITY_MIN
                && (
                        // Too big chunk of change, but within reasonable bounds (should be contained in some other generic case?).
                        data.lastYDist < 3.0 * GRAVITY_MAX + GRAVITY_MIN && yDistChange < -GRAVITY_MIN && yDistChange > -2.5 * GRAVITY_MAX -GRAVITY_MIN
                        // Transition to 0.0 yDistance.
                        || data.lastYDist > GRAVITY_ODD / 2.0 && data.lastYDist < GRAVITY_MIN && yDistance == 0.0
                        // yDist inversion near 0 (almost). TODO: This actually happens near liquid, but NORMAL env!?
                        // lastYDist < Gravity max + min happens with dirty phase (slimes),. previously: max + span
                        // TODO: Can all cases be reduced to change sign with max. neg. gain of max + span ?
                        || data.lastYDist <= GRAVITY_MAX + GRAVITY_MIN && data.lastYDist > GRAVITY_ODD
                        && yDistance < GRAVITY_ODD && yDistance > -2.0 * GRAVITY_MAX - GRAVITY_ODD / 2.0
                        // Head is obstructed. TODO: Cover this in a more generic way elsewhere (<= friction envelope + obstructed).
                        || data.lastYDist >= 0.0 && (from.isHeadObstructed(from.getyOnGround()) || data.fromWasReset && from.isHeadObstructed())
                        // Break the block underneath.
                        || data.lastYDist < 0.0 && data.toWasReset // TODO: Also assumeGround? Should have more precise flags.
                        && yDistance >= -GRAVITY_MAX - GRAVITY_SPAN && yDistance <= GRAVITY_MIN
                        // Slope with slimes (also near ground without velocityJumpPhase, rather lowjump but not always).
                        || data.lastYDist < -GRAVITY_MAX && yDistChange < - GRAVITY_ODD / 2.0 && yDistChange > -GRAVITY_MIN
                        // Near ground (slime block).
                        || data.lastYDist == 0.0 && yDistance < -GRAVITY_ODD / 2.5 && yDistance > -GRAVITY_MIN && to.isOnGround(GRAVITY_MIN)
                        )
                        // With velocity.
                        || data.isVelocityJumpPhase()
                        && (
                                // Near zero inversion with slimes (rather dirty phase).
                                data.lastYDist > GRAVITY_ODD && data.lastYDist < GRAVITY_MAX + GRAVITY_MIN
                                && yDistance <= -data.lastYDist && yDistance > -data.lastYDist - GRAVITY_MAX - GRAVITY_ODD
                                // Odd mini-decrease with dirty phase (slime).
                                || data.lastYDist < -0.204 && yDistance > -0.26
                                && yDistChange > -GRAVITY_MIN && yDistChange < -GRAVITY_ODD / 4.0
                                // Lot's of decrease near zero TODO: merge later.
                                || data.lastYDist < -GRAVITY_ODD && data.lastYDist > -GRAVITY_MIN
                                && yDistance > -2.0 * GRAVITY_MAX - 2.0 * GRAVITY_MIN && yDistance < -GRAVITY_MAX
                                // Odd decrease less near zero.
                                || yDistChange > -GRAVITY_MIN && yDistChange < -GRAVITY_ODD 
                                && data.lastYDist < 0.5 && data.lastYDist > 0.4
                                // Small decrease after high edge. TODO: Consider min <-> span, generic.
                                || data.lastYDist == 0.0 && yDistance > -GRAVITY_MIN && yDistance < -GRAVITY_ODD
                                )
                                // Small distance to set.back. .
                                || data.hasSetBack() && Math.abs(data.getSetBackY() - from.getY()) < 1.0
                                // TODO: Consider low fall distance as well.
                                && (
                                        // Near ground small decrease.
                                        data.lastYDist > GRAVITY_MAX && data.lastYDist < 3.0 * GRAVITY_MAX
                                        && yDistChange > - GRAVITY_MIN && yDistChange < -GRAVITY_ODD
                                        // Bounce without velocity set.
                                        //|| data.lastYDist == 0.0 && yDistance > -GRAVITY_MIN && yDistance < GRAVITY_SPAN
                                        )
                                        // Jump-effect-specific
                                        // TODO: Jump effect at reduced lift off envelope -> skip this?
                                        || data.jumpAmplifier > 0 && data.lastYDist < GRAVITY_MAX + GRAVITY_MIN / 2.0 && data.lastYDist > -2.0 * GRAVITY_MAX - 0.5 * GRAVITY_MIN
                                        && yDistance > -2.0 * GRAVITY_MAX - 2.0 * GRAVITY_MIN && yDistance < GRAVITY_MIN
                                        && yDistChange < -GRAVITY_SPAN
                                        // Another near 0 yDistance case. TODO: Inaugurate into some more generic envelope.
                                        || data.lastYDist > -GRAVITY_MAX && data.lastYDist < GRAVITY_MIN && !data.toWasReset
                                        && yDistance < data.lastYDist - GRAVITY_MIN / 2.0 && yDistance > data.lastYDist - GRAVITY_MAX - 0.5 * GRAVITY_MIN
                                        // Reduced jumping envelope.
                                        || data.liftOffEnvelope != LiftOffEnvelope.NORMAL
                                        && (
                                                // Wild-card allow half gravity near 0 yDistance. TODO: Check for removal of included cases elsewhere.
                                                data.lastYDist > -10.0 * GRAVITY_ODD / 2.0 && data.lastYDist < 10.0 * GRAVITY_ODD
                                                && yDistance < data.lastYDist - GRAVITY_MIN / 2.0 && yDistance > data.lastYDist - GRAVITY_MAX
                                                // 
                                                || data.lastYDist < GRAVITY_MAX + GRAVITY_SPAN && data.lastYDist > GRAVITY_ODD
                                                && yDistance > 0.4 * GRAVITY_ODD && yDistance - data.lastYDist < -GRAVITY_ODD / 2.0
                                                // 
                                                || data.lastYDist < 0.2 && data.lastYDist >= 0.0 && yDistance > -0.2 && yDistance < 2.0 * GRAVITY_MAX
                                                // 
                                                || data.lastYDist > 0.4 * GRAVITY_ODD && data.lastYDist < GRAVITY_MIN && yDistance == 0.0
                                                // Too small decrease, right after lift off.
                                                || data.sfJumpPhase == 1 && data.lastYDist > -GRAVITY_ODD && data.lastYDist <= GRAVITY_MAX + GRAVITY_SPAN
                                                && yDistance - data.lastYDist < 0.0114
                                                // Any leaving liquid and keeping distance once.
                                                || data.sfJumpPhase == 1 
                                                && Math.abs(yDistance) <= swimBaseSpeedV() && yDistance == data.lastYDist
                                                )
                                                ;
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
    private double inAirChecks(final long now, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final MovingData data, final MovingConfig cc) {
        double vDistanceAboveLimit = 0;

        if (data.sfLowJump) {
            tags.add("lowjump");
        }

        // y direction change detection.
        // TODO: Consider using accounting for y-change detection. <- Nope :).
        final boolean yDirChange = data.lastYDist != Double.MAX_VALUE && data.lastYDist != yDistance && (yDistance <= 0 && data.lastYDist >= 0 || yDistance >= 0 && data.lastYDist <= 0 ); 
        if (yDirChange) {
            // yDirChange uses velocity if needed.
            vDistanceAboveLimit = yDirChange(from, to, yDistance, vDistanceAboveLimit, data);
        }

        // Accounting support.
        if (cc.survivalFlyAccountingV) {
            // Currently only for "air" phases.
            // Vertical.
            if (yDirChange && data.lastYDist > 0) { // (Double.MAX_VALUE is checked above.)
                // Change to descending phase.
                data.vDistAcc.clear();
                // Allow adding 0.
                data.vDistAcc.add((float) yDistance);
            }
            else if (data.verVelUsed == null) { // Only skip if just used.
                // Here yDistance can be negative and positive.
                if (yDistance != 0D) {
                    data.vDistAcc.add((float) yDistance);
                    final double accAboveLimit = verticalAccounting(yDistance, data.vDistAcc ,tags, "vacc" + (data.isVelocityJumpPhase() ? "dirty" : ""));
                    if (accAboveLimit > vDistanceAboveLimit) {
                        if (data.getOrUseVerticalVelocity(yDistance) == null) {
                            vDistanceAboveLimit = accAboveLimit;
                        }
                    }
                }
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
                    sc0 = acc.bucketScore(0) * (float) cap / (float) count0 - GRAVITY_VACC * (float) (cap - count0);
                }
                final float sc1 = acc.bucketScore(1);
                if (sc0 > sc1 - 3.0 * GRAVITY_VACC) {
                    // TODO: Velocity downwards fails here !!!
                    if (yDistance <= -1.05 && sc1 < -8.0 && sc0 < -8.0) { // (aDiff < Math.abs(yDistance) || sc2 < - 10.0f)) {
                        // High falling speeds may pass.
                        tags.add(tag + "grace");
                        return 0.0;
                    }
                    tags.add(tag);
                    return sc0 - (sc1 - 3.0 * GRAVITY_VACC);
                }
            }
        }
        return 0.0;
    }

    /**
     * Check on change of y direction.
     * <br>Note: data.sfLastYDist must not be Double.MAX_VALUE when calling this.
     * @param yDistance
     * @param vDistanceAboveLimit
     * @return vDistanceAboveLimit
     */
    private double yDirChange(final PlayerLocation from, final PlayerLocation to, final double yDistance, double vDistanceAboveLimit, final MovingData data) {
        // TODO: Does this account for velocity in a sufficient way?
        if (yDistance > 0) {
            // TODO: Clear active vertical velocity here ?
            // TODO: Demand consuming queued velocity for valid change (!).
            // Increase
            if (data.toWasReset) {
                tags.add("ychinc");
            }
            else {
                // Moving upwards after falling without having touched the ground.
                if (data.bunnyhopDelay < 9 && !(data.fromWasReset && data.lastYDist == 0D) && data.getOrUseVerticalVelocity(yDistance) == null) {
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
                    data.lastYDist != Double.MAX_VALUE && data.lastYDist > 0.0 && !data.isVelocityJumpPhase()) {
                final double setBackYDistance = from.getY() - data.getSetBackY();
                if (setBackYDistance > 0.0) {
                    // Only count it if the player has actually been jumping (higher than setback).
                    final Player player = from.getPlayer();
                    // Estimate of minimal jump height.
                    double estimate = 1.15;
                    if (data.jumpAmplifier > 0) {
                        // TODO: Could skip this.
                        estimate += 0.5 * getJumpAmplifier(player);
                    }
                    if (setBackYDistance < estimate) {
                        // Low jump, further check if there might have been a reason for low jumping.
                        if (!from.isHeadObstructed() && !to.isHeadObstructed()) {
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
     * @param player
     * @param from
     * @param to
     * @param walkSpeed
     * @param hAllowedDistance
     * @param hDistance
     * @param hDistanceAboveLimit
     * @param yDistance
     * @param sprinting
     * @param downStream
     * @param skipPermChecks
     * @param data
     * @param cc
     * @return hAllowedDistance, hDistanceAboveLimit, hFreedom
     */
    private double[] hDistAfterFailure(final Player player, final PlayerLocation from, final PlayerLocation to, final double walkSpeed, double hAllowedDistance, final double hDistance, double hDistanceAboveLimit, final double yDistance, final boolean sprinting, final boolean downStream, final MovingData data, final MovingConfig cc, final boolean skipPermChecks) {

        // TODO: Still not entirely sure about this checking order.
        // TODO: Would quick returns make sense for hDistanceAfterFailure == 0.0?

        // Test bunny early, because it applies often and destroys as little as possible.
        hDistanceAboveLimit = bunnyHop(from, to, hDistance, hAllowedDistance, hDistanceAboveLimit, yDistance, sprinting, data);

        // After failure permission checks ( + speed modifier + sneaking + blocking + speeding) and velocity (!).
        if (hDistanceAboveLimit > 0.0 && !skipPermChecks) {
            // TODO: Most cases these will not apply. Consider redesign to do these last or checking right away and skip here on some conditions.
            hAllowedDistance = getAllowedhDist(player, from, to, sprinting, downStream, hDistance, walkSpeed, data, cc, true);
            hDistanceAboveLimit = hDistance - hAllowedDistance;
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
            hDistanceAboveLimit = bunnyHop(from, to, hDistance, hAllowedDistance, hDistanceAboveLimit, yDistance, sprinting, data);
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
    private double bunnyHop(final PlayerLocation from, final PlayerLocation to, final double hDistance, final double hAllowedDistance, double hDistanceAboveLimit, final double yDistance, final boolean sprinting, final MovingData data) {
        // Check "bunny fly" here, to not fall over sprint resetting on the way.
        boolean allowHop = true;
        boolean double_bunny = false;

        // TODO: A more state-machine like modeling (hop, slope, states, low-edge).

        // Fly phase.
        if (data.lastHDist != Double.MAX_VALUE && data.bunnyhopDelay > 0 && hDistance > walkSpeed) { // * modSprint) {
            // (lastHDist may be reset on commands.)
            allowHop = false; // Magic!
            final int hopTime = bunnyHopMax - data.bunnyhopDelay;

            // Increase buffer if hDistance is decreasing properly.
            if (data.lastHDist != Double.MAX_VALUE && data.lastHDist > hDistance) {
                final double hDistDiff = data.lastHDist - hDistance;

                // Bunny slope (downwards, directly after hop but before friction).
                if (data.bunnyhopDelay == bunnyHopMax - 1) {
                    // Ensure relative speed decrease vs. hop is met somehow.
                    if (hDistDiff >= 0.66 * (data.lastHDist - hAllowedDistance)) {
                        tags.add("bunnyslope");
                        hDistanceAboveLimit = 0.0;
                    }
                }
                else if (
                        hDistDiff >= data.lastHDist / bunnyDivFriction || hDistDiff >= hDistanceAboveLimit / 33.3 || 
                        hDistDiff >= (hDistance - hAllowedDistance) * (1.0 - SurvivalFly.FRICTION_MEDIUM_AIR)
                        ) {
                    // TODO: Confine friction by medium ?
                    // TODO: Also calculate an absolute (minimal) speed decrease over the whole time, at least max - count?
                    tags.add("bunnyfriction");
                    //if (hDistanceAboveLimit <= someThreshold) { // To be covered by bunnyslope.
                    // Speed must decrease by "a lot" at first, then by some minimal amount per event.
                    // TODO: Confine buffer to only be used during low jump phase !?
                    //if (!(data.toWasReset && from.isOnGround() && to.isOnGround())) { // FISHY

                    // Allow the move.
                    hDistanceAboveLimit = 0.0;
                    if (data.bunnyhopDelay == 1 && !to.isOnGround() && !to.isResetCond()) {
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
            if (!allowHop && data.lastHDist != Double.MAX_VALUE && hDistance - data.lastHDist >= walkSpeed * 0.5 && hopTime == 1) {
                if (data.lastYDist >= -GRAVITY_MAX / 2.0 && data.lastYDist <= 0.0 && (data.fromWasReset || data.toWasReset) && yDistance >= 0.4) {
                    // TODO: Confine to increasing set back y ?
                    tags.add(DOUBLE_BUNNY);
                    allowHop = double_bunny = true;
                }
            }

            // Allow hop for special cases.
            if (!allowHop && (from.isOnGround() || data.noFallAssumeGround)) {
                if (data.bunnyhopDelay <= 6 || from.isHeadObstructed() || to.isHeadObstructed()) {
                    // TODO: headObstructed: check always and set a flag in data + consider regain buffer?
                    tags.add("ediblebunny");
                    allowHop = true;
                }
            }
        }

        // Check hop (singular peak up to roughly two times the allowed distance).
        // TODO: Needs better modeling.
        if (allowHop && hDistance >= walkSpeed
                && (hDistance > (((data.lastHDist == Double.MAX_VALUE || data.lastHDist == 0.0 && data.lastYDist == 0.0) ? 1.11 : 1.314)) * hAllowedDistance) 
                && hDistance < 2.15 * hAllowedDistance
                || (yDistance > from.getyOnGround() || hDistance < 2.6 * walkSpeed) && data.lastHDist != Double.MAX_VALUE && hDistance > 1.314 * data.lastHDist && hDistance < 2.15 * data.lastHDist
                ) { // if (sprinting) {
            // TODO: Test bunny spike over all sorts of speeds + attributes.
            // TODO: Allow slightly higher speed on lost ground?
            // TODO: LiftOffEnvelope.allowBunny ?
            if (data.liftOffEnvelope == LiftOffEnvelope.NORMAL // && yDistance >= 0.4 
                    && !data.sfLowJump || data.sfNoLowJump
                    && (data.sfJumpPhase == 0 && from.isOnGround() || data.sfJumpPhase <= 1 && data.noFallAssumeGround || double_bunny)
                    && !from.isResetCond() && !to.isResetCond() // TODO: !to.isResetCond() should be reviewed.
                    ) {
                // TODO: Jump effect might allow more strictness. 
                // TODO: Expected minimum gain depends on last speed (!).
                // TODO: Speed effect affects hDistanceAboveLimit?
                data.bunnyhopDelay = bunnyHopMax;
                hDistanceAboveLimit = 0D;
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
    private void hBufRegain(final double hDistance, final double amount, final MovingData data) {
        /*
         * TODO: Consider different concepts: 
         * 			- full resetting with harder conditions.
         * 			- maximum regain amount.
         * 			- reset or regain only every x blocks h distance.
         */
        // TODO: Confine general conditions for buffer regain further (regain in air, whatever)?
        data.sfHorizontalBuffer = Math.min(hBufMax, data.sfHorizontalBuffer + amount);
    }

    private static double swimBaseSpeedV() {
        // TODO: Does this have to be the dynamic walk speed (refactoring)?
        return walkSpeed * modSwim + 0.02;
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
    private double[] vDistLiquid(final PlayerLocation from, final PlayerLocation to, final boolean toOnGround, final double yDistance, final MovingData data) {
        data.sfNoLowJump = true;

        // Expected envelopes.
        final double baseSpeed = swimBaseSpeedV();
        final double yDistAbs = Math.abs(yDistance);

        // TODO: Later also cover things like a sudden stop.

        // Minimal speed.
        if (yDistAbs <= baseSpeed) {
            return new double[]{baseSpeed, 0.0};
        }

        // Friction envelope (allow any kind of slow down).
        final double frictDist = data.lastYDist == Double.MAX_VALUE ? baseSpeed : Math.abs(data.lastYDist) * data.lastFrictionVertical; // Bounds differ with sign.
        if (data.lastYDist != Double.MAX_VALUE) {
            if (data.lastYDist < 0.0 && yDistance < 0.0 && yDistAbs < frictDist + GRAVITY_MAX + GRAVITY_SPAN) {
                return new double[]{-frictDist - GRAVITY_MAX - GRAVITY_SPAN, 0.0};
            }
            if (data.lastYDist > 0.0 && yDistance > 0.0 && yDistance < frictDist - GRAVITY_MIN) {
                return new double[]{frictDist - GRAVITY_MIN, 0.0};
            }
            // ("== 0.0" is covered by the minimal speed check above.)
        }

        // Special cases.
        if (yDistance >= 0) {
            // TODO: liftOffEnvelope: refine conditions (general) , should be near water level.
            // TODO: 1.5 high blocks ?
            // TODO: Conditions seem warped.
            if (yDistance <= 0.5) {
                if (data.lastYDist != Double.MAX_VALUE && yDistance < data.lastYDist
                        && data.lastYDist - yDistance > Math.max(0.001, yDistance - baseSpeed)) {
                    // Decrease more than difference to baseSpeed.
                    return new double[]{yDistance, 0.0};
                }
                if (yDistance <= data.liftOffEnvelope.getMaxJumpGain(data.jumpAmplifier) && !BlockProperties.isLiquid(from.getTypeIdAbove()) 
                        // TODO: What now !?
                        || !to.isInLiquid()
                        || (toOnGround || data.lastYDist != Double.MAX_VALUE && data.lastYDist - yDistance >= 0.010 || to.isAboveStairs())) {
                    double vAllowedDistance = walkSpeed * modSwim + 0.5;
                    double vDistanceAboveLimit = yDistance - vAllowedDistance;
                    if (vDistanceAboveLimit <= 0.0) {
                        return new double[]{vAllowedDistance, 0.0};
                    }
                }
            }

        }
        else if (yDistance < 0.0) {
            // Falling into water, mid-speed (second move after diving in).
            if (yDistance > -0.9 && data.lastYDist != Double.MAX_VALUE && yDistance < data.lastYDist 
                    && Math.abs(yDistance - data.lastYDist) <= GRAVITY_MAX + GRAVITY_MIN 
                    && yDistance - data.lastYDist < -GRAVITY_MIN
                    //&& !BlockProperties.isLiquid(to.getTypeId(to.getBlockX(), Location.locToBlock(to.getY() + to.getEyeHeight()), to.getBlockZ()))
                    ) {
                return new double[]{data.lastYDist - GRAVITY_MAX - GRAVITY_MIN, 0.0};
            }
            // In-water rough near-0-inversion from allowed speed to a negative amount, little more than allowed (magic -0.2 roughly).
            else if (data.lastYDist >= GRAVITY_MAX / 2.0 && data.lastYDist <= GRAVITY_MAX + GRAVITY_MIN / 2.0
                    && yDistance < 0.0 && yDistance > -2.0 * GRAVITY_MAX - GRAVITY_MIN / 2.0
                    && data.isVelocityJumpPhase() && to.isInLiquid() // TODO: Might skip the liquid check, though.
                    && data.fromWasReset && data.toWasReset
                    ) {
                return new double[]{yDistance, 0.0};
            }
        }

        // TODO: Also DOWNSTREAM !?


        // Try to use velocity for compensation.
        if (data.getOrUseVerticalVelocity(yDistance) != null) {
            return new double[]{yDistance, 0.0};
        }

        // At this point a violation.
        tags.add(yDistance < 0.0 ? "swimdown" : "swimup");
        final double vl1 = yDistAbs - baseSpeed;
        final double vl2 = Math.abs(yDistAbs - frictDist - (yDistance < 0.0 ? GRAVITY_MAX + GRAVITY_SPAN : GRAVITY_MIN));
        if (vl1 <= vl2) {
            return new double[]{yDistance < 0.0 ? -baseSpeed : baseSpeed, vl1};
        } else {
            return new double[]{yDistance < 0.0 ? -frictDist - GRAVITY_MAX - GRAVITY_SPAN : frictDist - GRAVITY_MIN, vl2};
        }
    }

    /**
     * On-climbable vertical distance checking.
     * @param from
     * @param fromOnGround
     * @param toOnGround
     * @param yDistance
     * @param data
     * @return vDistanceAboveLimit
     */
    private double vDistClimbable(final Player player, final PlayerLocation from, final boolean fromOnGround, final boolean toOnGround, final double yDistance, final MovingData data) {
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
        if (Math.abs(yDistance) > climbSpeed) {
            if (from.isOnGround(jumpHeight, 0D, 0D, BlockProperties.F_CLIMBABLE)) {
                if (yDistance > data.liftOffEnvelope.getMaxJumpGain(data.jumpAmplifier)+ 0.1) {
                    tags.add("climbstep");
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance) - climbSpeed);
                }
            } else {
                tags.add("climbspeed");
                vDistanceAboveLimit = Math.max(vDistanceAboveLimit, Math.abs(yDistance) - climbSpeed);
            }
        }
        if (yDistance > 0) {
            if (!fromOnGround && !toOnGround && !data.noFallAssumeGround) {
                // Check if player may climb up.
                // (This does exclude ladders.)
                if (!from.canClimbUp(jumpHeight)) {
                    tags.add("climbdetached");
                    vDistanceAboveLimit = Math.max(vDistanceAboveLimit, yDistance);
                }
            }
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
    private double[] vDistWeb(final Player player, final PlayerLocation from, final PlayerLocation to, final boolean toOnGround, final double hDistanceAboveLimit, final double yDistance, final long now, final MovingData data, final MovingConfig cc) {
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
                vAllowedDistance = from.isOnGround() ? 0.1D : 0;
            }
            vDistanceAboveLimit = yDistance - vAllowedDistance;
        }
        else {
            // Descending in web.
            // TODO: Implement something (at least for being in web with the feet or block above)?
        }
        if (cc.survivalFlyCobwebHack && vDistanceAboveLimit > 0.0 && hDistanceAboveLimit <= 0.0) {
            // TODO: Seemed fixed at first by CB/MC, but still does occur due to jumping. 
            if (hackCobweb(player, data, to, now, vDistanceAboveLimit)) {
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
     * Check if a ground-touch has been lost due to event-sending-frequency or other reasons.<br>
     * This is for ascending only (yDistance >= 0).
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
    private boolean lostGroundAscend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
        final double setBackYDistance = to.getY() - data.getSetBackY();
        // Step height related.
        // TODO: Combine / refine preconditions for step height related.
        // TODO: || yDistance <= jump estimate? 
        if (yDistance <= cc.sfStepHeight && hDistance <= 1.5) { // hDistance is arbitrary, just to confine.
            // Half block step up (definitive).
            // TODO: && hDistance < 0.5  ~  switch to about 2.2 * baseSpeed once available.
            if (setBackYDistance <= Math.max(0.0, 1.3 + 0.2 * data.jumpAmplifier) && to.isOnGround()) {
                // TODO: hDistance > 0.0
                if (data.lastYDist < 0.0 || yDistance <= cc.sfStepHeight && from.isOnGround(cc.sfStepHeight - yDistance)) {
                    return applyLostGround(player, from, true, data, "step");
                }
            }
            // Could step up (but might move to another direction, potentially).
            if (data.lastYDist < 0.0) { // TODO: <= ?
                // Generic could step.
                // TODO: Possibly confine margin depending on side, moving direction (see client code).
                // TODO: Should this also be checked vs. last from?
                if (BlockProperties.isOnGroundShuffled(to.getBlockCache(), from.getX(), from.getY() + cc.sfStepHeight, from.getZ(), to.getX(), to.getY(), to.getZ(), 0.1 + (double) Math.round(from.getWidth() * 500.0) / 1000.0, to.getyOnGround(), 0.0)) {
                    // TODO: Set a data property, so vdist does not trigger (currently: scan for tag)
                    // TODO: !to.isOnGround?
                    return applyLostGround(player, from, false, data, "couldstep");
                }
                // Close by ground miss (client side blocks y move, but allows h move fully/mostly, missing the edge on server side).
                // Possibly confine by more criteria.
                if (!to.isOnGround()) { // TODO: Note, that there may be cases with to on ground (!).
                    if (data.fromX != Double.MAX_VALUE && data.lastHDist != Double.MAX_VALUE && data.lastYDist != Double.MAX_VALUE) {
                        // (Use covered area to last from.)
                        // TODO: Plausible: last to is about this from?
                        // TODO: Otherwise cap max. amount (seems not really possible, could confine by passable checking).
                        // TODO: Might estimate by the yDist from before last from (cap x2 and y2).
                        // TODO: A ray-tracing version of isOnground?
                        if (lostGroundEdgeAsc(player, from.getBlockCache(), from.getWorld(), from.getX(), from.getY(), from.getZ(), from.getWidth(), from.getyOnGround(), data, "asc1")) {
                            return true;
                        }

                        // Special cases.
                        if (yDistance == 0.0 && data.lastYDist <= -0.23 && (hDistance <= data.lastHDist * 1.1)) {
                            // Similar to couldstep, with 0 y-distance but slightly above any ground nearby (no micro move!).
                            // TODO: (hDistance <= data.sfLastHDist || hDistance <= hAllowedDistance)
                            // TODO: Confining in x/z direction in general: should detect if collided in that direction (then skip the x/z dist <= last time).
                            // TODO: Temporary test (should probably be covered by one of the above instead).
                            // TODO: Code duplication with edgeasc7 below.
                            if (lostGroundEdgeAsc(player, from.getBlockCache(), to.getWorld(), to.getX(), to.getY(), to.getZ(), from.getX(), from.getY(), from.getZ(), hDistance, to.getWidth(), 0.3, data, "asc5")) {
                                return true;
                            }
                        }
                    }
                    else if (from.isOnGround(from.getyOnGround(), 0.0625, 0.0)) {
                        // (Minimal margin.)
                        //data.sfLastAllowBunny = true; // TODO: Maybe a less powerful flag (just skipping what is necessary).
                        return applyLostGround(player, from, false, data, "edgeasc2"); // Maybe true ?
                    }
                }
            }
        }
        // Nothing found.
        return false;
    }

    /**
     * Preconditions move dist is 0, not on ground, last h dist > 0, last y dist < 0.
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
    private boolean lostGroundStill(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
        if (data.lastYDist <= -0.23) {
            // TODO: Code duplication with edgeasc5 above.
            if (lostGroundEdgeAsc(player, from.getBlockCache(), to.getWorld(), to.getX(), to.getY(), to.getZ(), from.getX(), from.getY(), from.getZ(), hDistance, to.getWidth(), 0.3, data, "asc7")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vertical collision with ground on client side, shifting over an edge with the horizontal move. Using last from from MovingData.
     * @param player
     * @param blockCache
     * @param world
     * @param x1 Target position.
     * @param y1
     * @param z1
     * @param width
     * @param yOnGround
     * @param data
     * @param tag
     * @return
     */
    private final boolean lostGroundEdgeAsc(final Player player, final BlockCache blockCache, final World world, final double x1, final double y1, final double z1, final double width, final double yOnGround, final MovingData data, final String tag) {
        return lostGroundEdgeAsc(player, blockCache, world, x1, y1, z1, data.fromX, data.fromY, data.fromZ, data.lastHDist, width, yOnGround, data, tag);
    }

    private final boolean lostGroundEdgeAsc(final Player player, final BlockCache blockCache, final World world, final double x1, final double y1, final double z1, double x2, final double y2, double z2, final double hDistance2, final double width, final double yOnGround, final MovingData data, final String tag) {
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
        final double xzMargin = Math.round(width * 500.0) / 1000.0; // Bounding box "radius" at some resolution.
        // (We don't add another xz-margin here, as the move should cover ground.)
        if (BlockProperties.isOnGroundShuffled(blockCache, x1, y1, z1, x2, y1, z2, xzMargin, yOnGround, 0.0)) {
            //data.sfLastAllowBunny = true; // TODO: Maybe a less powerful flag (just skipping what is necessary).
            // TODO: data.fromY for set back is not correct, but currently it is more safe (needs instead: maintain a "distance to ground").
            return applyLostGround(player, new Location(world, x2, y2, z2), true, data, "edge" + tag); // Maybe true ?
        } else {
            return false;
        }
    }

    /**
     * Check if a ground-touch has been lost due to event-sending-frequency or other reasons.<br>
     * This is for descending "mildly" only (-0.5 <= yDistance <= 0).
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
    private boolean lostGroundDescend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
        // TODO: re-organize for faster exclusions (hDistance, yDistance).
        // TODO: more strict conditions 

        final double setBackYDistance = to.getY() - data.getSetBackY();

        // Collides vertically.
        // Note: checking loc should make sense, rather if loc is higher than from?
        if (yDistance < 0.0 && !to.isOnGround() && from.isOnGround(from.getY() - to.getY() + 0.001)) {
            // Test for passability of the entire box, roughly from feet downwards.
            // TODO: Efficiency with Location instances.
            // TODO: Full bounds check (!).
            final Location ref = from.getLocation();
            ref.setY(to.getY());
            if (BlockProperties.isPassable(from.getLocation(), ref)) {
                // TODO: Needs new model (store detailed on-ground properties).
                return applyLostGround(player, from, false, data, "vcollide");
            }
        }

        if (data.sfJumpPhase <= 7) {
            // Check for sprinting down blocks etc.
            if (data.lastYDist <= yDistance && setBackYDistance < 0 && !to.isOnGround()) {
                // TODO: setbackydist: <= - 1.0 or similar
                // TODO: <= 7 might work with speed II, not sure with above.
                // TODO: account for speed/sprint
                // TODO: account for half steps !?
                if (from.isOnGround(0.6, 0.4, 0.0, 0L) ) {
                    // TODO: further narrow down bounds ?
                    // Temporary "fix".
                    // TODO: Seems to virtually always be preceded by a "vcollide" move.
                    return applyLostGround(player, from, true, data, "pyramid");
                }
            }

            // Check for jumping up strange blocks like flower pots on top of other blocks.
            if (yDistance == 0.0 && data.lastYDist > 0.0 && data.lastYDist < 0.25 && data.sfJumpPhase <= Math.max(0, 6 + data.jumpAmplifier * 3.0) && setBackYDistance > 1.0 && setBackYDistance < Math.max(0.0, 1.5 + 0.2 * data.jumpAmplifier) && !to.isOnGround()) {
                // TODO: confine by block types ?
                if (from.isOnGround(0.25, 0.4, 0, 0L) ) {
                    // Temporary "fix".
                    //data.sfThisAllowBunny = true;
                    return applyLostGround(player, from, true, data, "ministep");
                }
            }
        }
        // Lost ground while falling onto/over edges of blocks.
        if (yDistance < 0 && hDistance <= 1.5 && data.lastYDist < 0.0 && yDistance > data.lastYDist && !to.isOnGround()) {
            // TODO: Should this be an extra lost-ground(to) check, setting toOnGround  [for no-fall no difference]?
            // TODO: yDistance <= 0 might be better.
            // Also clear accounting data.
            //			if (to.isOnGround(0.5) || from.isOnGround(0.5)) {
            if (from.isOnGround(0.5, 0.2, 0) || to.isOnGround(0.5, Math.min(0.2, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance))) {
                return applyLostGround(player, from, true, data, "edgedesc");
            }
        }

        // Nothing found.
        return false;
    }

    /**
     * Check if a ground-touch has been lost due to event-sending-frequency or other reasons.<br>
     * This is for fast descending only (yDistance < -0.5).
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
    private boolean lostGroundFastDescend(final Player player, final PlayerLocation from, final PlayerLocation to, final double hDistance, final double yDistance, final boolean sprinting, final MovingData data, final MovingConfig cc) {
        // TODO: re-organize for faster exclusions (hDistance, yDistance).
        // TODO: more strict conditions 
        // Lost ground while falling onto/over edges of blocks.
        if (yDistance > data.lastYDist && !to.isOnGround()) {
            // TODO: Should this be an extra lost-ground(to) check, setting toOnGround  [for no-fall no difference]?
            // TODO: yDistance <= 0 might be better.
            // Also clear accounting data.
            // TODO: stairs ?
            // TODO: Can it be safe to only check to with raised margin ? [in fact should be checked from higher yMin down]
            // TODO: Interpolation method (from to)?
            if (from.isOnGround(0.5, 0.2, 0) || to.isOnGround(0.5, Math.min(0.3, 0.01 + hDistance), Math.min(0.1, 0.01 + -yDistance))) {
                // (Usually yDistance should be -0.078)
                return applyLostGround(player, from, true, data, "fastedge");
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
    private boolean applyLostGround(final Player player, final Location refLoc, final boolean setBackSafe, final MovingData data, final String tag) {
        if (setBackSafe) {
            data.setSetBack(refLoc);
        }
        else {
            // Keep Set-back.
        }
        return applyLostGround(player, data, tag);
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
    private boolean applyLostGround(final Player player, final PlayerLocation refLoc, final boolean setBackSafe, final MovingData data, final String tag) {
        // Set the new setBack and reset the jumpPhase.
        if (setBackSafe) {
            data.setSetBack(refLoc);
        }
        else {
            // Keep Set-back.
        }
        return applyLostGround(player, data, tag);
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
    private boolean applyLostGround(final Player player, final MovingData data, final String tag) {
        // Reset the jumpPhase.
        // ? set jumpphase to 1 / other, depending on stuff ?
        data.sfJumpPhase = 0;
        data.jumpAmplifier = getJumpAmplifier(player);
        data.clearAccounting();
        // Tell NoFall that we assume the player to have been on ground somehow.
        data.noFallAssumeGround = true;
        tags.add("lostground_" + tag);
        return true;
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
    private final Location handleViolation(final long now, final double result, final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc)
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
        if (executeActions(vd)) {
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
    protected final void handleHoverViolation(final Player player, final Location loc, final MovingConfig cc, final MovingData data) {
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
        if (executeActions(vd)) {
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
    private final boolean hackCobweb(final Player player, final MovingData data, final PlayerLocation to, 
            final long now, final double vDistanceAboveLimit)
    {
        if (now - data.sfCobwebTime > 3000) {
            data.sfCobwebTime = now;
            data.sfCobwebVL = vDistanceAboveLimit * 100D;
        } else {
            data.sfCobwebVL += vDistanceAboveLimit * 100D;
        }
        if (data.sfCobwebVL < 550) { // Totally random !
            // Silently set back.
            if (!data.hasSetBack()) {
                data.setSetBack(player.getLocation(useLoc)); // ? check moment of call.
                useLoc.setWorld(null);
            }
            data.sfJumpPhase = 0;
            data.lastYDist = data.lastHDist = Double.MAX_VALUE;
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
     * Determine "some jump amplifier": 1 is jump boost, 2 is jump boost II. <br>
     * NOTE: This is not the original amplifier value (use mcAccess for that).
     * @param mcPlayer
     * @return
     */
    protected final double getJumpAmplifier(final Player player) {
        final double amplifier = mcAccess.getJumpAmplifier(player);
        if (amplifier == Double.NEGATIVE_INFINITY) return 0D;
        else return 1D + amplifier;
    }

    /**
     * Syso debug output.
     * @param player
     * @param data
     * @param cc
     * @param hDistance
     * @param hAllowedDistance
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
        final StringBuilder builder = new StringBuilder(500);
        final String hBuf = (data.sfHorizontalBuffer < 1.0 ? ((" hbuf=" + StringUtil.fdec3.format(data.sfHorizontalBuffer))) : "");
        final String lostSprint = (data.lostSprintCount > 0 ? (" lostSprint=" + data.lostSprintCount) : "");
        final String hVelUsed = hFreedom > 0 ? " hVelUsed=" + StringUtil.fdec3.format(hFreedom) : "";
        builder.append(player.getName() + " SurvivalFly\nground: " + (data.noFallAssumeGround ? "(assumeonground) " : "") + (fromOnGround ? "onground -> " : (resetFrom ? "resetcond -> " : "--- -> ")) + (toOnGround ? "onground" : (resetTo ? "resetcond" : "---")) + ", jumpphase: " + data.sfJumpPhase + ", liftoff: " + data.liftOffEnvelope.name());
        final String dHDist = (data.lastHDist != Double.MAX_VALUE && Math.abs(data.lastHDist - hDistance) > 0.0005) ? ("(" + (hDistance > data.lastHDist ? "+" : "") + StringUtil.fdec3.format(hDistance - data.lastHDist) + ")") : "";
        builder.append("\n" + " hDist: " + StringUtil.fdec3.format(hDistance) + dHDist + " / " +  StringUtil.fdec3.format(hAllowedDistance) + hBuf + lostSprint + hVelUsed + " , vDist: " + StringUtil.fdec3.format(yDistance) + (data.lastYDist == Double.MAX_VALUE ? "" : (" (" + (yDistance > data.lastYDist ? "+" : "") + StringUtil.fdec3.format(yDistance - data.lastYDist) + ")")) + " / " + StringUtil.fdec3.format(vAllowedDistance) + ", sby=" + (data.hasSetBack() ? (data.getSetBackY() + " (" + StringUtil.fdec3.format(to.getY() - data.getSetBackY()) + " / " + data.liftOffEnvelope.getMaxJumpHeight(data.jumpAmplifier) + ")") : "?"));
        if (data.verVelUsed != null) {
            builder.append(" vVelUsed= " + data.verVelUsed + " ");
        }
        data.addVerticalVelocity(builder);
        //		if (data.horizontalVelocityCounter > 0 || data.horizontalFreedom >= 0.001) {
        //			builder.append("\n" + player.getName() + " horizontal freedom: " +  StringUtil.fdec3.format(data.horizontalFreedom) + " (counter=" + data.horizontalVelocityCounter +"/used="+data.horizontalVelocityUsed);
        //		}
        data.addHorizontalVelocity(builder);
        if (!resetFrom && !resetTo) {
            if (cc.survivalFlyAccountingV && data.vDistAcc.count() > data.vDistAcc.bucketCapacity()) builder.append("\n" + " vacc=" + data.vDistAcc.toInformalString());
        }
        if (player.isSleeping()) tags.add("sleeping");
        if (player.getFoodLevel() <= 5 && player.isSprinting()) {
            // Exception: does not take into account latency.
            tags.add("lowfoodsprint");
        }
        if (!tags.isEmpty()) builder.append("\n" + " tags: " + StringUtil.join(tags, "+"));
        builder.append("\n");
        //		builder.append(data.stats.getStatsStr(false));
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, builder.toString());
    }

    private void logPostViolationTags(final Player player) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " SurvivalFly Post violation handling tag update:\n" + StringUtil.join(tags, "+"));
    }

}
