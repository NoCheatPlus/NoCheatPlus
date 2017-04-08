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
package fr.neatmonster.nocheatplus.checks.moving.util;

import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.MoveData;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.checks.moving.player.PlayerSetBackMethod;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.model.CountableLocation;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.debug.IDebugPlayer;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.location.RichBoundsLocation;
import fr.neatmonster.nocheatplus.utilities.location.TrigUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.MapUtil;

/**
 * Static utility methods.
 * @author dev1mc
 *
 */
public class MovingUtil {

    /**
     * Always set world to null after use, careful with nested methods. Main thread only.
     */
    private static final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Check if the player is to be checked by the survivalfly check.
     * 
     * @param player
     * @param fromLoc
     *            The location the player is moving from or just where the
     *            player is.
     * @param data
     * @param cc
     * @return
     */
    public static final boolean shouldCheckSurvivalFly(final Player player, final PlayerLocation fromLocation, final MovingData data, final MovingConfig cc) {
        final GameMode gameMode = player.getGameMode();
        return  cc.survivalFlyCheck && gameMode != BridgeMisc.GAME_MODE_SPECTATOR
                && (cc.ignoreCreative || gameMode != GameMode.CREATIVE) && !player.isFlying() 
                && (cc.ignoreAllowFlight || !player.getAllowFlight())
                && !NCPExemptionManager.isExempted(player, CheckType.MOVING_SURVIVALFLY)
                && (Double.isInfinite(Bridge1_9.getLevitationAmplifier(player)) || fromLocation.isInLiquid())
                && (!Bridge1_9.isGlidingWithElytra(player) || fromLocation.isOnGroundOrResetCond())
                && !player.hasPermission(Permissions.MOVING_SURVIVALFLY);
    }

