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
package fr.neatmonster.nocheatplus.permissions;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.permissions.PermissionPolicy.FetchingPolicy;

/**
 * Per player per permission runtime information (PlayerData). This is kept
 * minimal, in order to fit many into some cache - more properties and mapping
 * has to be relayed to the registry.
 * 
 * @author asofold
 *
 */
public class PermissionNode {

    private final PermissionInfo info;
    private long lastFetch = 0;
    private AlmostBoolean lastState = AlmostBoolean.MAYBE;

    public PermissionNode(PermissionInfo info) {
        this.info = info;
        /*
         * Might store extras like fetchingPolicy here, for most efficient
         * access. Affects performance of config reload too / to be seen.
         */
    }

    public PermissionInfo getPermissionInfo() {
        return info;
    }

    public FetchingPolicy getFetchingPolicy() {
        return info.fetchingPolicy();
    }

    /**
     * 
     * @param state
     * @param time 
     */
    public void setState(AlmostBoolean state, long time) {
        this.lastState = state;
        this.lastFetch = time;
    }

    /**
     * In case of 0, the entry has not been fetched yet. Note that invalidation
     * doesn't reset the time.
     * 
     * @return
     */
    public long getLastFetch() {
        return lastFetch;
    }

    public long getFetchInterval() {
        return info.fetchingInterval();
    }

    /**
     * 
     * @return MAYBE means 'invalid', including 'not set'.
     */
    public AlmostBoolean getLastState() {
        return lastState;
    }

    public void invalidate() {
        lastState = AlmostBoolean.MAYBE;
    }

}
