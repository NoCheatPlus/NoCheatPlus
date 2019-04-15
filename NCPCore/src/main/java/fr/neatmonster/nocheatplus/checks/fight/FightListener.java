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
package fr.neatmonster.nocheatplus.checks.fight;

import java.util.Iterator;

import fr.neatmonster.nocheatplus.permissions.PermissionCache;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.inventory.Items;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.location.tracking.LocationTrace;
import fr.neatmonster.nocheatplus.checks.moving.location.tracking.LocationTrace.ITraceEntry;
import fr.neatmonster.nocheatplus.checks.moving.model.LiftOffEnvelope;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveInfo;
import fr.neatmonster.nocheatplus.checks.moving.player.UnusedVelocity;
import fr.neatmonster.nocheatplus.checks.moving.util.AuxMoving;
import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.checks.moving.velocity.VelocityFlags;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeEnchant;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.compat.IBridgeCrossPlugin;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.components.registry.feature.JoinLeaveListener;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;
import fr.neatmonster.nocheatplus.utilities.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * Central location to listen to events that are relevant for the fight checks.<br>
 * This listener is registered after the CombinedListener.
 * 
 * @see FightEvent
 */
public class FightListener extends CheckListener implements JoinLeaveListener{

    /** The angle check. */
    private final Angle       angle       = addCheck(new Angle());

    /** The critical check. */
    private final Critical    critical    = addCheck(new Critical());

    /** The direction check. */
    private final Direction   direction   = addCheck(new Direction());

    /** Faster health regeneration check. */
    private final FastHeal fastHeal		  = addCheck(new FastHeal());

    /** The god mode check. */
    private final GodMode     godMode     = addCheck(new GodMode());

    /** The no swing check. */
    private final NoSwing     noSwing     = addCheck(new NoSwing());

    /** The reach check. */
    private final Reach       reach       = addCheck(new Reach());

    /** The self hit check */
    private final SelfHit     selfHit     = addCheck(new SelfHit());

    /** The speed check. */
    private final Speed       speed       = addCheck(new Speed());

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc1 = new Location(null, 0, 0, 0);

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc2 = new Location(null, 0, 0, 0);

    private final AuxMoving auxMoving = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(AuxMoving.class);

    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idCancelDead = counters.registerKey("cancel.dead");

    // Assume it to stay the same all time.
    private final IGenericInstanceHandle<IBridgeCrossPlugin> crossPlugin = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(IBridgeCrossPlugin.class);

    public FightListener() {
        super(CheckType.FIGHT);
    }

