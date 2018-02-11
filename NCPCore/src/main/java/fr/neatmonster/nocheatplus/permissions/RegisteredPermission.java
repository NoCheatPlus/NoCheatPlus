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

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

/**
 * A permission registered with the PermissionRegistry. Essentially contains an
 * id and the string representation. The hashCode and equals methods are
 * implemented towards use with Integer keys (ids).
 * 
 * @author asofold permission
 */
public class RegisteredPermission {

    public static final String toLowerCaseStringRepresentation(final String stringRepresentation) {
        return stringRepresentation.toLowerCase(java.util.Locale.ENGLISH);
    }

    private final Integer id;
    private final String stringRepresentation;
    /** What Bukkit does internally. */
    private final String lowerCaseStringRepresentation;
    private final String description;
    /** Lazily updated. */
    private Permission bukkitPermission = null;

    public RegisteredPermission(Integer id, String stringRepresentation) {
        this(id, stringRepresentation, null);
    }

    public RegisteredPermission(Integer id, String stringRepresentation, String description) {
        if (id == null) {
            throw new NullPointerException("Can't have a null id.");
        }
        if (stringRepresentation == null) {
            // (Later this might be possible.)
            throw new NullPointerException("Can't have a null stringRepresentation.");
        }
        this.id = id;
        this.stringRepresentation = stringRepresentation;
        this.lowerCaseStringRepresentation = toLowerCaseStringRepresentation(stringRepresentation);
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public String getLowerCaseStringRepresentation() {
        return lowerCaseStringRepresentation;
    }

    public String getDescription() {
        return description;
    }
    
    public Permission getBukkitPermission() {
        return bukkitPermission == null ? fetchBukkitPermission() : bukkitPermission;
    }

    private Permission fetchBukkitPermission() {
        // Create with lower case string representation anyway.
        final PluginManager pm = Bukkit.getPluginManager();
        Permission permission = pm.getPermission(lowerCaseStringRepresentation);
        if (permission == null) {
            // Assume this one doesn't have children etc.
            permission = new Permission(lowerCaseStringRepresentation, 
                    PermissionUtil.AUTO_GENERATED, 
                    PermissionDefault.FALSE // Cautious, perhaps.
                    );
            pm.addPermission(permission);
        }
        return permission;
    }

    @Override
    public String toString() {
        return getStringRepresentation();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Integer) {
            return id.intValue() == ((Integer) obj).intValue();
        }
        else if (obj instanceof RegisteredPermission) {
            return id.intValue() == ((RegisteredPermission) obj).getId().intValue();
        }
        return false;
    }

}
