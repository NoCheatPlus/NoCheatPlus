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
package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

/**
 * Minimal implementation, doing nothing.
 * @author mc_dev
 *
 */
public abstract class ACheckConfig implements ICheckConfig {

    /** For on the fly debug setting. */
    public boolean debug = false; // TODO: Might make private.

    /** If to adapt to server side lag. */
    public final boolean lag;

    /** Permissions to hold in player data cache, not final for flexibility. */
    protected RegisteredPermission[] cachePermissions;

    /**
     * 
     * @param config
     * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
     */
    public ACheckConfig(final ConfigFile config, final String pathPrefix){
        this(config, pathPrefix, null);
    }

    /**
     * 
     * @param config
     * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
     * @param cachePermissions  cachePermissions Permissions to hold in player data cache. Can be null.
     */
    public ACheckConfig(final ConfigFile config, final String pathPrefix, 
            final RegisteredPermission[] cachePermissions){
        // TODO: Path prefix construction is somewhat inconsistent with debug hierarchy ?
        debug = config.getBoolean(pathPrefix + ConfPaths.SUB_DEBUG, config.getBoolean(ConfPaths.CHECKS_DEBUG, false));
        // TODO: Use lag flag where appropriate and document it (or get rid of it).
        lag = config.getBoolean(pathPrefix + ConfPaths.SUB_LAG, true) && config.getBoolean(ConfPaths.MISCELLANEOUS_LAG, true);
        this.cachePermissions = cachePermissions;
    }

    @Override
    public RegisteredPermission[] getCachePermissions() {
        return cachePermissions;
    }

    @Override
    public boolean getDebug() {
        return debug;
    }

    @Override
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

}