    /**
     * A player attacked something with DamageCause ENTITY_ATTACK.
     * 
     * @param player
     *            The attacking player.
     * @param damaged
     * @param originalDamage
     *            Damage before applying modifiers.
     * @param finalDamage
     *            Damage after applying modifiers.
     * @param tick
     * @param data
     * @return
     */
    private boolean handleNormalDamage(final Player player, final boolean attackerIsFake,
            final Entity damaged, final boolean damagedIsFake,
            final double originalDamage, final double finalDamage, final int tick, final FightData data) {
        final FightConfig cc = FightConfig.getConfig(player);

        // Hotfix attempt for enchanted books.
        // TODO: maybe a generalized version for the future...
        // Illegal enchantments hotfix check.
        if (Items.checkIllegalEnchantmentsAllHands(player)) {
            return true;
        }

        boolean cancelled = false;

        final String worldName = player.getWorld().getName();
        final long now = System.currentTimeMillis();
        final boolean worldChanged = !worldName.equals(data.lastWorld);

        final Location loc =  player.getLocation(useLoc1);
        //        // Bad pitch/yaw, just in case.
        // 		if (LocUtil.needsDirectionCorrection(useLoc1.getYaw(), useLoc1.getPitch())) {
        // 			mcAccess.correctDirection(player);
        // 			player.getLocation(useLoc1);
        // 		}
        final Location damagedLoc = damaged.getLocation(useLoc2);
        //        final double targetDist = CheckUtils.distance(loc, targetLoc); // TODO: Calculate distance as is done in fight.reach !
        final double targetMove;
        final int tickAge;
        final long msAge; // Milliseconds the ticks actually took.
        final double normalizedMove; // Blocks per second.
        // TODO: relative distance (player - target)!
        // TODO: Use trace for this ?
        if (data.lastAttackedX == Double.MAX_VALUE || tick < data.lastAttackTick || worldChanged || tick - data.lastAttackTick > 20) {
            // TODO: 20 ?
            tickAge = 0;
            targetMove = 0.0;
            normalizedMove = 0.0;
            msAge = 0;
        }
        else {
            tickAge = tick - data.lastAttackTick;
            // TODO: Maybe use 3d distance if dy(normalized) is too big. 
            targetMove = TrigUtil.distance(data.lastAttackedX, data.lastAttackedZ, damagedLoc.getX(), damagedLoc.getZ());
            msAge = (long) (50f * TickTask.getLag(50L * tickAge, true) * (float) tickAge);
            normalizedMove = msAge == 0 ? targetMove : targetMove * Math.min(20.0, 1000.0 / (double) msAge);
        }
        // TODO: calculate factor for dists: ticks * 50 * lag

        // TODO: dist < width => skip some checks (direction, ..)

        final LocationTrace damagedTrace;
        final Player damagedPlayer;
        if (damaged instanceof Player) {
            damagedPlayer = (Player) damaged;
            //        	// Bad pitch/yaw, just in case.
            //     		if (LocUtil.needsDirectionCorrection(useLoc2.getYaw(), useLoc2.getPitch())) {
            //     			mcAccess.correctDirection(damagedPlayer);
            //     			damagedPlayer.getLocation(useLoc2);
            //     		}
            // Log.
            if (data.debug && PermissionCache.hasPermission(damagedPlayer, Permissions.ADMINISTRATION_DEBUG)) {
                damagedPlayer.sendMessage("Attacked by " + player.getName() + ": inv=" + mcAccess.getHandle().getInvulnerableTicks(damagedPlayer) + " ndt=" + damagedPlayer.getNoDamageTicks());
            }
            // Check for self hit exploits (mind that projectiles are excluded from this.)
            if (selfHit.isEnabled(player) && selfHit.check(player, damagedPlayer, data, cc)) {
                cancelled = true;
            }
            // Get+update the damaged players.
            // TODO: Problem with NPCs: data stays (not a big problem).
            // (This is done even if the event has already been cancelled, to keep track, if the player is on a horse.)
            damagedTrace = MovingData.getData(damagedPlayer).updateTrace(damagedPlayer, damagedLoc, tick, damagedIsFake ? null : mcAccess.getHandle());
        }
        else {
            damagedPlayer = null; // TODO: This is a temporary workaround.
            // Use a fake trace.
            // TODO: Provide for entities too? E.g. one per player, or a fully fledged bookkeeping thing (EntityData).
            //final MovingConfig mcc = MovingConfig.getConfig(damagedLoc.getWorld().getName());
            damagedTrace = null; //new LocationTrace(mcc.traceSize, mcc.traceMergeDist);
            //damagedTrace.addEntry(tick, damagedLoc.getX(), damagedLoc.getY(), damagedLoc.getZ());
        }

        // Log generic properties of this attack.
        if (data.debug) {
            debug(player, "Attacks " + (damagedPlayer == null ? ("entity " + damaged.getType()) : ("player" + damagedPlayer.getName())) + " damage=" + (finalDamage == originalDamage ? finalDamage : (originalDamage + "/" + finalDamage)));
        }

        // Can't fight dead.
        if (cc.cancelDead) {
            if (damaged.isDead()) {
                cancelled = true;
            }
            // Only allow damaging others if taken damage this tick.
            if (player.isDead() && data.damageTakenByEntityTick != TickTask.getTick()) {
                cancelled = true;
            }
        }

        // LEGACY: 1.9: sweep attack.
        if (BridgeHealth.DAMAGE_SWEEP == null) {
            // TODO: Account for charge/meter thing?
            final int locHashCode = LocUtil.hashCode(loc);
            if (originalDamage == 1.0) {
                // Might be a sweep attack.
                if (tick == data.sweepTick && locHashCode == data.sweepLocationHashCode) {
                    // TODO: Might limit the amount of 'too far off' sweep hits, possibly silent cancel for low frequency.
                    // Could further guard by checking equality of loc to last location.
                    if (data.debug) {
                        debug(player, "(Assume sweep attack follow up damage.)");
                    }
                    return cancelled;
                }
            }
            else {
                // TODO: More side conditions for a sweep attack.
                data.sweepTick = tick;
                data.sweepLocationHashCode = locHashCode;
            }
        }

        // LEGACY: thorns.
        if (BridgeHealth.DAMAGE_THORNS == null && originalDamage <= 4.0 && tick == data.damageTakenByEntityTick 
                && data.thornsId != Integer.MIN_VALUE && data.thornsId == damaged.getEntityId()) {
            // Don't handle further, but do respect selfhit/canceldead.
            // TODO: Remove soon, at least version-dependent.
            data.thornsId = Integer.MIN_VALUE;
            return cancelled;
        }
        else {
            data.thornsId = Integer.MIN_VALUE;
        }

        // Run through the main checks.
        if (!cancelled && speed.isEnabled(player)) {
            if (speed.check(player, now)) {
                cancelled = true;
                // Still feed the improbable.
                if (data.speedVL > 50) {
                    Improbable.check(player, 2f, now, "fight.speed");
                }
                else {
                    Improbable.feed(player, 2f, now);
                }
            }
            else if (normalizedMove > 2.0 && Improbable.check(player, 1f, now, "fight.speed")) {
                // Feed improbable in case of ok-moves too.
                // TODO: consider only feeding if attacking with higher average speed (!)
                cancelled = true;
            }
        }
        // TODO: Consider to always check improbable (first?). At least if config.always or speed or net.attackfrequency are enabled.

        if (!cancelled && critical.isEnabled(player) && critical.check(player, loc, data, cc)) {
            // TODO: Check config for settings.
            cancelled = true;
        }

        if (!cancelled && noSwing.isEnabled(player) && noSwing.check(player, data, cc)) {
            cancelled = true;
        }

        if (!cancelled && player.isBlocking() && !PermissionCache.hasPermission(player, Permissions.MOVING_SURVIVALFLY_BLOCKING)) {
            // TODO: Permission ?
            cancelled = true;
        }

        if (!cancelled) {
            final boolean reachEnabled = reach.isEnabled(player);
            final boolean directionEnabled = direction.isEnabled(player);
            if (reachEnabled || directionEnabled) {
                if (damagedTrace != null) {
                    // Checks that use the LocationTrace instance of the attacked entity/player.
                    cancelled = locationTraceChecks(player, loc, data, cc, damaged, damagedIsFake, damagedLoc, damagedTrace, tick, now, reachEnabled, directionEnabled);
                }
                else {
                    // Still use the classic methods for non-players. maybe[]
                    if (reachEnabled && reach.check(player, loc, damaged, damagedIsFake, damagedLoc, data, cc)) {
                        cancelled = true;
                    }

                    if (directionEnabled && direction.check(player, loc, damaged, damagedIsFake, damagedLoc, data, cc)) {
                        cancelled = true;
                    }
                }
            }
        }

        // Check angle with allowed window.
        // TODO: Actual angle needs to be related to the best matching trace element(s) (loop checks).
        if (angle.isEnabled(player)) {
            // TODO: Revise, use own trace.
            // The "fast turning" checks are checked in any case because they accumulate data.
            // Improbable yaw changing: Moving events might be missing up to a ten degrees change.
            if (Combined.checkYawRate(player, loc.getYaw(), now, worldName, cc.yawRateCheck)) {
                // (Check or just feed).
                // TODO: Work into this somehow attacking the same aim and/or similar aim position (not cancel then).
                cancelled = true;
            }
            // Angle check.
            if (angle.check(player, loc, damaged, worldChanged, data, cc)) {
                if (!cancelled && data.debug) {
                    debug(player, "FIGHT_ANGLE cancel without yawrate cancel.");
                }
                cancelled = true;
            }
        }

        // Set values.
        data.lastWorld = worldName;
        data.lastAttackTick = tick;
        data.lastAttackedX = damagedLoc.getX();
        data.lastAttackedY = damagedLoc.getY();
        data.lastAttackedZ = damagedLoc.getZ();
        //    	data.lastAttackedDist = targetDist;

        // Care for the "lost sprint problem": sprint resets, client moves as if still...
        // TODO: If this is just in-air, model with friction, so this can be removed.
        // TODO: Use stored distance calculation same as reach check?
        // TODO: For pvp: make use of "player was there" heuristic later on.
        // TODO: Confine further with simple pre-conditions.
        // TODO: Evaluate if moving traces can help here.
        if (!cancelled && TrigUtil.distance(loc.getX(), loc.getZ(), damagedLoc.getX(), damagedLoc.getZ()) < 4.5) {
            final MovingData mData = MovingData.getData(player);
            // Check if fly checks is an issue at all, re-check "real sprinting".
            final PlayerMoveData lastMove = mData.playerMoves.getFirstPastMove();
            if (lastMove.valid && mData.liftOffEnvelope == LiftOffEnvelope.NORMAL) {
                final double hDist = TrigUtil.xzDistance(loc, lastMove.from);
                if (hDist >= 0.23) {
                    // TODO: Might need to check hDist relative to speed / modifiers.
                    final MovingConfig mCc = MovingConfig.getConfig(player);
                    final PlayerMoveInfo moveInfo = auxMoving.usePlayerMoveInfo();
                    moveInfo.set(player, loc, null, mCc.yOnGround);
                    if (now <= mData.timeSprinting + mCc.sprintingGrace && MovingUtil.shouldCheckSurvivalFly(player, moveInfo.from, mData, mCc)) {
                        // Judge as "lost sprint" problem.
                        // TODO: What would mData.lostSprintCount > 0  mean here?
                        mData.lostSprintCount = 7;
                        if ((data.debug || mCc.debug) && BuildParameters.debugLevel > 0) {
                            debug(player, "lostsprint: hDist to last from: " + hDist + " | targetdist=" + TrigUtil.distance(loc.getX(), loc.getZ(), damagedLoc.getX(), damagedLoc.getZ()) + " | sprinting=" + player.isSprinting() + " | food=" + player.getFoodLevel() +" | hbuf=" + mData.sfHorizontalBuffer);
                        }
                    }
                    auxMoving.returnPlayerMoveInfo(moveInfo);
                }
            }
        }

        // Generic attacking penalty.
        // (Cancel after sprinting hacks, because of potential fp).
        if (!cancelled && data.attackPenalty.isPenalty(now)) {
            cancelled = true;
            if (data.debug) {
                debug(player, "~ attack penalty.");
            }
        }

        // Cleanup.
        useLoc1.setWorld(null);
        useLoc2.setWorld(null);

        return cancelled;
    }

