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

/**
 * Combine permission information (registry): RegisteredPermission, policy
 * information (default fetching policy, extra flags), (the Bukkit permission is
 * lazily fetched within RegisteredPermission).
 * 
 * @author asofold
 *
 */
public class PermissionInfo extends PermissionPolicy {

    // TODO: Consider IPermissionPolicy with read only access.

    private final RegisteredPermission registeredPermission;

    /**
     * Minimal constructor.
     * @param permission
     */
    public PermissionInfo(RegisteredPermission registeredPermission) {
        this.registeredPermission = registeredPermission;
    }

    /**
     * Initialize with a default policy.
     * 
     * @param registeredPermission
     * @param defaultPolicy
     */
    public PermissionInfo(RegisteredPermission registeredPermission, PermissionPolicy defaultPolicy) {
        super(defaultPolicy);
        this.registeredPermission = registeredPermission;
    }

    public RegisteredPermission getRegisteredPermission() {
        return registeredPermission;
    }

}
