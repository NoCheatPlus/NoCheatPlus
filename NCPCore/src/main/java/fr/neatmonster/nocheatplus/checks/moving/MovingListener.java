package fr.neatmonster.nocheatplus.checks.moving;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.BedLeave;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.components.IData;
import fr.neatmonster.nocheatplus.components.IHaveCheckType;
import fr.neatmonster.nocheatplus.components.INeedConfig;
import fr.neatmonster.nocheatplus.components.INotifyReload;
import fr.neatmonster.nocheatplus.components.IRemoveData;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.components.TickListener;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.DebugUtil;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.build.BuildParameters;

/**
 * Central location to listen to events that are relevant for the moving checks.
 * 
 * @see MovingEvent
 */
public class MovingListener extends CheckListener implements TickListener, IRemoveData, IHaveCheckType, INotifyReload, INeedConfig, JoinLeaveListener{

    /**
     * Check if the player is to be checked by the survivalfly check.
     * @param player
     * @param data
     * @param cc
     * @return
     */
    public static final boolean shouldCheckSurvivalFly(final Player player, final MovingData data, final MovingConfig cc) {
        return cc.survivalFlyCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_SURVIVALFLY) && !player.hasPermission(Permissions.MOVING_SURVIVALFLY) &&
                (cc.ignoreCreative || player.getGameMode() != GameMode.CREATIVE) && !player.isFlying() && (cc.ignoreAllowFlight || !player.getAllowFlight());
    }

    /**
     * Handle an illegal move by a player, attempt to restore a valid location.
     * @param event
     * @param player
     * @param data
     */
    public static void handleIllegalMove(final PlayerMoveEvent event, final Player player, final MovingData data)
    {
        // This might get extended to a check-like thing.
        boolean restored = false;
        final PlayerLocation pLoc = new PlayerLocation(NCPAPIProvider.getNoCheatPlusAPI().getMCAccess(), null);
        // (Mind that we don't set the block cache here).
        final Location loc = player.getLocation();
        if (!restored && data.hasSetBack()) {
            final Location setBack = data.getSetBack(loc); 
            pLoc.set(setBack, player);
            if (!pLoc.isIllegal()) {
                event.setFrom(setBack);
                event.setTo(setBack);
                restored = true;
            }
            else {
                data.resetSetBack();
            }
        } 
        if (!restored) {
            pLoc.set(loc, player);
            if (!pLoc.isIllegal()) {
                event.setFrom(loc);
                event.setTo(loc);
                restored = true;
            }
        }
        pLoc.cleanup();
        if (!restored) {
            // TODO: reset the bounding box of the player ?
            if (MovingConfig.getConfig(player).tempKickIllegal) {
                NCPAPIProvider.getNoCheatPlusAPI().denyLogin(player.getName(), 24L * 60L * 60L * 1000L);
                LogUtil.logSevere("[NCP] could not restore location for " + player.getName() + ", kicking them and deny login for 24 hours");
            } else {
                LogUtil.logSevere("[NCP] could not restore location for " + player.getName() + ", kicking them.");
            }
            CheckUtils.kickIllegalMove(player);
        }
    }


    /** The instance of NoCheatPlus. */
    private final Plugin plugin = Bukkit.getPluginManager().getPlugin("NoCheatPlus"); // TODO

    /** The no fall check. **/
    public final NoFall 			noFall 				= addCheck(new NoFall());

    /** The creative fly check. */
    private final CreativeFly        creativeFly        = addCheck(new CreativeFly());

    /** The more packets check. */
    private final MorePackets        morePackets        = addCheck(new MorePackets());

    /** The more packets vehicle check. */
    private final MorePacketsVehicle morePacketsVehicle = addCheck(new MorePacketsVehicle());

    /** The survival fly check. */
    private final SurvivalFly        survivalFly        = addCheck(new SurvivalFly());

    /** The Passable (simple no-clip) check.*/
    private final Passable passable 					= addCheck(new Passable());

    /** Combined check but handled here (subject to change!) */
    private final BedLeave bedLeave 					= addCheck(new BedLeave());

    /**
     * Unused instances.<br>
     * Might be better due to cascading events in case of actions or plugins doing strange things.
     */
    private final List<MoveInfo> parkedInfo = new ArrayList<MoveInfo>(10);

    /**
     * Store events by player name, in order to invalidate moving processing on higher priority level in case of teleports.
     */
    private final Map<String, PlayerMoveEvent> processingEvents = new HashMap<String, PlayerMoveEvent>();

    /** Player names to check hover for, case insensitive. */
    private final Set<String> hoverTicks = new LinkedHashSet<String>(30); // TODO: Rename

    /** Player names to check enforcing the location for in onTick, case insensitive. */
    private final Set<String> playersEnforce = new LinkedHashSet<String>(30);

    private int hoverTicksStep = 5;

    private final Set<EntityType> normalVehicles = new HashSet<EntityType>();

    /** Location for temporary use with getLocation(useLoc). Always call setWorld(null) after use. Use LocUtil.clone before passing to other API. */
    private final Location useLoc = new Location(null, 0, 0, 0); // TODO: Put to use...

    /** Statistics / debugging counters. */
    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idMoveHandled = counters.registerKey("event.player.move.handled");
    private final int idMoveHandledPos = counters.registerKey("event.player.move.handled.pos");
    private final int idMoveHandledLook = counters.registerKey("event.player.move.handled.look");
    private final int idMoveHandledPosAndLook = counters.registerKey("event.player.move.handled.pos_look");


    public MovingListener() {
        super(CheckType.MOVING);
    }

    /**
     * A workaround for players placing blocks below them getting pushed off the block by NoCheatPlus.
     * 
     * It essentially moves the "setbackpoint" to the top of the newly placed block, therefore tricking NoCheatPlus into
     * thinking the player was already on top of that block and should be allowed to stay there.
     * 
     * It also prevent players from placing a block on a liquid (which is impossible without a modified version of
     * Minecraft).
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        // Ignore players inside a vehicle.
        if (player.isInsideVehicle())
            return;

        final org.bukkit.block.Block block = event.getBlock();
        if (block == null) return;
        final int blockY = block.getY();

        final Material mat = block.getType();

        final MovingData data = MovingData.getData(player);
        if (!creativeFly.isEnabled(player) && !survivalFly.isEnabled(player)) return;

        if (!data.hasSetBack() || blockY + 1D < data.getSetBackY()) return;

        final Location loc = player.getLocation(useLoc);
        if (Math.abs(loc.getX() - 0.5 - block.getX()) <= 1D
                && Math.abs(loc.getZ() - 0.5 - block.getZ()) <= 1D
                && loc.getY() - blockY > 0D && loc.getY() - blockY < 2D
                && (canJumpOffTop(mat) || BlockProperties.isLiquid(mat))) {
            // The creative fly and/or survival fly check is enabled, the
            // block was placed below the player and is
            // solid, so do what we have to do.
            data.setSetBackY(blockY + 1D);
            data.sfJumpPhase = 0;
        }
        useLoc.setWorld(null);
    }

    /**
     * Used for a workaround that resets the set-back for the case of jumping on just placed blocks.
     * @param id
     * @return
     */
    private static boolean canJumpOffTop(final Material blockType) {
        // TODO: Test if this can be removed!
        return BlockProperties.isGround(blockType) || BlockProperties.isSolid(blockType);
    }

    /**
     * We listen to this event to prevent player from flying by sending bed leaving packets.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        CombinedData.getData(event.getPlayer()).wasInBed = true;
    }

    /**
     * We listen to this event to prevent player from flying by sending bed leaving packets.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerBedLeave(final PlayerBedLeaveEvent event) {
        final Player player = event.getPlayer();

        if (bedLeave.isEnabled(player) && bedLeave.checkBed(player)) {
            // Check if the player has to be reset.
            // To "cancel" the event, we teleport the player.
            final Location loc = player.getLocation(useLoc);
            final MovingData data = MovingData.getData(player);
            final MovingConfig cc = MovingConfig.getConfig(player); 
            Location target = null;
            final boolean sfCheck = shouldCheckSurvivalFly(player, data, cc);
            if (sfCheck) {
                target = data.getSetBack(loc);
            }
            if (target == null) {
                // TODO: Add something to guess the best set back location (possibly data.guessSetBack(Location)).
                target = LocUtil.clone(loc);
            }
            if (sfCheck && cc.sfFallDamage && noFall.isEnabled(player)) {
                // Check if to deal damage.
                double y = loc.getY();
                if (data.hasSetBack()) y = Math.min(y, data.getSetBackY());
                noFall.checkDamage(player, data, y);
            }
            // Cleanup
            useLoc.setWorld(null);
            // Teleport.
            data.prepareSetBack(target); // Should be enough. | new Location(target.getWorld(), target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch());
            player.teleport(target, TeleportCause.PLUGIN);// TODO: schedule / other measures ?
        }
        else{
            // Reset bed ...
            CombinedData.getData(player).wasInBed = false;
        }
    }

    /**
     * Just for security, if a player switches between worlds, reset the fly and more packets checks data, because it is
     * definitely invalid now.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        // Maybe this helps with people teleporting through Multiverse portals having problems?
        final Player player = event.getPlayer();
        final MovingData data = MovingData.getData(player);
        final MovingConfig cc = MovingConfig.getConfig(player);
        data.clearFlyData();
        data.clearMorePacketsData();
        // TODO: Might omit this if neither check is activated.
        final Location loc = player.getLocation(useLoc);
        data.setSetBack(loc);
        data.resetPositions(loc);
        data.resetTrace(loc, TickTask.getTick(), cc.traceSize, cc.traceMergeDist);
        if (cc.enforceLocation) {
            // Just in case.
            playersEnforce.add(player.getName());
        }
        useLoc.setWorld(null);
    }

    /**
     * When a player changes their gamemode, all information related to the moving checks becomes invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getNewGameMode() == GameMode.CREATIVE) {
            final MovingData data = MovingData.getData(event.getPlayer());
            data.clearFlyData();
            data.clearMorePacketsData();
            // TODO: Set new set-back if any fly check is activated.
        }
    }

    /**
     * When a player moves, they will be checked for various suspicious behaviors.<br>
     * (lowest priority)
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        // Store the event for monitor level checks.
        final String playerName = player.getName();
        processingEvents.put(playerName, event);

        final MovingData data = MovingData.getData(player);

        final Location from = event.getFrom();
        final Location to = event.getTo();
        Location newTo = null;

        //		// Check problematic yaw/pitch values.
        //		if (LocUtil.needsDirectionCorrection(from.getYaw(), from.getPitch())
        //				|| LocUtil.needsDirectionCorrection(to.getYaw(), to.getPitch())) {
        //			DataManager.getPlayerData(player).task.correctDirection();
        //		}

        // TODO: Check illegal moves here anyway (!).
        // TODO: Check if vehicle move logs correctly (fake).

        // Early return checks (no full processing).
        final boolean earlyReturn;
        if (player.isInsideVehicle()) {
            // No full processing for players in vehicles.
            newTo = onPlayerMoveVehicle(player, from, to, data);
            earlyReturn = true;
        } else if (player.isDead() || player.isSleeping()) {
            // Ignore dead players.
            data.sfHoverTicks = -1;
            earlyReturn = true;
        } else if (player.isSleeping()) {
            // Ignore sleeping playerrs.
            // TODO: sleeping: (which cb!) System.out.println("-> " + player.isSleepingIgnored());
            data.sfHoverTicks = -1;
            earlyReturn = true;
        } else if (!from.getWorld().equals(to.getWorld())) {
            // Keep hover ticks.
            // Ignore changing worlds.
            earlyReturn = true;
        } else {
            earlyReturn = false;
        }

        // TODO: Might log base parts here (+extras).
        if (earlyReturn) {
            // TODO: Remove player from enforceLocation ?
            // TODO: Log "early return: " + tags.
            if (newTo != null) {
                // Illegal Yaw/Pitch.
                if (LocUtil.needsYawCorrection(newTo.getYaw())) {
                    newTo.setYaw(LocUtil.correctYaw(newTo.getYaw()));
                }
                if (LocUtil.needsPitchCorrection(newTo.getPitch())) {
                    newTo.setPitch(LocUtil.correctPitch(newTo.getPitch()));
                }
                // Set.
                // TODO: Reset positions? enforceLocation?
                event.setTo(newTo);
            }
            return;
        }
        // newTo should be null here.

        // TODO: Order this to above "early return"?
        // Set up data / caching.
        final MoveInfo moveInfo;
        if (parkedInfo.isEmpty()) {
            moveInfo = new MoveInfo(mcAccess);
        }
        else {
            moveInfo = parkedInfo.remove(parkedInfo.size() - 1);
        }
        final MovingConfig cc = MovingConfig.getConfig(player);
        moveInfo.set(player, from, to, cc.yOnGround);
        // TODO: Data resetting above ?
        data.noFallAssumeGround = false;
        data.resetTeleported();
        // Debug.
        if (cc.debug) {
            DebugUtil.outputMoveDebug(player, moveInfo.from, moveInfo.to, Math.max(cc.noFallyOnGround, cc.yOnGround), mcAccess);
        }
        // Check for illegal move and bounding box etc.
        if (moveInfo.from.isIllegal() || moveInfo.to.isIllegal()) {
            handleIllegalMove(event, player, data);
            moveInfo.cleanup();
            parkedInfo.add(moveInfo);
            return;
        }

        {
            // Debugging statistics, rather light weight.
            final boolean hasPos = !moveInfo.from.isSamePos(moveInfo.to);
            final boolean hasLook = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
            counters.addPrimaryThread(idMoveHandled, 1);
            final int counterId;
            if (hasPos && hasLook) {
                counterId = idMoveHandledPosAndLook;
            }
            else if (hasPos) {
                counterId = idMoveHandledPos;
            }
            else if (hasLook) {
                counterId = idMoveHandledLook;
            }
            else {
                counterId = -1;
            }
            if (counterId != -1) {
                counters.addPrimaryThread(counterId, 1);
            }
        }

        // The players location.
        final Location loc = (cc.noFallCheck || cc.passableCheck) ? player.getLocation(moveInfo.useLoc) : null;

        // Check for location consistency.
        if (cc.enforceLocation && playersEnforce.contains(playerName)) {
            // NOTE: The setback should not be set before this, even if not yet set.
            // Last to vs. from.
            newTo = enforceLocation(player, from, data);
            // TODO: Remove anyway ? 
            playersEnforce.remove(playerName);
        }

        final long time = System.currentTimeMillis();
        if (player.isSprinting() || cc.assumeSprint) {
            // Hard to confine assumesprint further (some logics change with hdist or sprinting).
            if (player.getFoodLevel() > 5) {
                data.timeSprinting = time;
            }
            else if (time < data.timeSprinting) {
                data.timeSprinting = 0;
            }
            // else: keep sprinting time.
        }
        else{
            // Reset if not actually sprinting.
            data.timeSprinting = 0;
        }

        // Prepare locations for use.
        // TODO: Block flags might not be needed if neither sf nor passable get checked.
        final PlayerLocation pFrom, pTo;
        pFrom = moveInfo.from;
        pTo = moveInfo.to;

        // HOT FIX - for VehicleLeaveEvent missing.
        if (data.wasInVehicle) {
            onVehicleLeaveMiss(player, data, cc);
        }

        // Potion effect "Jump".
        final double jumpAmplifier = survivalFly.getJumpAmplifier(player);
        if (jumpAmplifier > data.jumpAmplifier) {
            data.jumpAmplifier = jumpAmplifier;
        }
        // TODO: same for speed (once medium is introduced).

        // Velocity tick (decrease + invalidation).
        // TODO: Rework to generic (?) queued velocity entries: activation + invalidation
        final int tick = TickTask.getTick();
        data.removeInvalidVelocity(tick - cc.velocityActivationTicks);
        data.velocityTick();

        // Check passable first to prevent set-back override.
        // TODO: Redesign to set set-backs later (queue + invalidate).
        boolean mightSkipNoFall = false; // If to skip nofall check (mainly on violation of other checks).
        if (newTo == null && cc.passableCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_PASSABLE) && !player.hasPermission(Permissions.MOVING_PASSABLE)) {
            // Passable is checked first to get the original set-back locations from the other checks, if needed. 
            newTo = passable.check(player, loc, pFrom, pTo, data, cc);
            if (newTo != null) {
                // Check if to skip the nofall check.
                mightSkipNoFall = true;
            }
        }

        // Check which fly check to use.
        final boolean checkCf;
        final boolean checkSf;
        if (shouldCheckSurvivalFly(player, data, cc)) {
            checkCf = false;
            checkSf = true;
            data.adjustWalkSpeed(player.getWalkSpeed(), tick, cc.speedGrace);

        }
        else if (cc.creativeFlyCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_CREATIVEFLY) && !player.hasPermission(Permissions.MOVING_CREATIVEFLY)) {
            checkCf = true;
            checkSf = false;
            data.adjustFlySpeed(player.getFlySpeed(), tick, cc.speedGrace);
            data.adjustWalkSpeed(player.getWalkSpeed(), tick, cc.speedGrace);
        }
        else{
            checkCf = checkSf = false;
        }

        // Flying checks.
        if (checkSf) {
            // SurvivalFly

            // Collect block flags.
            // TODO: Could further differentiate if really needed to (newTo / NoFall).
            final double maxYNoFall = Math.max(cc.noFallyOnGround, cc.yOnGround);
            pFrom.collectBlockFlags(maxYNoFall);
            if (pFrom.isSamePos(pTo)) {
                // TODO: Could consider pTo = pFrom, set pitch / yaw elsewhere.
                // Sets all properties, but only once.
                pTo.prepare(pFrom);
            }
            else{
                // Might collect block flags for small distances with the containing bounds for both. 
                pTo.collectBlockFlags(maxYNoFall);
            }

            // Actual check.
            if (newTo == null) {
                // Only check if passable has not already set back.
                newTo = survivalFly.check(player, pFrom, pTo, data, cc, time);
            }
            final boolean checkNf = cc.noFallCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_NOFALL) && !player.hasPermission(Permissions.MOVING_NOFALL);
            if (newTo == null) {
                // Hover.
                // TODO: Could reset for from-on-ground as well, for not too big moves.
                if (cc.sfHoverCheck && !data.toWasReset && !pTo.isOnGround()) {
                    // Start counting ticks.
                    hoverTicks.add(playerName);
                    data.sfHoverTicks = 0;
                }
                else{
                    data.sfHoverTicks = -1;
                }
                // NoFall.
                if (checkNf) {
                    noFall.check(player, loc, pFrom, pTo, data, cc);
                }
            }
            else{
                if (checkNf && cc.sfFallDamage) {
                    if (mightSkipNoFall) {
                        // Check if to really skip.
                        if (!pFrom.isOnGround() && !pFrom.isResetCond()) {
                            mightSkipNoFall = false;
                        }
                    }
                    if (!mightSkipNoFall) {
                        noFall.checkDamage(player, data, Math.min(Math.min(from.getY(), to.getY()), loc.getY()));
                    }
                }
            }
        }
        else if (checkCf) {
            // CreativeFly
            if (newTo == null) {
                newTo = creativeFly.check(player, pFrom, pTo, data, cc, time);
            }
            data.sfHoverTicks = -1;
            data.sfLowJump = false;
        }
        else{
            // No fly checking :(.
            data.clearFlyData();
        }

        // Morepackets.
        // TODO: Also update counters if newTo == null?
        if (newTo == null && cc.morePacketsCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_MOREPACKETS) && !player.hasPermission(Permissions.MOVING_MOREPACKETS)) {
            // If it hasn't been stopped by any other check and is handled by the more packets check, execute it.
            newTo = morePackets.check(player, pFrom, pTo, data, cc);
        } else {
            // Otherwise we need to clear their data.
            data.clearMorePacketsData();
        }

        // Reset jump amplifier if needed.
        if ((checkSf || checkCf) && jumpAmplifier != data.jumpAmplifier) {
            if (data.noFallAssumeGround || pFrom.isOnGround() || pTo.isOnGround()) {
                data.jumpAmplifier = jumpAmplifier;
            }
        }


        if (newTo == null) {
            // Set positions.
            // TODO: Consider setting in Monitor (concept missing for changing coordinates, could double-check).
            data.fromX = from.getX();
            data.fromY = from.getY();
            data.fromZ = from.getZ();
            data.toX = to.getX();
            data.toY = to.getY();
            data.toZ = to.getZ();
        }
        else {
            // Set-back handling.
            onSetBack(player, event, newTo, data, cc);
        }

        // Cleanup.
        moveInfo.cleanup();
        parkedInfo.add(moveInfo);
    }

    /**
     * 
     * @param player
     * @param event
     * @param newTo Must be a cloned or new Location instance, free for whatever other plugins do with it.
     * @param data
     * @param cc
     */
    private void onSetBack(final Player player, final PlayerMoveEvent event, final Location newTo, final MovingData data, final MovingConfig cc) {
        // Illegal Yaw/Pitch.
        if (LocUtil.needsYawCorrection(newTo.getYaw())) {
            newTo.setYaw(LocUtil.correctYaw(newTo.getYaw()));
        }
        if (LocUtil.needsPitchCorrection(newTo.getPitch())) {
            newTo.setPitch(LocUtil.correctPitch(newTo.getPitch()));
        }

        // Reset some data.
        data.prepareSetBack(newTo);
        data.resetPositions(newTo); // TODO: Might move into prepareSetBack, experimental here.

        // Set new to-location.
        event.setTo(newTo);

        // Debug.
        if (cc.debug) {
            System.out.println(player.getName() + " set back to: " + newTo.getWorld() + StringUtil.fdec3.format(newTo.getX()) + ", " + StringUtil.fdec3.format(newTo.getY()) + ", " + StringUtil.fdec3.format(newTo.getZ()));
        }
    }

    /**
     * Called from player-move checking, if the player is inside of a vehicle.
     * @param player
     * @param from
     * @param to
     * @param data
     */
    private Location onPlayerMoveVehicle(final Player player, final Location from, final Location to, final MovingData data) {
        // Workaround for pigs and other (1.5.x and before)!
        // Note that with 1.6 not even PlayerMove fires for horses and pigs.
        // (isInsideVehicle is the faster check without object creation, do re-check though, if it changes to only check for Vehicle instances.)
        final Entity vehicle = CheckUtils.getLastNonPlayerVehicle(player);
        data.wasInVehicle = true;
        data.sfHoverTicks = -1;
        data.removeAllVelocity();
        data.sfLowJump = false;
        // TODO: What with processingEvents.remove(player.getName());
        if (vehicle != null) {
            final Location vLoc = vehicle.getLocation(); // TODO: Use a location as argument.
            // (Auto detection of missing events, might fire one time too many per plugin run.)
            if (!normalVehicles.contains(vehicle.getType())) {
                onVehicleMove(vehicle, vLoc, vLoc, true);
                return null;
            } else {
                data.vehicleConsistency = MoveConsistency.getConsistency(from, to, vLoc);
                // TODO: Consider TeleportUtil.forceMount or similar.
                if (data.vehicleConsistency == MoveConsistency.INCONSISTENT) {
                    if (MovingConfig.getConfig(player).vehicleEnforceLocation) {
                        return vLoc;
                    } else {
                        return null;
                    }
                } else {
                    data.resetPositions(vLoc);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Called from player-move checking, if vehicle-leave has not been called after entering, but the player is not inside of a vehicle anymore.
     * @param player
     * @param data
     * @param cc
     */
    private void onVehicleLeaveMiss(final Player player, final MovingData data, final MovingConfig cc) {
        if (cc.debug) {
            LogUtil.logWarning("[NoCheatPlus] VehicleExitEvent missing for: " + player.getName());
        }
        onPlayerVehicleLeave(player, null);
        //		if (BlockProperties.isRails(pFrom.getTypeId())) {
        // Always clear no fall data, let Minecraft do fall damage.
        data.noFallSkipAirCheck = true; // Might allow one time cheat.
        data.sfLowJump = false;
        data.clearNoFallData();
        // TODO: What with processingEvents.remove(player.getName());
        //		}
    }

    /**
     * Monitor level PlayerMoveEvent.
     * @param event
     */
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerMoveMonitor(final PlayerMoveEvent event) {
        // TODO: revise: cancelled events.
        final long now = System.currentTimeMillis();
        final Player player = event.getPlayer();

        if (processingEvents.remove(player.getName()) == null) {
            // This means moving data has been reset by a teleport.
            // TODO: vehicles, cancelled, ...
            return;
        }

        if (player.isDead() || player.isSleeping()) return;

        // Feed combined check.
        final CombinedData data = CombinedData.getData(player);
        data.lastMoveTime = now; // TODO: Evaluate moving this to MovingData !?

        final Location from = event.getFrom();
        final String fromWorldName = from.getWorld().getName();

        // Feed yawrate and reset moving data positions if necessary.
        final MovingData mData = MovingData.getData(player);
        final long time = TickTask.getTick();
        if (!event.isCancelled()) {
            final Location to = event.getTo();
            final String toWorldName = to.getWorld().getName();
            Combined.feedYawRate(player, to.getYaw(), now, toWorldName, data);
            // TODO: maybe even not count vehicles at all ?
            if (player.isInsideVehicle()) {
                // TODO: refine (!).
                final Location ref = player.getVehicle().getLocation(useLoc);
                mData.resetPositions(ref);
                useLoc.setWorld(null);
                mData.resetTrace(player, ref, time);
            }
            else if (!fromWorldName.equals(toWorldName)) {
                mData.resetPositions(to);
                mData.resetTrace(player, to, time);
            }
            else{
                // Slightly redundant at present.
                mData.setTo(to);
                mData.resetTrace(player, to, time);
            }
        }
        else {
            // TODO: teleported + other resetting ?
            Combined.feedYawRate(player, from.getYaw(), now, fromWorldName, data);
            mData.resetPositions(from);
            mData.resetTrace(player, from, time); // TODO: Should probably leave this to the teleport event!
        }
    }

    /**
     * When a player uses a portal, all information related to the moving checks becomes invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPortal(final PlayerPortalEvent event) {
        final MovingData data = MovingData.getData(event.getPlayer());
        data.clearFlyData();
        data.clearMorePacketsData();
        // TODO: This event might be redundant (!).
    }

    /**
     * Clear fly data on death.
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final MovingData data = MovingData.getData(player);
        data.clearFlyData();
        data.clearMorePacketsData();
        data.setSetBack(player.getLocation(useLoc)); // TODO: Monitor this change (!).
        useLoc.setWorld(null);
    }

    /**
     * If a player gets teleported, it may have two reasons. Either it was NoCheat or another plugin. If it was
     * NoCheatPlus, the target location should match the "data.teleportedTo" value.
     * 
     * On teleports, reset some movement related data that gets invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final MovingData data = MovingData.getData(player);
        final Location teleported = data.getTeleported();

        // If it was a teleport initialized by NoCheatPlus, do it anyway even if another plugin said "no".
        final Location to = event.getTo();
        final Location ref;
        if (teleported != null && teleported.equals(to)) {
            // Teleport by NCP.
            // Prevent cheaters getting rid of flying data (morepackets, other).
            // TODO: even more strict enforcing ?
            if (event.isCancelled()) {
                event.setCancelled(false);
                event.setTo(teleported); // ?
                event.setFrom(teleported);
                ref = teleported;
            }
            else{
                // Not cancelled but NCP teleport.
                ref = to;
            }
            // TODO: This could be done on MONITOR.
            data.onSetBack(teleported);
        } else {
            final MovingConfig cc = MovingConfig.getConfig(player);
            // Only if it wasn't NoCheatPlus, drop data from more packets check.
            if (to != null && !event.isCancelled()) {
                // Normal teleport.

                // Detect small distance teleports.
                boolean smallRange = false;
                boolean cancel = false;
                //				boolean pass = false;

                final double margin = 0.67;
                final Location from = event.getFrom();


                final TeleportCause cause = event.getCause();
                if (cause == TeleportCause.UNKNOWN) {
                    // Check special small range teleports (moved too quickly).
                    if (from != null && from.getWorld().equals(to.getWorld())) {
                        if (TrigUtil.distance(from, to) < margin) {
                            smallRange = true;
                        }
                        else if (data.toX != Double.MAX_VALUE && data.hasSetBack()) {
                            final Location setBack = data.getSetBack(to);
                            if (TrigUtil.distance(to.getX(), to.getY(), to.getZ(), setBack.getX(), setBack.getY(), setBack.getZ()) < margin) {
                                smallRange = true;
                            }
                        }
                    }
                }
                else if (cause == TeleportCause.ENDER_PEARL) {
                    if (CombinedConfig.getConfig(player). enderPearlCheck && !BlockProperties.isPassable(to)) { // || !BlockProperties.isOnGroundOrResetCond(player, to, 1.0)) {
                        // Not check on-ground: Check the second throw.
                        cancel = true;
                    }
                    else{
                        //						pass = true;
                    }
                }

                //				if (pass) {
                //					ref = to;
                //				}
                //				else 
                if (cancel) {
                    // Cancel!
                    if (data.hasSetBack() && !data.hasSetBackWorldChanged(to)) {
                        ref = data.getSetBack(to);
                        event.setTo(ref);
                    }
                    else{
                        ref = from; // Player.getLocation ?
                        event.setCancelled(true);
                    }
                }
                else if (smallRange) {
                    // Very small range teleport, keep set back etc.
                    ref = to;
                    //					if (data.hasSetBack() && !data.hasSetBackWorldChanged(to)) {
                    //						final Location setBack = data.getSetBack(from);
                    //						Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    //							@Override
                    //							public void run() {
                    //								if (!data.hasSetBackWorldChanged(setBack)) { // && data.isSetBack(setBack)) {
                    //									player.sendMessage("SETBACK FROM MC DERP.");
                    //									player.teleport(setBack, TeleportCause.PLUGIN);
                    //								}
                    //							}
                    //						});
                    //					}
                }
                else{
                    // "real" teleport
                    ref = to;
                    double fallDistance = data.noFallFallDistance;
                    final MediumLiftOff oldMLO = data.mediumLiftOff; // Remember for workarounds.
                    data.clearMorePacketsData();
                    data.clearFlyData();
                    data.resetPositions(to);
                    if (TrigUtil.maxDistance(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ())  <= 12.0) {
                        // TODO: Might happen with bigger distances (mainly ender pearl thrown at others).
                        // Keep old MediumLiftOff.
                        data.mediumLiftOff = oldMLO;
                    }
                    data.setSetBack(to);
                    // TODO: How to account for plugins that reset the fall distance here?
                    if (fallDistance > 1.0 && fallDistance - player.getFallDistance() > 0.0) {
                        // Reset fall distance if set so in the config.
                        if (!cc.noFallTpReset) {
                            // (Set fall distance if set to not reset.)
                            player.setFallDistance((float) fallDistance);
                        }
                    }
                    if (event.getCause() == TeleportCause.ENDER_PEARL) {
                        // Prevent NoFall violations for ender-pearls.
                        data.noFallSkipAirCheck = true;
                    }
                    data.sfHoverTicks = -1; // Important against concurrent modification exception.
                }

                if (cc.debug && BuildParameters.debugLevel > 0) {
                    System.out.println(player.getName() + " TP" + (smallRange ? " (small-range)" : "") + (cancel ? " (cancelled)" : "") +  ": " + to);
                }
            }
            else{
                // Cancelled, not a set back, ignore it, basically.
                // Better reset teleported (compatibility). Might have drawbacks.
                data.resetTeleported();
                if (cc.debug && BuildParameters.debugLevel > 0) {
                    System.out.println(player.getName() + " TP (cancelled): " + to);
                }
                return;
            }

        }
        // Reset stuff.
        Combined.resetYawRate(player, ref.getYaw(), System.currentTimeMillis(), true);
        data.resetTeleported();
        // Prevent further moving processing for nested events.
        processingEvents.remove(player.getName());
    }

    /**
     * Player got a velocity packet. The server can't keep track of actual velocity values (by design), so we have to
     * try and do that ourselves. Very rough estimates.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerVelocity(final PlayerVelocityEvent event) {
        final Player player = event.getPlayer();
        final MovingData data = MovingData.getData(player);
        // Ignore velocity if inside of vehicles.
        if (player.isInsideVehicle()) {
            data.removeAllVelocity();
            return;
        }
        final MovingConfig cc = MovingConfig.getConfig(player);

        final int tick = TickTask.getTick();
        data.removeInvalidVelocity(tick  - cc.velocityActivationTicks);


        final Vector velocity = event.getVelocity();

        if (cc.debug) {
            System.out.println(event.getPlayer().getName() + " new velocity: " + velocity);
        }

        double newVal = velocity.getY();
        boolean used = false;
        if (newVal >= 0D) { // TODO: Just >, not >=.
            used = true;
            if (data.verticalFreedom <= 0.001 && data.verticalVelocityCounter >= 0) {
                data.verticalVelocity = 0;
            }
            data.verticalVelocity += newVal;
            data.verticalFreedom += data.verticalVelocity;
            data.verticalVelocityCounter = Math.min(100, Math.max(data.verticalVelocityCounter, cc.velocityGraceTicks ) + 1 + (int) Math.round(newVal * 10.0)); // 50;
            data.verticalVelocityUsed = 0;
        }

        newVal = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        if (newVal > 0D) {
            used = true;
            final Velocity vel = new Velocity(tick, newVal, cc.velocityActivationCounter, Math.max(20,  1 + (int) Math.round(newVal * 10.0)));
            data.addHorizontalVelocity(vel);
            //            data.horizontalFreedom += newVal;
            //            data.horizontalVelocityCounter = Math.min(100, Math.max(data.horizontalVelocityCounter, cc.velocityGraceTicks ) + 1 + (int) Math.round(newVal * 10.0)); // 30;
            //            data.horizontalVelocityUsed = 0;
        }

        // Set dirty flag here.
        if (used) {
            data.sfDirty = true;
            data.sfNoLowJump = true;
        }

        // TODO: clear accounting here ?
    }

    /**
     * When a vehicle moves, its player will be checked for various suspicious behaviors.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVehicleMove(final VehicleMoveEvent event) {
        final Vehicle vehicle = event.getVehicle();
        final EntityType entityType = vehicle.getType();
        if (!normalVehicles.contains(entityType)) {
            // A little extra sweep to check for debug flags.
            normalVehicles.add(entityType);
            if (MovingConfig.getConfig(vehicle.getWorld().getName()).debug) {
                System.out.println("[NoCheatPlus] VehicleMoveEvent fired for: " + entityType);
            }
        }
        // TODO: Might account for the case of a player letting the vehicle move but not themself (do mind latency).
        // Mind that players could be riding horses inside of minecarts etc.
        if (vehicle.getVehicle() != null) {
            // Do ignore events for vehicles inside of other vehicles.
            return;
        }
        onVehicleMove(vehicle, event.getFrom(), event.getTo(), false);
    }


    public void onVehicleMove(final Entity vehicle, final Location from, final Location to, final boolean fake) {	
        // (No re-check for vehicles that have vehicles, pre condition is that this has already been checked.)
        final Player player = CheckUtils.getFirstPlayerPassenger(vehicle);
        if (player == null) {
            return;
        }
        if (vehicle.isDead() || !vehicle.isValid()) {
            onPlayerVehicleLeave(player, vehicle);
            return;
        }
        if (!from.getWorld().equals(to.getWorld())) return;

        final MovingData data = MovingData.getData(player);
        data.vehicleConsistency = MoveConsistency.getConsistency(from, to, player.getLocation(useLoc));
        switch (data.vehicleConsistency) {
        case FROM:
        case TO:
            data.resetPositions(player.getLocation(useLoc)); // TODO: Drop MC 1.4!
            break;
        case INCONSISTENT:
            // TODO: Any exploits exist? -> TeleportUtil.forceMount(player, vehicle)
            // TODO: Test with latency.
            break;
        }

        Location newTo = null;
        data.sfNoLowJump = true;

        final MovingConfig cc = MovingConfig.getConfig(player);
        if (cc.noFallVehicleReset) {
            // Reset noFall data.
            data.noFallSkipAirCheck = true; // Might allow one time cheat.
            data.sfLowJump = false;
            data.clearNoFallData();
        }

        if (cc.debug) {
            // Log move.
            DebugUtil.outputDebugVehicleMove(player, vehicle, from, to, fake);
        }

        if (morePacketsVehicle.isEnabled(player)) {
            // If the player is handled by the more packets vehicle check, execute it.
            newTo = morePacketsVehicle.check(player, from, to, data, cc);
        }
        else{
            // Otherwise we need to clear their data.
            data.clearMorePacketsData();
        }

        // Schedule a set-back?
        if (newTo != null && data.morePacketsVehicleTaskId == -1) {
            // Schedule a delayed task to teleport back the vehicle with the player.
            // (Only schedule if not already scheduled.)
            // TODO: Might log debug if skipping.
            // TODO: Problem: scheduling allows a lot of things to happen until the task is run. Thus control about some things might be necessary.
            // TODO: Reset on world changes or not?
            data.morePacketsVehicleTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new VehicleSetBack(vehicle, player, newTo, cc.debug));
        }
        useLoc.setWorld(null);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamage(final EntityDamageEvent event) {
        if (event.getCause() != DamageCause.FALL) {
            return;
        }
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) entity;
        final MovingData data = MovingData.getData(player);
        if (player.isInsideVehicle()) {
            // Ignore vehicles (noFallFallDistance will be inaccurate anyway).
            data.clearNoFallData();
            return;
        }
        final MovingConfig cc = MovingConfig.getConfig(player);
        if (event.isCancelled() || !shouldCheckSurvivalFly(player, data, cc) || !noFall.isEnabled(player)) {
            data.clearNoFallData();
            return;
        }
        final Location loc = player.getLocation(useLoc);
        boolean allowReset = true;
        if (!data.noFallSkipAirCheck) {
            final MoveInfo moveInfo;
            if (parkedInfo.isEmpty()) {
                moveInfo = new MoveInfo(mcAccess);
            }
            else {
                moveInfo = parkedInfo.remove(parkedInfo.size() - 1);
            }
            moveInfo.set(player, loc, null, cc.noFallyOnGround);
            // NOTE: No isIllegal check here.
            final PlayerLocation pLoc = moveInfo.from;
            moveInfo.from.collectBlockFlags(cc.noFallyOnGround);
            // Be sure not to lose that block.
            data.noFallFallDistance += 1.0;
            // TODO: Accound for liquid too?
            if (!pLoc.isOnGround(1.0, 0.3, 0.1) && !pLoc.isResetCond() && !pLoc.isAboveLadder() && !pLoc.isAboveStairs()) {
                // Likely a new style no-fall bypass (damage in mid-air).
                data.noFallVL += 1.0;
                if (noFall.executeActions(player, data.noFallVL, 1.0, cc.noFallActions, true) && data.hasSetBack()) {
                    // Cancel the event and restore fall distance.
                    // NoFall data will not be reset 
                    allowReset = false;
                }
            }
            else{
                // Legitimate damage: clear accounting data.
                data.vDistAcc.clear();
                // TODO: Also reset other properties.
                // TODO: Also reset in other cases (moved too quickly)?
            }
            moveInfo.cleanup();
            parkedInfo.add(moveInfo);
        }
        final float fallDistance = player.getFallDistance();
        final double damage = BridgeHealth.getDamage(event);
        final float yDiff = (float) (data.noFallMaxY - loc.getY());
        if (cc.debug) {
            System.out.println(player.getName() + " damage(FALL): " + damage + " / dist=" + player.getFallDistance() + " nf=" + data.noFallFallDistance + " yDiff=" + yDiff);
        }
        // Fall-back check.
        final double maxD = NoFall.getDamage(Math.max(yDiff, Math.max(data.noFallFallDistance, fallDistance))) + (allowReset ? 0.0 : 3.0);
        if (maxD > damage) {
            // TODO: respect dealDamage ?
            BridgeHealth.setDamage(event, maxD);
            if (cc.debug) {
                System.out.println(player.getName() + " Adjust fall damage to: " + maxD);
            }
        }
        if (allowReset) {
            // Normal fall damage, reset data.
            data.clearNoFallData();
        }
        else{
            // Minecraft/NCP bug or cheating.
            // (Do not cancel the event, otherwise: "moved too quickly exploit".)
            if (cc.noFallViolationReset) {
                data.clearNoFallData();
            }
            // Add player to hover checks.
            if (cc.sfHoverCheck && data.sfHoverTicks < 0) {
                data.sfHoverTicks = 0;
                hoverTicks.add(player.getName());
            }
        }
        // Entity fall-distance should be reset elsewhere.
        // Cleanup.
        useLoc.setWorld(null);
    }

    /**
     * When a player respawns, all information related to the moving checks
     * becomes invalid.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final MovingData data = MovingData.getData(player);
        data.clearFlyData();
        data.resetSetBack(); // To force dataOnJoin to set it to loc.
        // Handle respawn like join.
        dataOnJoin(player, event.getRespawnLocation(), data, MovingConfig.getConfig(player));
    }

    @Override
    public void playerJoins(final Player player) {
        dataOnJoin(player, player.getLocation(useLoc), MovingData.getData(player), MovingConfig.getConfig(player));
        // Cleanup.
        useLoc.setWorld(null);
    }

    /**
     * Alter data for players joining (join, respawn).<br>
     * Do before, if necessary:<br>
     * <li>data.clearFlyData()</li>
     * <li>data.setSetBack(...)</li>
     * @param player
     * @param loc Can be useLoc (!).
     * @param data
     * @param cc
     */
    private void dataOnJoin(Player player, Location loc, MovingData data, MovingConfig cc) {
        
        final int tick = TickTask.getTick();
        // Check loaded chunks.
        if (cc.loadChunksOnJoin) {
            final int loaded = BlockCache.ensureChunksLoaded(loc.getWorld(), loc.getX(), loc.getZ(), 3.0);
            if (loaded > 0 && cc.debug && BuildParameters.debugLevel > 0) {
                // DEBUG
                LogUtil.logInfo("[NoCheatPlus] Player join: Loaded " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + loc.getWorld().getName() +  " for player: " + player.getName());
            }
        }
        
        // Correct set-back on join.
        if (!data.hasSetBack() || data.hasSetBackWorldChanged(loc)) {
            data.clearFlyData();
            data.setSetBack(loc);
            data.resetPositions(loc);
        } else {
            // TODO: Check consistency/distance.
            //final Location setBack = data.getSetBack(loc);
            //final double d = loc.distanceSquared(setBack);
            // TODO: If to reset positions: relate to previous ones and set-back.
            data.resetPositions(loc); // TODO: See above.
        }
        
        // Always reset position to this one.
        // TODO: more fine grained reset?
        data.clearMorePacketsData();
        data.removeAllVelocity();
        data.resetTrace(loc, tick, cc.traceSize, cc.traceMergeDist); // Might reset to loc instead of set-back ?

        // More resetting.
        data.vDistAcc.clear();
        data.toWasReset = BlockProperties.isOnGroundOrResetCond(player, loc, cc.yOnGround);
        data.fromWasReset = data.toWasReset;

        // Enforcing the location.
        if (cc.enforceLocation) {
            playersEnforce.add(player.getName());
        }

        // Hover.
        initHover(player, data, cc, data.toWasReset); // isOnGroundOrResetCond

        //		// Bad pitch/yaw, just in case.
        //		if (LocUtil.needsDirectionCorrection(useLoc.getYaw(), useLoc.getPitch())) {
        //			DataManager.getPlayerData(player).task.correctDirection();
        //		}
    }

    /**
     * Initialize the hover check for a player (login, respawn). 
     * @param player
     * @param data
     * @param cc
     * @param isOnGroundOrResetCond 
     */
    private void initHover(final Player player, final MovingData data, final MovingConfig cc, final boolean isOnGroundOrResetCond) {
        // Reset hover ticks until a better method is used.
        if (!isOnGroundOrResetCond && cc.sfHoverCheck) {
            // Start as if hovering already.
            // Could check shouldCheckSurvivalFly(player, data, cc), but this should be more sharp (gets checked on violation).
            data.sfHoverTicks = 0;
            data.sfHoverLoginTicks = cc.sfHoverLoginTicks;
            hoverTicks.add(player.getName());
        }
        else{
            data.sfHoverLoginTicks = 0;
            data.sfHoverTicks = -1;
        }
    }

    @Override
    public void playerLeaves(final Player player) {
        final MovingConfig cc = MovingConfig.getConfig(player);
        final MovingData data = MovingData.getData(player);
        final Location loc = player.getLocation(useLoc);
        // Debug logout.
        if (cc.debug) {
            LogUtil.logInfo("[NoCheatPlus] Player " + player.getName() + " leaves at location: " + loc.toString());
        }
        if (!player.isSleeping() && !player.isDead()) {
            // Check for missed moves.
            // TODO: Consider to catch all, at least (debug-) logging-wise.
            if (!BlockProperties.isPassable(loc)) {
                if (data.toX != Double.MAX_VALUE) {
                    final Location refLoc = new Location(loc.getWorld(), data.toX, data.toY, data.toZ);
                    final double d = refLoc.distanceSquared(loc);
                    if (d > 0.0) {
                        // TODO: Consider to always set back here. Might skip on big distances.
                        if (TrigUtil.manhattan(loc, refLoc) > 0 || BlockProperties.isPassable(refLoc)) {
                            if (passable.isEnabled(player)) {
                                LogUtil.logWarning("[NoCheatPlus] Potential exploit: Player " + player.getName() + " leaves, having moved into a block (not tracked by moving checks): " + player.getWorld().getName() + " / " + DebugUtil.formatMove(refLoc, loc));
                                // TODO: Actually trigger a passable violation (+tag).
                                if (d > 1.25) {
                                    LogUtil.logWarning("[NoCheatPlus] SKIP set-back for " + player.getName() + ", because distance is too high (risk of false positives): " + d);
                                } else {
                                    LogUtil.logInfo("[NoCheatPlus] Set back player " + player.getName() + ": " + DebugUtil.formatLocation(refLoc));
                                    data.prepareSetBack(refLoc);
                                    if (!player.teleport(refLoc)) {
                                        LogUtil.logWarning("[NoCheatPlus] FAILED to set back player " + player.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        useLoc.setWorld(null);
        // Adjust data.
        survivalFly.setReallySneaking(player, false);
        noFall.onLeave(player);
        // TODO: Add a method for ordinary presence-change resetting (use in join + leave).
        data.onPlayerLeave();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldunload(final WorldUnloadEvent event) {
        // TODO: Consider removing the world-related data anyway (even if the event is cancelled).
        MovingData.onWorldUnload(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event) {
        final Entity entity = event.getExited();
        if (!(entity instanceof Player)) return;
        onPlayerVehicleLeave((Player) entity, event.getVehicle());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroyLowest(final VehicleDestroyEvent event) {
        // Prevent destroying ones own vehicle.
        final Entity attacker = event.getAttacker();
        if (attacker instanceof Player && attacker.equals(event.getVehicle().getPassenger())) {
            final Player player = (Player) attacker;
            if (survivalFly.isEnabled(player) || creativeFly.isEnabled(player)) {
                if (MovingConfig.getConfig(player).vehiclePreventDestroyOwn) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_RED + "Destroying your own vehicle is disabled.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        final Entity entity = event.getVehicle().getPassenger();
        if (!(entity instanceof Player)) return;
        onPlayerVehicleLeave((Player) entity, event.getVehicle());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVehicleEnter(final VehicleEnterEvent event) {
        final Entity entity = event.getEntered();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) entity;
        final MovingData data = MovingData.getData(player);
        data.removeAllVelocity();
        // Event should have a vehicle, in case check this last.
        data.vehicleConsistency = MoveConsistency.getConsistency(event.getVehicle().getLocation(), null, player.getLocation(useLoc));
        useLoc.setWorld(null); // TODO: A pool ?
        // TODO: more resetting, visible check ?
    }

    /**
     * Call on leaving or just having left a vehicle.
     * @param player
     * @param vehicle May be null in case of "not possible to determine".
     */
    private void onPlayerVehicleLeave(final Player player, final Entity vehicle) {
        final MovingData data = MovingData.getData(player);
        data.wasInVehicle = false;
        //    	if (data.morePacketsVehicleTaskId != -1) {
        //    		// Await set-back.
        //    		// TODO: might still set ordinary set-backs ?
        //    		return;
        //    	}

        final MovingConfig cc = MovingConfig.getConfig(player);
        // TODO: Loc can be inconsistent, determine which to use ! 
        final Location pLoc = player.getLocation(useLoc);
        Location loc = pLoc; // The location to use as set-back.
        //  TODO: Which vehicle to use ?
        // final Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            final Location vLoc = vehicle.getLocation();
            // Workaround for some entities/animals that don't fire VehicleMoveEventS.
            if (!normalVehicles.contains(vehicle.getType()) || cc.noFallVehicleReset) {
                data.noFallSkipAirCheck = true; // Might allow one time cheat.
                data.clearNoFallData();
            }
            // Check consistency with vehicle location.
            if (MoveConsistency.getConsistency(vLoc, null, pLoc) == MoveConsistency.INCONSISTENT) {
                // TODO: Consider teleporting the player (...)
                // TODO: What with the case of vehicle moved to another world !?
                loc = vLoc; // 
                if (data.vehicleConsistency != MoveConsistency.INCONSISTENT) {
                    final Location oldLoc = new Location(pLoc.getWorld(), data.toX, data.toY, data.toZ);
                    if (data.toX != Double.MAX_VALUE && MoveConsistency.getConsistency(oldLoc, null, pLoc) != MoveConsistency.INCONSISTENT) {
                        loc = oldLoc;
                    }
                }

            }
            if (cc.debug) {
                System.out.println(player.getName() + " vehicle leave: " + vehicle.getType() + "@" + pLoc.distance(vLoc));
            }
        }

        // Adjust loc if in liquid (meant for boats !?).
        if (BlockProperties.isLiquid(loc.getBlock().getType())) {
            loc.setY(Location.locToBlock(loc.getY()) + 1.25);
        }

        if (cc.debug) {
            System.out.println(player.getName() + " vehicle leave: " + pLoc.toString() + (pLoc.equals(loc) ? "" : " / player at: " + pLoc.toString()));
        }
        data.resetPositions(loc);
        data.setSetBack(loc);
        // Give some freedom to allow the "exiting move".
        data.removeAllVelocity();
        data.addHorizontalVelocity(new Velocity(0.9, 1, 1));
        data.verticalVelocityCounter = 1;
        data.verticalFreedom = 1.2;
        data.verticalVelocity = 0.15;
        data.verticalVelocityUsed = 0;
        useLoc.setWorld(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(final PlayerToggleSneakEvent event) {
        survivalFly.setReallySneaking(event.getPlayer(), event.isSneaking());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSprint(final PlayerToggleSprintEvent event) {
        if (!event.isSprinting()) {
            MovingData.getData(event.getPlayer()).timeSprinting = 0;
        }
    }

    @Override
    public void onTick(final int tick, final long timeLast) {
        final List<String> rem = new ArrayList<String>(hoverTicks.size()); // Pessimistic.
        // TODO: Change to per world checking (as long as configs are per world).

        // Enforcing location check.
        for (final String playerName : playersEnforce) {
            final Player player = DataManager.getPlayerExact(playerName);
            if (player == null || !player.isOnline()) {
                rem.add(playerName);
                continue;
            } else if (player.isDead() || player.isSleeping() || player.isInsideVehicle()) {
                // Don't remove but also don't check [subject to change].
                continue;
            }
            final MovingData data = MovingData.getData(player);
            final Location newTo = enforceLocation(player, player.getLocation(useLoc), data);
            if (newTo != null) {
                data.prepareSetBack(newTo);
                player.teleport(newTo, TeleportCause.PLUGIN);
            }
        }
        if (!rem.isEmpty()) {
            playersEnforce.removeAll(rem);
        }
        // Hover check (survivalfly).
        rem.clear();
        if (tick % hoverTicksStep != 0) {
            // Only check every so and so ticks.
            return;
        }
        final MoveInfo info;
        if (parkedInfo.isEmpty()) {
            info = new MoveInfo(mcAccess);
        }
        else {
            info = parkedInfo.remove(parkedInfo.size() - 1);
        }
        for (final String playerName : hoverTicks) {
            // TODO: put players into the set (+- one tick would not matter ?)
            // TODO: might add an online flag to data !
            final Player player = DataManager.getPlayerExact(playerName);
            if (player == null || !player.isOnline()) {
                rem.add(playerName);
                continue;
            }
            final MovingData data = MovingData.getData(player);
            if (player.isDead() || player.isSleeping() || player.isInsideVehicle()) {
                data.sfHoverTicks = -1;
                // (Removed below.)
            }
            if (data.sfHoverTicks < 0) {
                data.sfHoverLoginTicks = 0;
                rem.add(playerName);
                continue;
            }
            else if (data.sfHoverLoginTicks > 0) {
                // Additional "grace period".
                data.sfHoverLoginTicks --;
                continue;
            }
            final MovingConfig cc = MovingConfig.getConfig(player);
            // Check if enabled at all.
            if (!cc.sfHoverCheck) {
                rem.add(playerName);
                data.sfHoverTicks = -1;
                continue;
            }
            // Increase ticks here.
            data.sfHoverTicks += hoverTicksStep;
            if (data.sfHoverTicks < cc.sfHoverTicks) {
                // Don't do the heavier checking here, let moving checks reset these.
                continue;
            }
            if (checkHover(player, data, cc, info)) {
                rem.add(playerName);
            }
        }
        hoverTicks.removeAll(rem);
        rem.clear();
        info.cleanup();
        parkedInfo.add(info);
        useLoc.setWorld(null);
    }

    private Location enforceLocation(final Player player, final Location loc, final MovingData data) {
        if (data.toX != Double.MAX_VALUE && TrigUtil.distanceSquared(data.toX, data.toY, data.toZ, loc.getX(), loc.getY(), loc.getZ()) > 1.0 / 256.0) {
            // Teleport back. 
            // TODO: Add history / alert?
            //player.sendMessage(ChatColor.RED + "NCP: enforce location !"); // TODO: DEBUG - REMOVE.
            if (data.hasSetBack()) {
                // Might have to re-check all context with playerJoins and keeping old set-backs...
                // Could use a flexible set-back policy (switch to in-air on login). 
                return data.getSetBack(loc);
            } else {
                return new Location(player.getWorld(), data.toX, data.toY, data.toZ, loc.getYaw(), loc.getPitch());
            }
        } else {
            return null;
        }
    }

    /**
     * The heavier checking including on.ground etc., check if enabled/valid to check before this. 
     * @param player
     * @param data
     * @param cc
     * @param info
     * @return
     */
    private boolean checkHover(final Player player, final MovingData data, final MovingConfig cc, final MoveInfo info) {
        // Check if player is on ground.
        final Location loc = player.getLocation(useLoc); // useLoc.setWorld(null) is done in onTick.
        info.set(player, loc, null, cc.yOnGround);
        // (Could use useLoc of MoveInfo here. Note orderm though.)
        final boolean res;
        // TODO: Collect flags, more margin ?
        final int loaded = info.from.ensureChunksLoaded();
        if (loaded > 0 && cc.debug && BuildParameters.debugLevel > 0) {
            // DEBUG
            LogUtil.logInfo("[NoCheatPlus] Hover check: Needed to load " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + loc.getWorld().getName() +  " around " + loc.getBlockX() + "," + loc.getBlockZ() + " in order to check player: " + player.getName());
        }
        if (info.from.isOnGround() || info.from.isResetCond() || info.from.isAboveLadder() || info.from.isAboveStairs()) {
            res = true;
            data.sfHoverTicks = 0;
        }
        else{
            if (data.sfHoverTicks > cc.sfHoverTicks) {
                // Re-Check if survivalfly can apply at all.
                if (shouldCheckSurvivalFly(player, data, cc)) {
                    handleHoverViolation(player, loc, cc, data);
                    // Assume the player might still be hovering.
                    res = false;
                    data.sfHoverTicks = 0;
                }
                else{
                    // Reset hover ticks and check next period.
                    res = false;
                    data.sfHoverTicks = 0;
                }
            }
            else res = false;
        }
        info.cleanup();
        return res;
    }

    private void handleHoverViolation(final Player player, final Location loc, final MovingConfig cc, final MovingData data) {
        // Check nofall damage (!).
        if (cc.sfHoverFallDamage && noFall.isEnabled(player)) {
            // Consider adding 3/3.5 to fall distance if fall distance > 0?
            noFall.checkDamage(player, data, loc.getY());
        }
        // Delegate violation handling.
        survivalFly.handleHoverViolation(player, loc, cc, data);
    }

    @Override
    public CheckType getCheckType() {
        // TODO: this is for the hover check only...
        return CheckType.MOVING_SURVIVALFLY;
    }

    @Override
    public IData removeData(String playerName) {
        hoverTicks.remove(playerName);
        playersEnforce.remove(playerName);
        return null;
    }

    @Override
    public void removeAllData() {
        hoverTicks.clear();
        playersEnforce.clear();
        parkedInfo.clear();
    }

    @Override
    public void onReload() {
        for (final MoveInfo info : parkedInfo) {
            // Just in case.
            info.cleanup();
        }
        parkedInfo.clear();
        hoverTicksStep = Math.max(1, ConfigManager.getConfigFile().getInt(ConfPaths.MOVING_SURVIVALFLY_HOVER_STEP));
        MovingData.onReload();
    }

}
