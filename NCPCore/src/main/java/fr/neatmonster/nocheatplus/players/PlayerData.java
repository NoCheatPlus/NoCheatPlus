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
package fr.neatmonster.nocheatplus.players;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.data.ICanHandleTimeRunningBackwards;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.permissions.PermissionInfo;
import fr.neatmonster.nocheatplus.permissions.PermissionNode;
import fr.neatmonster.nocheatplus.permissions.PermissionPolicy.FetchingPolicy;
import fr.neatmonster.nocheatplus.permissions.PermissionRegistry;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;
import fr.neatmonster.nocheatplus.utilities.ds.corw.DualSet;
import fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW;

/**
 * Central player-specific data object.
 * <ul>
 * <li>Access to on-tick request functionality.</li>
 * <li>TBD: Permission cache.</li>
 * <li>TBD: Check data.</li>
 * <li>TBD: Exemptions</li>
 * <li>...</li>
 * </ul>
 * <hr>
 * Creating PlayerData must always be thread-safe and fail-safe.
 * <hr>
 * OLD javadocs to be cleaned up (...):<br>
 * On the medium run this is intended to carry all data for the player...
 * <li>Checks data objects.</li>
 * <li>Time stamps for logged out players</li>
 * <li>Data to be persisted, like set backs, xray.</li> <br>
 * Might contain...
 * <li>References of configs.</li>
 * <li>Exemption entries.</li>
 * <li>Player references
 * <li>
 * <hr>
 * Main reasons are...
 * <li>Faster cross-check data access both for check and data management.</li>
 * <li>Have the data in one place, easy to control and manage.</li>
 * <li>Easier transition towards non-static access, if it should ever
 * happen.</li>
 * <hr>
 * (not complete)<br>
 * Might contain individual settings such as debug flags, exemption,
 * notification settings, task references.
 * 
 * @author asofold
 *
 */
public class PlayerData implements IData, ICanHandleTimeRunningBackwards {

    /*
     * TODO: Still consider interfaces, even if this is the only implementation.
     * E.g. for requesting on-tick action, permission-related, (check-)
     * data-related.
     */

    // TODO: Use the same lock for permissions stuff ?

    // Default tags.
    public static final String TAG_NOTIFY_OFF = "notify_off";

    private static final short frequentTaskLazyDefaultDelay = 10;
    private static final short frequentTaskUnregisterDefaultDelay = 2;

    //////////////
    // Instance //
    //////////////

    /** Not sure this is the future of extra properties. */
    private Set<String> tags = null;

    /*
     * TODO: Consider updating the UUID for stuff like
     * "exempt player/name on next login". This also implies the addition of a
     * method to force-postpone data removal, as well as configuration for how
     * exactly to apply/timeout, plus new syntax for 'ncp exempt' (flags/side
     * conditions like +login/...).
     */
    /** Unique id of the player. */
    final UUID playerId;

    // TODO: Names should get updated. (In which case)
    /** Exact case name of the player. */
    final String playerName;
    /** Lower case name of the player. */
    final String lcName;

    /*
     * TODO: Flags/counters for (async-login,) login, join, 'online', kick, quit
     * + shouldBeOnline(). 'online' means that some action has been recorded.
     * Same/deduce: isFake(), as opposed to ExemptionSettings.isRegardedAsNPC().
     */

    /** A reference for handling the permission cache with policies. */
    private final PermissionRegistry permissionRegistry;

    /** Permission cache. */
    private final HashMapLOW<Integer, PermissionNode> permissions = new HashMapLOW<Integer, PermissionNode>(35);

    private boolean requestUpdateInventory = false;
    private boolean requestPlayerSetBack = false;

    private boolean frequentPlayerTaskShouldBeScheduled = false;
    /** Actually queried ones. */
    private final DualSet<RegisteredPermission> updatePermissions = new DualSet<RegisteredPermission>();
    /** Possibly needed in future. */
    private final DualSet<RegisteredPermission> updatePermissionsLazy = new DualSet<RegisteredPermission>();

    /** Unregister the tasks once 0 count is reached. */
    private short frequentTaskDelayUnregister = 0;
    /** Run lazy stuff on reaching zero. */
    private short frequentTaskDelayLazy = 0;

