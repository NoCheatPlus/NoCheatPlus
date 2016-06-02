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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ChecData for async checks like chat, actually implementing cached permissions.
 * @author mc_dev
 *
 */
public abstract class AsyncCheckData extends ACheckData {

    // TODO: consider using a PermissionEntry class with a timestamp to schedule renewing it.
    // TODO: consider using a normal HashMap and ensure by contract that the permissions get filled at login, so updates are thread safe.

    public AsyncCheckData(ICheckConfig config) {
        super(config);
    }

    /** The permissions that are actually cached. */
    protected final Map<String, Boolean> cachedPermissions = Collections.synchronizedMap(new HashMap<String, Boolean>());

    @Override
    public boolean hasCachedPermissionEntry(final String permission) {
        return cachedPermissions.containsKey(permission);
    }

    @Override
    public boolean hasCachedPermission(final String permission) {
        final Boolean has = cachedPermissions.get(permission);
        return (has == null) ? false : has.booleanValue();
    }

    @Override
    public void setCachedPermission(final String permission, final boolean has){
        cachedPermissions.put(permission, has);
    }

}
