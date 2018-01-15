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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.moving.util.MovingUtil;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.utilities.TickTask;

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
public class PlayerData implements IData {

    /*
     * TODO: Still consider interfaces, even if this is the only implementation.
     * E.g. for requesting on-tick action, permission-related, (check-)
     * data-related.
     */

    /**
     * Interface for TickTask. Uses the UUID for hash and equals, equals accepts
     * UUID instances as well.
     * 
     * @author asofold
     *
     */
    public static final class PlayerTickListener {

        private final PlayerData data;
        private final int hashCode;

        private PlayerTickListener(PlayerData data) {
            this.data = data;
            this.hashCode = data.playerId.hashCode();
        }

        /**
         * 
         * @param tick
         * @param timeLast
         * @return True , if the listener is to be removed, false otherwise.
         */
        public boolean processOnTick(int tick, long timeLast) {
            return data.processOnTick(tick, timeLast);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PlayerTickListener) {
                return this.data.playerId.equals(((PlayerTickListener) obj).data.playerId);
            }
            else if (obj instanceof UUID) {
                return this.data.playerId.equals((UUID) obj);
            }
            else {
                return false;
            }
        }

    }

    // Default tags.
    public static final String TAG_NOTIFY_OFF = "notify_off";


    /////////////
    // Instance
    /////////////

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

    private final PlayerTickListener playerTickListener;

    private boolean requestUpdateInventory = false;
    private boolean requestPlayerSetBack = false;

    /**
     * 
     * @param playerName
     *            Accurate case not (yet) demanded.
     */
    public PlayerData(final UUID playerId, final String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.lcName = playerName.toLowerCase();
        this.playerTickListener = new PlayerTickListener(this);
    }

    /**
     * Run with TickTask.
     * @param tick
     * @param timeLast
     * @return True, of the listener is to be removed, false otherwise.
     */
    @SuppressWarnings("deprecation")
    private boolean processOnTick(final int tick, final long timeLast) {
        final Player player = DataManager.getPlayer(playerId);
        if (player != null) { // Common criteria ...
            if (requestPlayerSetBack) {
                MovingUtil.processStoredSetBack(player, "Player set back on tick: ");
            }
            if (player.isOnline()) {
                if (requestUpdateInventory) {
                    player.updateInventory();
                }
            } // (The player is online.)
        } // (The player is not null.)
        // Reset request flags.
        requestPlayerSetBack = requestUpdateInventory = false;
        return true;
    }

    private void registerPlayerTickListener() {
        TickTask.addPlayerTickListener(playerTickListener);
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
        registerPlayerTickListener();
    }

    /**
     * Let the player be set back to the location stored in moving data (run in
     * TickTask). Only applies if it's set there.
     */
    public void requestPlayerSetBack() {
        this.requestPlayerSetBack = true;
        registerPlayerTickListener();
    }

    /**
     * Test if it's set to process a player set back on tick. This does not
     * check MovingData.hasTeleported().
     * 
     * @return
     */
    public boolean isPlayerSetBackScheduled() {
        return this.requestPlayerSetBack && TickTask.isPlayerTiskListenerThere(this.playerTickListener);
    }

}
