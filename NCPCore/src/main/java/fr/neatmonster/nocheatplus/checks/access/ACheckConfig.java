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

import fr.neatmonster.nocheatplus.components.config.ICheckConfig;
import fr.neatmonster.nocheatplus.worlds.IWorldData;

/**
 * Minimal implementation, doing nothing.
 * @author asofold
 *
 */
public abstract class ACheckConfig implements ICheckConfig {

    /** World data storage for this world. */
    public final IWorldData worldData;

    /**
     * 
     * @param config
     * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
     * @param cachePermissions  cachePermissions Permissions to hold in player data cache. Can be null.
     */
    public ACheckConfig(final IWorldData worldData){
        this.worldData = worldData;
    }


}
