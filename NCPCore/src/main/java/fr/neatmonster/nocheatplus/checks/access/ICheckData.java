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

import fr.neatmonster.nocheatplus.components.data.IData;

/**
 * This is for future purposes. Might remove...<br>
 * Some checks in chat synchronize over data, so using this from exectueActions can deadlock.<br>
 * One might think of making this an interface not for the internally used data, but for copy of data for external use
 * only. Then sync could go over other objects for async access.
 * 
 * @author asofold
 */
public interface ICheckData extends IData{

    /**
     * Set if to trace/debug this player for the associated checks.
     * @param debug
     */
    public void setDebug(boolean debug);

    /**
     * Test if to trace/debug this player for the associated checks.
     * @return
     */
    public boolean getDebug();


    /**
     * Check if an entry for the given permission exists.
     * @param permission
     * @return
     */
    public boolean hasCachedPermissionEntry(String permission);
    /**
     * Check if the user has the permission. If no entry is present, a false result is assumed an after failure check is made and the cache must be registered for updating.
     * @param permission
     * @return
     */
    public boolean hasCachedPermission(String permission);

    /**
     * Set a cached permission.
     * @param permission
     * @param value
     */
    public void setCachedPermission(String permission, boolean value);
}
