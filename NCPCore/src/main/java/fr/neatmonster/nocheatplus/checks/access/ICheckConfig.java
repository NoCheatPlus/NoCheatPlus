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

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

/**
 * This interface must be implemented by all configuration classes.
 * 
 * @author asofold
 */
public interface ICheckConfig {

    /**
     * Checks if a check is enabled.
     * 
     * @param checkType
     *            the check type
     * @return true, if the check is enabled
     */
    public boolean isEnabled(CheckType checkType);

    /** On the fly debug flags, to be set by commands and similar. */
    public boolean getDebug();

    /** On the fly debug flags, to be set by commands and similar. */ 
    public void setDebug(boolean debug);

    /**
     * Retrieve the permissions that have to be updated for this check.
     * @return An array of permissions, may be null.
     */
    public RegisteredPermission[] getCachePermissions();

}
