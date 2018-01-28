package fr.neatmonster.nocheatplus.permissions;

import java.util.LinkedHashSet;
import java.util.Set;

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
    /** Copy on write, primary thread write access only. */
    // TODO: Implement or remove.
    private RegisteredPermission[] preferUpdateOther = null;

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

    /**
     * Copy on write for primary thread only access.
     * 
     * @param otherPermissions
     */
    public void preferUpdateOther(final RegisteredPermission... otherPermissions) {
        final Set<RegisteredPermission> newEntries = new LinkedHashSet<RegisteredPermission>();
        if (preferUpdateOther != null) {
            for (int i = 0; i < preferUpdateOther.length; i++) {
                newEntries.add(preferUpdateOther[i]);
            }
        }
        for (int i = 0; i < otherPermissions.length; i++) {
            newEntries.add(otherPermissions[i]);
        }
        preferUpdateOther = newEntries.toArray(new RegisteredPermission[newEntries.size()]);
    }

    /**
     * Get the internally stored array with other permissions that are preferred
     * to be updated when the permission represented by this PermissionInfo is
     * to be updated. Note that the permission itself is not automatically
     * included, but external calls might add it. Aiming at requesting
     * permission updates from another thread than the primary server thread.
     * <br>
     * Thread-safe read.
     * 
     * @return
     */
    public RegisteredPermission[] preferUpdateOther() {
        return this.preferUpdateOther;
    }

}