    /**
     * Handle an illegal move by a player, attempt to restore a valid location.
     * <br>
     * NOTE: event.setTo is used to not leave a gap.
     * 
     * @param event
     * @param player
     * @param data
     * @param cc
     */
    public static void handleIllegalMove(final PlayerMoveEvent event, final Player player, 
            final MovingData data, final MovingConfig cc)
    {
        // This might get extended to a check-like thing.
        boolean restored = false;
        final PlayerLocation pLoc = new PlayerLocation(NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(MCAccess.class), null);
        // (Mind that we don't set the block cache here).
        final Location loc = player.getLocation();
        if (!restored && data.hasSetBack()) {
            final Location setBack = data.getSetBack(loc); 
            pLoc.set(setBack, player);
            if (!pLoc.hasIllegalCoords() && (cc.ignoreStance || !pLoc.hasIllegalStance())) {
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
            if (!pLoc.hasIllegalCoords() && (cc.ignoreStance || !pLoc.hasIllegalStance())) {
                event.setFrom(loc);
                event.setTo(loc);
                restored = true;
            }
        }
        pLoc.cleanup();
        if (!restored) {
            // TODO: reset the bounding box of the player ?
            if (cc.tempKickIllegal) {
                NCPAPIProvider.getNoCheatPlusAPI().denyLogin(player.getName(), 24L * 60L * 60L * 1000L);
                StaticLog.logSevere("[NCP] could not restore location for " + player.getName() + ", kicking them and deny login for 24 hours");
            } else {
                StaticLog.logSevere("[NCP] could not restore location for " + player.getName() + ", kicking them.");
            }
            CheckUtils.kickIllegalMove(player, cc);
        }
    }

    /**
     * Used for a workaround that resets the set back for the case of jumping on just placed blocks.
     * @param id
     * @return
     */
    public static boolean canJumpOffTop(final Material blockType) {
        return BlockProperties.isGround(blockType) || BlockProperties.isSolid(blockType);
    }

    /**
     * Check the context-independent pre-conditions for checking for untracked
     * locations (not the world spawn, location is not passable, passable is
     * enabled for the player).
     * 
     * @param player
     * @param loc
     * @return
     */
    public static boolean shouldCheckUntrackedLocation(final Player player, final Location loc) {
        return !TrigUtil.isSamePos(loc, loc.getWorld().getSpawnLocation()) 
                && !BlockProperties.isPassable(loc)
                && CheckType.MOVING_PASSABLE.isEnabled(player);
    }

    /**
     * Detect if the given location is an untracked spot. This is spots for
     * which a player is at the location, but the moving data has another
     * "last to" position set for that player. Note that one matching player
     * with "last to" being consistent is enough to let this return null, world spawn is exempted.
     * <hr>
     * Pre-conditions:<br>
     * <li>Context-specific (e.g. activation flags for command, teleport).</li>
     * <li>See MovingUtils.shouldCheckUntrackedLocation.</li>
     * 
     * @param loc
     * @return Corrected location, if loc is an "untracked location".
     */
    public static Location checkUntrackedLocation(final Location loc) {
        // TODO: More efficient method to get entities at the same position (might use MCAccess).
        final Chunk toChunk = loc.getChunk();
        final Entity[] entities = toChunk.getEntities();
        MovingData untrackedData = null;
        for (int i = 0; i < entities.length; i++) {
            final Entity entity = entities[i];
            if (entity.getType() != EntityType.PLAYER) {
                continue;
            }
            final Location refLoc = entity.getLocation(useLoc);
            // Exempt world spawn.
            // TODO: Exempt other warps -> HASH based exemption (expire by time, keep high count)?
            if (TrigUtil.isSamePos(loc, refLoc) && (entity instanceof Player)) {
                final Player other = (Player) entity;
                final MovingData otherData = MovingData.getData(other);
                final PlayerMoveData otherLastMove = otherData.playerMoves.getFirstPastMove();
                if (!otherLastMove.toIsValid) {
                    // Data might have been removed.
                    // TODO: Consider counting as tracked?
                    continue;
                }
                else if (TrigUtil.isSamePos(refLoc, otherLastMove.to.getX(), otherLastMove.to.getY(), otherLastMove.to.getZ())) {
                    // Tracked.
                    return null;
                }
                else {
                    // Untracked location.
                    // TODO: Discard locations in the same block, if passable.
                    // TODO: Sanity check distance?
                    // More leniency: allow moving inside of the same block.
                    if (TrigUtil.isSameBlock(loc, otherLastMove.to.getX(), otherLastMove.to.getY(), otherLastMove.to.getZ()) && !BlockProperties.isPassable(refLoc.getWorld(), otherLastMove.to.getX(), otherLastMove.to.getY(), otherLastMove.to.getZ())) {
                        continue;
                    }
                    untrackedData = otherData;
                }
            }
        }
        useLoc.setWorld(null); // Cleanup.
        if (untrackedData == null) {
            return null;
        }
        else {
            // TODO: Count and log to TRACE_FILE, if multiple locations would match (!).
            final PlayerMoveData lastMove = untrackedData.playerMoves.getFirstPastMove();
            return new Location(loc.getWorld(), lastMove.to.getX(), lastMove.to.getY(), lastMove.to.getZ(), loc.getYaw(), loc.getPitch());
        }
    }

    /**
     * Convenience method for the case that the server has already reset the
     * fall distance, e.g. with micro moves.
     * 
     * @param player
     * @param fromY
     * @param toY
     * @param data
     * @return
     */
    public static double getRealisticFallDistance(final Player player, final double fromY, final double toY, final MovingData data) {
        if (CheckType.MOVING_NOFALL.isEnabled(player)) { // Not optimal
            // (NoFall will not be checked, if this method is called.)
            if (data.noFallMaxY >= fromY ) {
                return Math.max(0.0, data.noFallMaxY - toY);
            } else {
                return Math.max(0.0, fromY - toY); // Skip to avoid exploits: + player.getFallDistance()
            }
        } else {
            // TODO: This would ignore the first split move, if this is the second one.
            return (double) player.getFallDistance() + Math.max(0.0, fromY - toY);
        }
    }

    /**
     * Ensure we have a set back location set, plus allow moving from upwards
     * with respawn/login. Intended for MovingListener (pre-checks).
     * 
     * @param player
     * @param from
     * @param data
     */
    public static void checkSetBack(final Player player, final PlayerLocation from, final MovingData data, final IDebugPlayer idp) {
        if (!data.hasSetBack()) {
            data.setSetBack(from);
        }
        else if (data.joinOrRespawn && from.getY() > data.getSetBackY() && 
                TrigUtil.isSamePos(from.getX(), from.getZ(), data.getSetBackX(), data.getSetBackZ()) &&
                (from.isOnGround() || from.isResetCond())) {
            // TODO: Move most to a method?
            // TODO: Is a margin needed for from.isOnGround()? [bukkitapionly]
            if (data.debug) {
                // TODO: Should this be info?
                idp.debug(player, "Adjust set back after join/respawn: " + from.getLocation());
            }
            data.setSetBack(from);
            data.resetPlayerPositions(from);
        }
    }

    public static double getJumpAmplifier(final Player player, final MCAccess mcAccess) {
        final double amplifier = mcAccess.getJumpAmplifier(player);
        if (Double.isInfinite(amplifier)) {
            return 0.0;
        }
        else {
            return 1.0 + amplifier;
        }
    }

    public static void prepareFullCheck(final RichBoundsLocation from, final RichBoundsLocation to, final MoveData thisMove, final double yOnGround) {
        // Collect block flags.
        from.collectBlockFlags(yOnGround);
        if (from.isSamePos(to)) {
            // TODO: Could consider pTo = pFrom, set pitch / yaw elsewhere.
            // Sets all properties, but only once.
            to.prepare(from);
        }
        else {
            // Might collect block flags for small distances with the containing bounds for both. 
            to.collectBlockFlags(yOnGround);
        }

        // Set basic properties for past move bookkeeping.
        thisMove.setExtraProperties(from, to);
    }

    /**
     * Ensure nearby chunks are loaded so that the move can be processed at all.
     * Assume too big moves to be cancelled anyway and/or checks like passable
     * accounting for chunk load. Further skip chunk loading if the latest
     * stored past move has extra properties set and is close by.
     * 
     * @param from
     * @param to
     * @param lastMove
     * @param tag
     *            The type of context/event for debug logging.
     * @param data
     * @param cc
     */
    public static void ensureChunksLoaded(final Player player, final Location from, final Location to, final PlayerMoveData lastMove, 
            final String tag, final MovingData data, final MovingConfig cc) {
        // (Worlds must be equal. Ensured in player move handling.)
        final double x0 = from.getX();
        final double z0 = from.getZ();
        final double x1 = to.getX();
        final double z1 = to.getZ();
        if (TrigUtil.distanceSquared(x0, z0, x1, z1) > 2.0 * Magic.CHUNK_LOAD_MARGIN_MIN) {
            // Assume extreme move to trigger.
            return;
        }
        boolean loadFrom = true;
        boolean loadTo = true;
        double margin = Magic.CHUNK_LOAD_MARGIN_MIN;
        // Heuristic for if loading may be necessary at all.
        if (lastMove.toIsValid && lastMove.to.extraPropertiesValid) {
            if (TrigUtil.distanceSquared(lastMove.to, x0, z0) < 1.0) {
                loadFrom = false;
            }
            if (TrigUtil.distanceSquared(lastMove.to, x1, z1) < 1.0) {
                loadTo = false;
            }
        }
        else if (lastMove.valid && lastMove.from.extraPropertiesValid
                && cc.loadChunksOnJoin) {
            // TODO: Might need to distinguish join/teleport/world-change later.
            if (TrigUtil.distanceSquared(lastMove.from, x0, z0) < 1.0) {
                loadFrom = false;
            }
            if (TrigUtil.distanceSquared(lastMove.from, x1, z1) < 1.0) {
                loadTo = false;
            }
        }
        int loaded = 0;
        if (loadFrom) {
            loaded += MapUtil.ensureChunksLoaded(from.getWorld(), x0, z0, margin);
            if (TrigUtil.distanceSquared(x0, z0, x1, z1) < 1.0) {
                loadTo = false;
            }
        }
        if (loadTo) {
            loaded += MapUtil.ensureChunksLoaded(to.getWorld(), x1, z1, margin);
        }
        if (loaded > 0 && data.debug) {
            StaticLog.logInfo("Player " + tag + ": Loaded " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + from.getWorld().getName() +  " for player: " + player.getName());
        }
    }

    /**
     * Ensure nearby chunks are loaded. Further skip chunk loading if the latest
     * stored past move has extra properties set and is close by.
     * 
     * @param player
     * @param loc
     * @param tag
     *            The type of context/event for debug logging.
     * @param data
     * @param cc
     */
    public static void ensureChunksLoaded(final Player player, final Location loc, final String tag, 
            final MovingData data, final MovingConfig cc) {
        final PlayerMoveData lastMove = data.playerMoves.getFirstPastMove();
        final double x0 = loc.getX();
        final double z0 = loc.getZ();
        // Heuristic for if loading may be necessary at all.
        if (lastMove.toIsValid && lastMove.to.extraPropertiesValid) {
            if (TrigUtil.distanceSquared(lastMove.to, x0, z0) < 1.0) {
                return;
            }
        }
        else if (lastMove.valid && lastMove.from.extraPropertiesValid
                && cc.loadChunksOnJoin) {
            // TODO: Might need to distinguish join/teleport/world-change later.
            if (TrigUtil.distanceSquared(lastMove.from, x0, z0) < 1.0) {
                return;
            }
        }
        int loaded = MapUtil.ensureChunksLoaded(loc.getWorld(), loc.getX(), loc.getZ(), Magic.CHUNK_LOAD_MARGIN_MIN);
        if (loaded > 0 && data.debug) {
            StaticLog.logInfo("Player " + tag + ": Loaded " + loaded + " chunk" + (loaded == 1 ? "" : "s") + " for the world " + loc.getWorld().getName() +  " for player: " + player.getName());
        }
    }

    /**
     * Test if a set back is set in data and scheduled. <br>
     * Primary thread only.
     * 
     * @param player
     * @return
     */
    public static boolean hasScheduledPlayerSetBack(final Player player) {
        return hasScheduledPlayerSetBack(player.getUniqueId(), MovingData.getData(player));
    }

    /**
     * Test if a set back is set in data and scheduled. <br>
     * Primary thread only.
     * @param playerId
     * @param data
     * @return
     */
    public static boolean hasScheduledPlayerSetBack(final UUID playerId, final MovingData data) {
        return data.hasTeleported() && DataManager.getPlayerData(playerId).isPlayerSetBackScheduled();
    }

    /**
     * 
     * @param player
     * @param debugMessagePrefix
     * @return
     */
    public static boolean processStoredSetBack(final Player player, final String debugMessagePrefix) {
        final MovingData data = MovingData.getData(player);
        if (!data.hasTeleported()) {
            if (data.debug) {
                CheckUtils.debug(player, CheckType.MOVING, debugMessagePrefix + "No stored location available.");
            }
            return false;
        }
        // (teleported is set.).
        final Location loc = player.getLocation(useLoc);
        if (data.isTeleportedPosition(loc)) {
            // Skip redundant teleport.
            if (data.debug) {
                CheckUtils.debug(player, CheckType.MOVING, debugMessagePrefix + "Skip teleport, player is there, already.");
            }
            data.resetTeleported(); // Not necessary to keep.
            useLoc.setWorld(null);
            return false;
        }
        useLoc.setWorld(null);
        // (player is somewhere else.)
        // TODO: Consider to skip checking for packet level, if not available (plus optimize access).
        // TODO: Consider a config flag, so this can be turned off (set back method).
        final PlayerSetBackMethod method = MovingConfig.getConfig(player).playerSetBackMethod;
        if ((method.shouldCancel() || method.shouldSetTo()) && method.shouldUpdateFrom()) {
            /*
             * Another leniency option: Skip, if we have already received an
             * ACK for this position on packet level.
             */
            // (CANCEL + UPDATE_FROM mean a certain teleport to the set back, still could be repeated tp.)
            final CountableLocation cl = ((NetData) CheckType.NET.getDataFactory().getData(player)).teleportQueue.getLastAck();
            if (data.isTeleportedPosition(cl)) {
                if (data.debug) {
                    CheckUtils.debug(player, CheckType.MOVING, debugMessagePrefix + "Skip teleport, having received an ACK for the teleport on packet level.");
                }
                // Keep teleported in data. Subject to debug logs and/or discussion.
                return false;
            }
        }

        // (No ACK received yet.)
        final Location teleported = data.getTeleported();
        // (Data resetting is done during PlayerTeleportEvent handling.)
        if (player.teleport(teleported, BridgeMisc.TELEPORT_CAUSE_CORRECTION_OF_POSITION)) {
            return true;
        }
        else {
            if (data .debug) {
                CheckUtils.debug(player, CheckType.MOVING, "Player set back on tick: Teleport failed.");
            }
            return false;
        }
    }

}
