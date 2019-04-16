package fr.neatmonster.nocheatplus.permissions;

import com.google.common.base.Objects;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Cache for permission checks
 *
 * @author phase
 * @since 4/14/19
 */
public class PermissionCache {

    public static boolean INITIALIZED = false;
    private static Set<String> PERMISSION_GROUPS;
    private static Map<PermissionInfo, Boolean> PERMISSION_CACHE;

    public static void init() {
        INITIALIZED = true;
        PERMISSION_CACHE = new HashMap<>();
        List<String> permissionPolicies = ConfigManager.getConfigFile().getStringList("permission-policy");
        PERMISSION_GROUPS = new HashSet<>();
        PERMISSION_GROUPS.addAll(permissionPolicies);
    }

    public static void close() {
        INITIALIZED = false;
        PERMISSION_CACHE.clear();
    }

    public static boolean hasPermission(Player player, String permission) {
        for (String permissionGroup : PERMISSION_GROUPS) {
            if (permission.startsWith(permissionGroup)) {
                PermissionInfo info = new PermissionInfo(player.getUniqueId(), permissionGroup);
                return PERMISSION_CACHE.computeIfAbsent(info, (i) -> player.hasPermission(permission));
            }
        }
        return player.hasPermission(permission);
    }

    public static void clearPlayer(Player player) {
        Set<PermissionInfo> permissionInfosToClear = new HashSet<>();
        for (Map.Entry<PermissionInfo, Boolean> entry : PERMISSION_CACHE.entrySet()) {
            PermissionInfo info = entry.getKey();
            if (info.uuid.equals(player.getUniqueId())) {
                permissionInfosToClear.add(info);
            }
        }
        permissionInfosToClear.forEach(PERMISSION_CACHE::remove);
    }

    public static class PermissionInfo {
        UUID uuid;
        String permission;

        public PermissionInfo(UUID uuid, String permisison) {
            this.uuid = uuid;
            this.permission = permisison;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(uuid, permission);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PermissionInfo
                    && ((PermissionInfo) o).permission.equals(permission)
                    && ((PermissionInfo) o).uuid.equals(uuid);
        }
    }

}