    /**
     * Note that PlayerData might get instantiated optimistically, and then be
     * discarded, if the map already contains one - due to the implementation of
     * concurrency support.
     * 
     * @param playerName
     *            Should be accurate case - TBD: ensure it's used that way,
     *            and/or update with login/join.
     */
    public PlayerData(final UUID playerId, final String playerName, 
            final PermissionRegistry permissionRegistry) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.lcName = playerName.toLowerCase();
        this.permissionRegistry = permissionRegistry;
    }

    /**
     * Run with DataManager frequent tasks (each tick).
     * 
     * @param tick
     * @param timeLast
     * @return True, of the listener is to be removed, false otherwise.
     */
    protected boolean processTickFrequent(final int tick, final long timeLast) {
        if (frequentTaskDelayUnregister == 0) {
            frequentTaskDelayUnregister = frequentTaskUnregisterDefaultDelay;
        }
        if (frequentTaskDelayLazy == 0) {
            frequentTaskDelayLazy = frequentTaskLazyDefaultDelay;
        }
        boolean busy = false;
        final Player player = DataManager.getPlayer(playerId);
        if (hasFrequentTasks()) {
            frequentTasks(tick, timeLast, player);
            busy = true;
        }
        if (--frequentTaskDelayLazy == 0) {
            if (!lazyTasks(tick, timeLast, player)) {
                busy = true;
            }
        }
        if (busy || frequentTaskDelayLazy > 0) {
            frequentTaskDelayUnregister = frequentTaskUnregisterDefaultDelay;
            return false;
        }
        else if (--frequentTaskDelayUnregister == 0) {
            // Check for lazy task.
            if (hasLazyTasks() || hasFrequentTasks()) {
                frequentTaskDelayLazy = frequentTaskLazyDefaultDelay;
                return false;
            }
            else {
                // Unregister.
                frequentPlayerTaskShouldBeScheduled = false; // uh oh.
                return true;
            }
        }
        else {
            // Stay a while.
            return false;
        }
    }

    private boolean hasFrequentTasks() {
        return !updatePermissions.isEmtpyAfterMergePrimaryThread() 
                // Should be primary thread:
                || requestPlayerSetBack || requestUpdateInventory;
    }

    @SuppressWarnings("deprecation")
    private void frequentTasks(final int tick, final long timeLast, final Player player) {
        if (player != null) { // Common criteria ...
            if (requestPlayerSetBack) {
                requestPlayerSetBack = false;
                MovingUtil.processStoredSetBack(player, "Player set back on tick: ");
            }
            if (player.isOnline()) {
                if (requestUpdateInventory) {
                    requestUpdateInventory = false;
                    player.updateInventory();
                }
                final Collection<RegisteredPermission> updatable = updatePermissions.getMergePrimaryThreadAndClear();
                if (updatable != null) {
                    for (final RegisteredPermission registeredPermission : updatable) {
                        // (Force update could be inefficient.)
                        hasPermission(registeredPermission, player);
                    }
                }
            } // (The player is online.)
        } // (The player is not null.)

    }

    private boolean hasLazyTasks() {
        return !updatePermissionsLazy.isEmtpyAfterMergePrimaryThread();
    }

    /**
     * 
     * @param tick
     * @param timeLast
     * @return True, once all lazy stuff has been processed (no unregister delay
     *         here).
     */
    private boolean lazyTasks(final int tick, final long timeLast, final Player player) {
        if (player == null) {
            return true;
        }
        updatePermissionsLazy.mergePrimaryThread();
        final Iterator<RegisteredPermission> it = updatePermissionsLazy.iteratorPrimaryThread();
        while (it.hasNext()) {
            hasPermission(it.next(), player);
            it.remove();
        }
        boolean hasWrk = it.hasNext();
        return !hasWrk;
    }

    private void registerFrequentPlayerTask() {
        if (Bukkit.isPrimaryThread()) {
            registerFrequentPlayerTaskPrimaryThread();
        }
        else {
            registerFrequentPlayerTaskAsynchronous();
        }
    }

    private void registerFrequentPlayerTaskPrimaryThread() {
        frequentPlayerTaskShouldBeScheduled = true;
        DataManager.registerFrequentPlayerTaskPrimaryThread(playerId);
    }

    private void registerFrequentPlayerTaskAsynchronous() {
        frequentPlayerTaskShouldBeScheduled = true;
        DataManager.registerFrequentPlayerTaskAsynchronous(playerId);
    }

    private boolean isFrequentPlayerTaskScheduled() {
        return DataManager.isFrequentPlayerTaskScheduled(playerId);
    }

    /**
     * Permission check (thread-safe, results and impact of asynchronous queries depends on
     * settings +- TBD).
     * 
     * @param registeredPermission Must not be null, must be registered.
     * @param player
     *            May be null (if lucky the permission is set to static/timed
     *            and/or has already been fetched).
     * @return
     */
    public boolean hasPermission(final RegisteredPermission registeredPermission, final Player player) {
        // Check cache and policy. 
        PermissionNode node = permissions.get(registeredPermission.getId());
        if (node == null) {
            node = getOrCreatePermissionNode(registeredPermission);
        }
        final FetchingPolicy fetchingPolicy = node.getFetchingPolicy();
        switch (fetchingPolicy) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
                // ONCE, TIMED, ALWAYS.
        }
        final AlmostBoolean lastState = node.getLastState();
        if (lastState != AlmostBoolean.MAYBE) {
            switch (fetchingPolicy) {
                case ONCE:
                    return lastState.decide();
                case INTERVAL:
                    if (System.currentTimeMillis() - node.getLastFetch() < node.getFetchInterval()) {
                        return lastState.decide();
                    }
                    // TODO: ALWAYS: Could still use cache within a check context.
                default:
                    // Must fetch.
                    break;
            }
        }
        // Permission not cached or needs to be updated with the PlayerTask.
        final AlmostBoolean fRes = fetchPermission(registeredPermission, player);
        if (fRes == AlmostBoolean.MAYBE) {
            // TODO: Get default state from info or Bukkit. Might need lastState for asynchronous ... redesign.
            return lastState.decide(); // TODO: Slight risk on left-over meant-temporary permissions.
        }
        else {
            node.setState(fRes, System.currentTimeMillis());
            return fRes.decide();
        }
    }

    private PermissionNode getOrCreatePermissionNode(final RegisteredPermission registeredPermission) {
        // Optimistic creation (concurrency).
        final PermissionNode node = new PermissionNode(permissionRegistry.getPermissionInfo(registeredPermission.getId()));
        final PermissionNode oldNode = permissions.putIfAbsent(registeredPermission.getId(), node);
        return oldNode == null ? node : oldNode;
    }

    /**
     * Fetch the permission hard, no putting to cache, just return the result.
     * For off-primary-server-thread access, this will wait for a
     * BukkitRunnable/TickTask task to finish, at an extraordinary performance
     * penalty.
     * 
     * @param registeredPermission
     * @param player
     * @return MAYBE in case permissions could not be fetched or 
     */
    private AlmostBoolean fetchPermission(final RegisteredPermission registeredPermission, Player player) {
        if (Bukkit.isPrimaryThread()) {
            if (player == null) {
                player = DataManager.getPlayer(this.playerId);
                if (player == null) {
                    return AlmostBoolean.MAYBE;
                }
            }
            // Minimal update within the primary thread.
            return player.hasPermission(registeredPermission.getBukkitPermission()) ? AlmostBoolean.YES : AlmostBoolean.NO;
        }
        else {
            requestPermissionUpdate(registeredPermission);
            /*
             * TODO: UNCERTAIN: request related permission right away ? Inefficient in case of exemption.
             * 
             */
            return AlmostBoolean.MAYBE;
        }
    }

    /**
     * Test if present.
     * 
     * @param tag
     * @return
     */
    public boolean hasTag(final String tag) {
        return tags != null && tags.contains(tag);
    }

    /**
     * Add the tag.
     * 
     * @param tag
     */
    public void addTag(final String tag) {
        if (tags == null) {
            tags = new HashSet<String>();
        }
        tags.add(tag);
    }

    /**
     * Remove the tag.
     * 
     * @param tag
     */
    public void removeTag(final String tag) {
        if (tags != null) {
            tags.remove(tag);
            if (tags.isEmpty()) {
                tags = null;
            }
        }
    }

    /**
     * Add tag or remove tag, based on arguments.
     * 
     * @param tag
     * @param add
     *            The tag will be added, if set to true. If set to false, the
     *            tag will be removed.
     */
    public void setTag(final String tag, final boolean add) {
        if (add) {
            addTag(tag);
        }
        else {
            removeTag(tag);
        }
    }

    /**
     * Check if notifications are turned off, this does not bypass permission
     * checks.
     * 
     * @return
     */
    public boolean getNotifyOff() {
        return hasTag(TAG_NOTIFY_OFF);
    }

    /**
     * Allow or turn off notifications. A player must have the admin.notify
     * permission to receive notifications.
     * 
     * @param notifyOff
     *            set to true to turn off notifications.
     */
    public void setNotifyOff(final boolean notifyOff) {
        setTag(TAG_NOTIFY_OFF, notifyOff);
    }

    /**
     * Let the inventory be updated (run in TickTask).
     */
    public void requestUpdateInventory() {
        this.requestUpdateInventory = true;
        registerFrequentPlayerTask();
    }

    /**
     * Let the player be set back to the location stored in moving data (run in
     * TickTask). Only applies if it's set there.
     */
    public void requestPlayerSetBack() {
        this.requestPlayerSetBack = true;
        registerFrequentPlayerTask();
    }

    /**
     * Test if it's set to process a player set back on tick. This does not
     * check MovingData.hasTeleported().
     * 
     * @return
     */
    public boolean isPlayerSetBackScheduled() {
        return this.requestPlayerSetBack 
                && (frequentPlayerTaskShouldBeScheduled || isFrequentPlayerTaskScheduled());
    }

    @Override
    public void handleTimeRanBackwards() {
        final Iterator<Entry<Integer, PermissionNode>> it = permissions.iterator();
        final long timeNow = System.currentTimeMillis();
        while (it.hasNext()) {
            final PermissionNode node = it.next().getValue();
            switch (node.getFetchingPolicy()) {
                case INTERVAL:
                    node.invalidate();
                    break;
                case ONCE:
                    node.setState(node.getLastState(), timeNow);
                    break;
                default:
                    // Ignore.
                    break;
            }
        }
    }

    /**
     * Request a permission cache update.
     * @param registeredPermission
     */
    public void requestPermissionUpdate(final RegisteredPermission registeredPermission) {
        if (Bukkit.isPrimaryThread()) {
            requestPermissionUpdatePrimaryThread(registeredPermission);
        }
        else {
            requestPermissionUpdateAsynchronous(registeredPermission);
        }
    }

    protected void requestPermissionUpdatePrimaryThread(final RegisteredPermission registeredPermission) {
        // Might throw something :p.
        updatePermissions.addPrimaryThread(registeredPermission);
        registerFrequentPlayerTaskPrimaryThread();
    }

    protected void requestPermissionUpdateAsynchronous(final RegisteredPermission registeredPermission) {
        updatePermissions.addAsynchronous(registeredPermission);
        registerFrequentPlayerTaskAsynchronous();
    }

    /**
     * Low priority permission update for check type specific permissions.
     * 
     * @param registeredPermissions
     *            May be null.
     */
    public void requestLazyPermissionUpdate(final RegisteredPermission...registeredPermissions) {
        if (registeredPermissions == null) {
            return;
        }
        if (Bukkit.isPrimaryThread()) {
            requestLazyPermissionUpdatePrimaryThread(registeredPermissions);
        }
        else {
            requestLazyPermissionUpdateAsynchronous(registeredPermissions);
        }
    }

    protected void requestLazyPermissionUpdatePrimaryThread(final RegisteredPermission... registeredPermissions) {
        // Might throw something :p.
        updatePermissionsLazy.addAllPrimaryThread(Arrays.asList(registeredPermissions));
        registerFrequentPlayerTaskPrimaryThread();
    }

    protected void requestLazyPermissionUpdateAsynchronous(final RegisteredPermission... registeredPermissions) {
        updatePermissionsLazy.addAllAsynchronous(Arrays.asList(registeredPermissions));
        registerFrequentPlayerTaskAsynchronous();
    }

    void onPlayerLeave(final long timeNow) {
        invalidateOffline();
    }

    void onPlayerJoin(final long timeNow) {
        invalidateOffline();
    }

    private void invalidateOffline() {
        final Iterator<Entry<Integer, PermissionNode>> it = permissions.iterator();
        // TODO: More efficient: get unmodifiable collection from registry?
        while (it.hasNext()) {
            final PermissionNode node = it.next().getValue();
            final PermissionInfo info = node.getPermissionInfo();
            if (info.invalidationOffline() || info.invalidationWorld()) {
                // TODO: Really count leave as world change?
                node.invalidate();
            }
        }
    }

    /**
     * Early adaption.
     * 
     * @param oldWorld
     * @param newWorld
     */
    void onPlayerChangedWorld(final World oldWorld, final World newWorld) {
        // TODO: Double-invalidation (previous policy and target world policy)
        final Iterator<Entry<Integer, PermissionNode>> it = permissions.iterator();
        // TODO: More efficient: get unmodifiable collection from registry?
        while (it.hasNext()) {
            final PermissionNode node = it.next().getValue();
            final PermissionInfo info = node.getPermissionInfo();
            if (info.invalidationWorld()) {
                node.invalidate();
            }
        }
    }

    /**
     * Called with adjusting to the configuration (enable / config reload).
     * @param changedPermissions 
     */
    public void adjustSettings(final Set<RegisteredPermission> changedPermissions) {
        final Iterator<Entry<Integer, PermissionNode>> it = permissions.iterator();
        while (it.hasNext()) {
            final PermissionNode node = it.next().getValue();
            if (changedPermissions.contains(node.getPermissionInfo().getRegisteredPermission())) {
                node.invalidate();
            }
        }
    }

    /**
     * Remove extra stored data, keeping "essential" data if set so. "Essential"
     * data can't be recovered once deleted, like set-back locations for players
     * who leave in-air (once stored here at all).
     * 
     * @param keepEssentialData
     */
    public void removeData(boolean keepEssentialData) {
        permissions.clear(); // Might keep login-related permissions. Implement a 'retain-xy' or 'essential' flag?
        updatePermissions.clearPrimaryThread();
        updatePermissionsLazy.clearPrimaryThread();
    }

}
