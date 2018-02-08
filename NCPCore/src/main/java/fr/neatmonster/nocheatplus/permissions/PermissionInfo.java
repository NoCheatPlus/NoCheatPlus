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