    /**
     * Quick split-off: Checks using a location trace.
     * @param player
     * @param loc
     * @param data
     * @param cc
     * @param damaged
     * @param damagedPlayer
     * @param damagedLoc
     * @param damagedTrace
     * @param tick
     * @param reachEnabled
     * @param directionEnabled
     * @return If to cancel (true) or not (false).
     */
    private boolean locationTraceChecks(final Player player, final Location loc, 
            final FightData data, final FightConfig cc, 
            final Entity damaged, final boolean damagedIsFake,
            final Location damagedLoc, LocationTrace damagedTrace, 
            final long tick, final long now,  final boolean reachEnabled, final boolean directionEnabled) {
        // TODO: Order / splitting off generic stuff.
        /*
         * TODO: Abstract: interface with common setup/loop/post routine, only
         * pass the ACTIVATED checks on to here (e.g. IFightLoopCheck...
         * loopChecks). Support an arbitrary number of loop checks, special
         * behavior -> interface and/or order within loopChecks.
         */
        boolean cancelled = false;

        // (Might pass generic context to factories, for shared + heavy properties.)
        final ReachContext reachContext = reachEnabled ? reach.getContext(player, loc, damaged, damagedLoc, data, cc) : null;
        final DirectionContext directionContext = directionEnabled ? direction.getContext(player, loc, damaged, damagedIsFake, damagedLoc, data, cc) : null;

        final long traceOldest = tick - cc.loopMaxLatencyTicks; // TODO: Set by latency-window.
        // TODO: Iterating direction, which, static/dynamic choice.
        final Iterator<ITraceEntry> traceIt = damagedTrace.maxAgeIterator(traceOldest);

        boolean violation = true; // No tick with all checks passed.
        boolean reachPassed = !reachEnabled; // Passed individually for some tick.
        boolean directionPassed = !directionEnabled; // Passed individually for some tick.
        // TODO: Maintain a latency estimate + max diff and invalidate completely (i.e. iterate from latest NEXT time)], or just max latency.
        // TODO: Consider a max-distance to "now", for fast invalidation.
        long latencyEstimate = -1;
        ITraceEntry successEntry = null;
        while (traceIt.hasNext()) {
            final ITraceEntry entry = traceIt.next();
            // Simplistic just check both until end or hit.
            // TODO: Other default distances/tolerances.
            boolean thisPassed = true;
            if (reachEnabled) {
                if (reach.loopCheck(player, loc, damaged, entry, reachContext, data, cc)) {
                    thisPassed = false;
                }
                else {
                    reachPassed = true;
                }
            }
            // TODO: Efficiency: don't check at all, if strict and !thisPassed.
            if (directionEnabled && (reachPassed || !directionPassed)) {
                if (direction.loopCheck(player, loc, damaged, entry, directionContext, data, cc)) {
                    thisPassed = false;
                }
                else {
                    directionPassed = true;
                }
            }
            if (thisPassed) {
                // TODO: Log/set estimated latency.
                violation = false;
                latencyEstimate = now - entry.getTime();
                successEntry = entry;
                break;
            }
        }
        // TODO: How to treat mixed state: violation && reachPassed && directionPassed [current: use min violation // thinkable: silent cancel, if actions have cancel (!)]
        // TODO: Adapt according to strictness settings?
        // TODO: violation vs. reachPassed + directionPassed (current: fail one = fail all).
        if (reachEnabled) {
            // TODO: Might ignore if already cancelled by mixed/silent cancel.
            if (reach.loopFinish(player, loc, damaged, reachContext, successEntry, violation, data, cc)) {
                cancelled = true;
            }
        }
        if (directionEnabled) {
            // TODO: Might ignore if already cancelled.
            if (direction.loopFinish(player, loc, damaged, directionContext, violation, data, cc)) {
                cancelled = true;
            }
        }
        // TODO: Log exact state, probably record min/max latency (individually).
        if (data.debug && latencyEstimate >= 0) {
            debug(player, "Latency estimate: " + latencyEstimate + " ms."); // FCFS rather, at present.
        }
        return cancelled;
    }

