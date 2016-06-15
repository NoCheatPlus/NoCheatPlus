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

import fr.neatmonster.nocheatplus.components.registry.feature.IData;

/**
 * Central player data object.
 * <hr>
 * On the medium run this is intended to carry all data for the player...
 * <li>Checks data objects.</li>
 * <li>Time stamps for logged out players</li>
 * <li>Data to be persisted, like set-backs, xray.</li>
 * <br>Might contain...
 * <li>References of configs.</li>
 * <li>Exemption entries.</li>
 * <li>Player references<li>
 * <hr>
 * Main reasons are...
 * <li>Faster cross-check data access both for check and data management.</li>
 * <li>Have the data in one place, easy to control and manage.</li>
 * <li>Easier transition towards non-static access, if it should ever happen.</li>
 * <hr>
 * (not complete)<br>
 * Might contain individual settings such as debug flags, exemption, notification settings, task references.
 * @author mc_dev
 *
 */
public class PlayerData implements IData {

    public static final String TAG_NOTIFY_OFF = "notify_off";

    public final PlayerTask task;

    /** Not sure this is the future of extra properties. */
    protected Set<String> tags = null;

    /** Lower case name of the player. */
    final String lcName;

    /**
     * 
     * @param playerName Accurate case not (yet) demanded.
     */
    public PlayerData(final String playerName) {
        this.lcName = playerName.toLowerCase();
        this.task = new PlayerTask(this.lcName);
    }

    /**
     * Test if present.
     * @param tag
     * @return
     */
    public boolean hasTag(final String tag) {
        return tags != null && tags.contains(tag);
    }

    /**
     * Add the tag.
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
     * @param tag
     * @param add The tag will be added, if set to true. If set to false, the tag will be removed.
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
     * Check if notifications are turned off, this does not bypass permission checks.
     * @return
     */
    public boolean getNotifyOff() {
        return hasTag(TAG_NOTIFY_OFF);
    }

    /**
     * Allow or turn off notifications. A player must have the admin.notify permission to receive notifications.
     * @param notifyOff set to true to turn off notifications.
     */
    public void setNotifyOff(final boolean notifyOff) {
        setTag(TAG_NOTIFY_OFF, notifyOff);
    }

}
