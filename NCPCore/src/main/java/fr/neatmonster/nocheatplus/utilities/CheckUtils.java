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
package fr.neatmonster.nocheatplus.utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.checks.blockbreak.BlockBreakData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.fight.FightData;
import fr.neatmonster.nocheatplus.checks.inventory.InventoryData;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.hooks.APIUtils;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;

// TODO: Auto-generated Javadoc
/**
 * Random auxiliary gear, some might have general quality. Contents are likely to get moved to other classes.
 */
public class CheckUtils {

    /** The Constant logOnce. */
    // TODO: Quick and dirty -> other methods elsewhere.
    private static final Set<Integer> logOnce = Collections.synchronizedSet(new HashSet<Integer>());

    /**
     * Kick and log.
     *
     * @param player
     *            the player
     * @param cc
     *            the cc
     */
    public static void kickIllegalMove(final Player player, final MovingConfig cc){
        player.kickPlayer(cc.msgKickIllegalMove);
        StaticLog.logWarning("[NCP] Disconnect " + player.getName() + " due to illegal move!");
    }

    /**
     * Guess some last-action time, likely to be replaced with centralized
     * PlayerData use.
     *
     * @param player
     *            the player
     * @param now
     *            the now
     * @param maxAge
     *            Maximum age in milliseconds.
     * @return Return timestamp or Long.MIN_VALUE if not possible or beyond
     *         maxAge.
     */
    public static final long guessKeepAliveTime(final Player player, final long now, final long maxAge){
        final int tick = TickTask.getTick();
        long ref = Long.MIN_VALUE;
        // Estimate last fight action time (important for gode modes).
        final FightData fData = FightData.getData(player); 
        ref = Math.max(ref, fData.speedBuckets.lastUpdate());
        ref = Math.max(ref, now - 50L * (tick - fData.lastAttackTick)); // Ignore lag.
        // Health regain (not unimportant).
        ref = Math.max(ref, fData.regainHealthTime);
        // Move time.
        ref = Math.max(ref, CombinedData.getData(player).lastMoveTime);
        // Inventory.
        final InventoryData iData = InventoryData.getData(player);
        ref = Math.max(ref, iData.lastClickTime);
        ref = Math.max(ref, iData.instantEatInteract);
        // BlcokBreak/interact.
        final BlockBreakData bbData = BlockBreakData.getData(player);
        ref = Math.max(ref, bbData.frequencyBuckets.lastUpdate());
        ref = Math.max(ref, bbData.fastBreakfirstDamage);
        // TODO: More, less ...
        if (ref > now || ref < now - maxAge){
            return Long.MIN_VALUE;
        }
        return ref;
    }

    /**
     * Check for config flag and exemption (hasBypass). Meant thread-safe.
     *
     * @param checkType
     *            the check type
     * @param player
     *            the player
     * @param data
     *            If data is null, the data factory will be used for the given
     *            check type.
     * @param cc
     *            If config is null, the config factory will be used for the
     *            given check type.
     * @return true, if is enabled
     */
    public static boolean isEnabled(final CheckType checkType, final Player player, final ICheckData data, final ICheckConfig cc) {
        if (cc == null) {
            if (!checkType.isEnabled(player)) {
                return false;
            }
        }
        else if (!cc.isEnabled(checkType)) {
            return false;
        }
        return !hasBypass(checkType, player, data);
    }

    /**
     * Check for exemption by permissions, API access, possibly other. Meant
     * thread-safe.
     * 
     * @see #hasBypass(CheckType, Player, ICheckData, boolean)
     *
     * @param checkType
     *            the check type
     * @param player
     *            the player
     * @param data
     *            If data is null, the data factory will be used for the given
     *            check type.
     * @return true, if successful
     */
    public static boolean hasBypass(final CheckType checkType, final Player player, final ICheckData data) {
        // TODO: Checking for the thread might be a temporary measure.
        return hasBypass(checkType, player, data, Bukkit.isPrimaryThread());
    }

    /**
     * Check for exemption by permissions, API access, possibly other. Meant
     * thread-safe.
     *
     * @param checkType
     *            the check type
     * @param player
     *            the player
     * @param data
     *            If data is null, the data factory will be used for the given
     *            check type.
     * @param isPrimaryThread
     *            If set to true, this must be the primary server thread as
     *            returned by Bukkit.isPrimaryThread().
     * @return true, if successful
     */
    public static boolean hasBypass(final CheckType checkType, final Player player, final ICheckData data,
            final boolean isPrimaryThread) {
        // TODO: Checking for the thread might be a temporary measure.
        final String permission =  checkType.getPermission();
        if (isPrimaryThread) {
            if (permission != null && player.hasPermission(permission)) {
                return true;
            }
        }
        else if (permission != null) {
            if (data == null) {
                if (checkType.hasCachedPermission(player, permission)) {
                    return true;
                }
            }
            else if (data.hasCachedPermission(permission)) {
                return true;
            }
            if (!APIUtils.needsSynchronization(checkType)) {
                // Checking for exemption can cause harm now.
                improperAPIAccess(checkType);
            }
        }
        // TODO: ExemptionManager relies on the initial definition for which type can be checked off main thread.
        // TODO: Maybe a solution: force sync into primary thread a) each time b) once with lazy force set to use copy on write [for the player or global?]. 
        return NCPExemptionManager.isExempted(player, checkType, isPrimaryThread);
    }

    /**
     * Improper api access.
     *
     * @param checkType
     *            the check type
     */
    private static void improperAPIAccess(final CheckType checkType) {
        // TODO: Log once + examine stack (which plugins/things are involved).
        final String trace = Arrays.toString(Thread.currentThread().getStackTrace());
        final int ref = trace.hashCode() ^ new Integer(trace.length()).hashCode();
        final String extra;
        final boolean details = logOnce.add(ref);
        if (details) {
            // Not already contained.
            extra = " (id=" + ref + ")";
        } else {
            extra = " (see earlier log with id=" + ref + ")";
        }
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, "Off primary thread call to hasByPass for " + checkType + extra + ".");
        if (details) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, trace);
            if (logOnce.size() > 10000) {
                logOnce.clear();
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.STATUS, "Cleared log-once ids, due to exceeding the maximum number of stored ids.");
            }
        }
    }

    /**
     * Static relay for the check-specific convenience methods, logging with
     * standard format ([check_type] [player_name] ...).
     *
     * @param player
     *            May be null.
     * @param checkType
     *            the check type
     * @param message
     *            the message
     */
    public static void debug(final Player player, final CheckType checkType, final String message) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, getLogMessagePrefix(player, checkType) + message);
    }

    /**
     * Get the standard log message prefix with a trailing space.
     *
     * @param player
     *            May be null.
     * @param checkType
     *            the check type
     * @return the log message prefix
     */
    public static String getLogMessagePrefix(final Player player, final CheckType checkType) {
        String base = "[" + checkType + "] ";
        if (player != null) {
            base += "[" + player.getName() + "] ";
        }
        return base;
    }

    /**
     * Convenience method to get a Random instance from the generic registry
     * (NoCheatPlusAPI).
     *
     * @return the random
     */
    public static Random getRandom() {
        return NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Random.class);
    }

}