    /**
     * We listen to EntityDamage events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(final EntityDamageEvent event) {

        final Entity damaged = event.getEntity();
        final Player damagedPlayer = damaged instanceof Player ? (Player) damaged : null;
        final FightData damagedData = damagedPlayer == null ? null : FightData.getData(damagedPlayer);
        final boolean damagedIsDead = damaged.isDead();
        final boolean damagedIsFake = !crossPlugin.getHandle().isNativeEntity(damaged);
        if (damagedPlayer != null && !damagedIsDead) {
            // God mode check.
            // (Do not test the savage.)
            if (godMode.isEnabled(damagedPlayer)
                    && godMode.check(damagedPlayer, damagedIsFake, BridgeHealth.getDamage(event), damagedData)) {
                // It requested to "cancel" the players invulnerability, so set their noDamageTicks to 0.
                damagedPlayer.setNoDamageTicks(0);
            }
            // Adjust buffer for fast heal checks.
            if (BridgeHealth.getHealth(damagedPlayer) >= BridgeHealth.getMaxHealth(damagedPlayer)) {
                // TODO: Might use the same FightData instance for GodMode.
                if (damagedData.fastHealBuffer < 0) {
                    // Reduce negative buffer with each full health.
                    damagedData.fastHealBuffer /= 2;
                }
                // Set reference time.
                damagedData.fastHealRefTime = System.currentTimeMillis();
            }
            // TODO: TEST: Check unused velocity for the damaged player. (Needs more efficient pre condition checks.)
            if (damagedData.debug) {
                // TODO: Pass result to further checks for reference?
                UnusedVelocity.checkUnusedVelocity(damagedPlayer, CheckType.FIGHT);
            }
        }
        // Attacking entities.
        if (event instanceof EntityDamageByEntityEvent) {
            onEntityDamageByEntity(damaged, damagedPlayer, damagedIsDead, damagedIsFake, 
                    damagedData, (EntityDamageByEntityEvent) event);
        }
    }

    /**
     * (Not an event listener method: call from EntityDamageEvent handler at
     * EventPriority.LOWEST.)
     * 
     * @param damagedPlayer
     * @param damagedIsDead
     * @param damagedData
     * @param event
     */
    private void onEntityDamageByEntity(final Entity damaged, final Player damagedPlayer, 
            final boolean damagedIsDead, final boolean damagedIsFake, 
            final FightData damagedData, final EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final int tick = TickTask.getTick();
        if (damagedPlayer != null && !damagedIsDead) {
            // TODO: check once more when to set this (!) in terms of order.
            damagedData.damageTakenByEntityTick = tick;
            // Legacy workaround: Before thorns damage cause existed (orchid).
            // TODO: Disable efficiently, if the damage cause exists.
            // TODO: Remove workaround anyway, if the issue only exists on a minor CB version.
            if (BridgeEnchant.hasThorns(damagedPlayer)) {
                // Remember the id of the attacker to allow counter damage.
                damagedData.thornsId = damager.getEntityId();
            }
            else {
                damagedData.thornsId = Integer.MIN_VALUE;
            }
        }
        final DamageCause damageCause = event.getCause();
        final Player player = damager instanceof Player ? (Player) damager : null;
        Player attacker = player;
        // TODO: deobfuscate.
        if (damager instanceof TNTPrimed) {
            final Entity source = ((TNTPrimed) damager).getSource();
            if (source instanceof Player) {
                attacker = (Player) source;
            }
        }
        final FightData attackerData;
        if (attacker != null) {
            attackerData = FightData.getData(attacker);
            // TODO: TEST: Check unused velocity for the attacker. (Needs more efficient pre condition checks.)
            if (attackerData.debug) {
                // TODO: Pass result to further checks for reference?
                // TODO: attackerData.debug flag.
                // TODO: Fake players likely have unused velocity, just clear unused?
                UnusedVelocity.checkUnusedVelocity(attacker, CheckType.FIGHT);
            }
            // Workaround for subsequent melee damage eventsfor explosions. TODO: Legacy or not, need a KB.
            if (damageCause == DamageCause.BLOCK_EXPLOSION || damageCause == DamageCause.ENTITY_EXPLOSION) {
                // NOTE: Pigs don't have data.
                attackerData.lastExplosionEntityId = damaged.getEntityId();
                attackerData.lastExplosionDamageTick = tick;
                return;
            }
        }
        else {
            attackerData = null;
        }
        if (player != null) {
            // Actual fight checks.
            if (damageCause == DamageCause.ENTITY_ATTACK) {
                // TODO: Might/should skip the damage comparison, though checking on lowest priority.
                if (damaged.getEntityId() == attackerData.lastExplosionEntityId && tick == attackerData.lastExplosionDamageTick) {
                    attackerData.lastExplosionDamageTick = -1;
                    attackerData.lastExplosionEntityId = Integer.MAX_VALUE;
                }
                // Prevent attacking if a set back is scheduled.
                else if (MovingUtil.hasScheduledPlayerSetBack(player)) {
                    if (attackerData.debug) {
                        // Use fight data flag for efficiency.
                        debug(attacker, "Prevent melee attack, due to a scheduled set back.");
                    }
                    event.setCancelled(true);
                }
                // Ordinary melee damage handling.
                else if (handleNormalDamage(player, !crossPlugin.getHandle().isNativePlayer(player),
                        damaged, damagedIsFake,
                        BridgeHealth.getOriginalDamage(event), BridgeHealth.getFinalDamage(event), 
                        tick, attackerData)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageMonitor(final EntityDamageEvent event) {
        final Entity damaged = event.getEntity();
        if (damaged instanceof Player) {
            final Player damagedPlayer = (Player) damaged;
            final FightData damagedData = FightData.getData(damagedPlayer);
            final int ndt = damagedPlayer.getNoDamageTicks();
            if (damagedData.lastDamageTick == TickTask.getTick() && damagedData.lastNoDamageTicks != ndt) {
                // Plugin compatibility thing.
                damagedData.lastNoDamageTicks = ndt;
            }
            // Knock-back calculation (1.8: events only fire if they would count by ndt).
            switch (event.getCause()) {
                case ENTITY_ATTACK:
                    if (event instanceof EntityDamageByEntityEvent) {
                        final Entity entity = ((EntityDamageByEntityEvent) event).getDamager();
                        if ((entity instanceof Player) && !damagedPlayer.isInsideVehicle() && FightConfig.getConfig(damagedPlayer).knockBackVelocityPvP) {
                            // TODO: Use the velocity event that is sent anyway and replace x/z if 0 (queue max. values).
                            applyKnockBack((Player) entity, damagedPlayer, damagedData);
                        }
                    }
                default:
                    break;
            }
        }
    }

    /**
     * Knock-back accounting: Add velocity.
     * @param attacker
     * @param damagedPlayer
     * @param damagedData
     */
    private void applyKnockBack(final Player attacker, final Player damagedPlayer, final FightData damagedData) {
        final double level = getKnockBackLevel(attacker);
        final MovingData mdata = MovingData.getData(damagedPlayer);
        final MovingConfig mcc = MovingConfig.getConfig(damagedPlayer);
        // TODO: How is the direction really calculated?
        // Aim at sqrt(vx * vx + vz * vz, 2), not the exact direction.
        final double vx = level / Math.sqrt(8.0);
        final double vz = vx;
        final double vy = 0.462; // TODO: (0.365) Needs other workarounds, to allow more when moving off ground (vel + GRAVITY_MAX or max jump gain + little?).
        useLoc1.setWorld(null); // Cleanup.
        if (damagedData.debug || mdata.debug) {
            debug(damagedPlayer, "Received knockback level: " + level);
        }
        mdata.addVelocity(damagedPlayer, mcc,  vx, vy, vz, VelocityFlags.ORIGIN_PVP);
    }

    /**
     * Get the knock-back "level", a player can deal based on sprinting +
     * item(s) in hand. The minimum knock-back level is 1.0 (1 + 1 for sprinting
     * + knock-back level), currently capped at 20. Since detecting relevance of
     * items in main vs. off hand, we use the maximum of both, for now.
     * 
     * @param player
     * @return
     */
    private double getKnockBackLevel(final Player player) {
        double level = 1.0; // 1.0 is the minimum knock-back value.
        // TODO: Get the RELEVANT item (...).
        final ItemStack stack = Bridge1_9.getItemInMainHand(player);
        if (!BlockProperties.isAir(stack)) {
            level = (double) stack.getEnchantmentLevel(Enchantment.KNOCKBACK);
        }
        if (player.isSprinting()) {
            // TODO: Lost sprint?
            level += 1.0;
        }
        // Cap the level to something reasonable. TODO: Config / cap the velocity anyway.
        return Math.min(20.0, level);
    }

    /**
     * We listen to death events to prevent a very specific method of doing godmode.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    protected void onEntityDeathEvent(final EntityDeathEvent event) {
        // Only interested in dying players.
        final Entity entity = event.getEntity();
        if (entity instanceof Player) {
            final Player player = (Player) entity;
            if (godMode.isEnabled(player)) {
                godMode.death(player);
            }
        }
    }

    /**
     * We listen to PlayerAnimation events because it is used for arm swinging.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerAnimation(final PlayerAnimationEvent event) {
        // Set a flag telling us that the arm has been swung.
        FightData.getData(event.getPlayer()).noSwingArmSwung = true;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityRegainHealthLow(final EntityRegainHealthEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) entity;
        if (player.isDead() && BridgeHealth.getHealth(player) <= 0.0) {
            // Heal after death.
            // TODO: Problematic. At least skip CUSTOM.
            event.setCancelled(true);
            counters.addPrimaryThread(idCancelDead, 1);
            return;
        }
        if (event.getRegainReason() != RegainReason.SATIATED) {
            return;
        }
        // TODO: EATING reason / peaceful difficulty / regen potion - byCaptain SpigotMC
        if (fastHeal.isEnabled(player) && fastHeal.check(player)) {
            // TODO: Can clients force events with 0-re-gain ?
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) entity;
        final FightData data = FightData.getData(player);
        // Adjust god mode data:
        // Remember the time.
        data.regainHealthTime = System.currentTimeMillis();
        // Set god-mode health to maximum.
        // TODO: Mind that health regain might half the ndt.
        final double health = Math.min(BridgeHealth.getHealth(player) + BridgeHealth.getAmount(event), BridgeHealth.getMaxHealth(player));
        data.godModeHealth = Math.max(data.godModeHealth, health);
    }

    @Override
    public void playerJoins(final Player player) {
    }

    @Override
    public void playerLeaves(final Player player) {
        final FightData data = FightData.getData(player);
        data.angleHits.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        FightData.getData(event.getPlayer()).onWorldChange();
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onItemHeld(final PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final long penalty = FightConfig.getConfig(player).toolChangeAttackPenalty;
        if (penalty > 0 ) {
            FightData.getData(player).attackPenalty.applyPenalty(penalty);
        }
    }

}
