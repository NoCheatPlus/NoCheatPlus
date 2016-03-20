package fr.neatmonster.nocheatplus.compat.versions;

import org.bukkit.Bukkit;

/**
 * Feature selection, based on the version.
 * @author asofold
 *
 */
public class Bugs {

    private static boolean enforceLocation = false;

    private static boolean pvpKnockBackVelocity = false;

    protected static void init() {
        final String mcVersion = ServerVersion.getMinecraftVersion();
        final String serverVersion = Bukkit.getServer().getVersion().toLowerCase();

        // Need to add velocity (only internally) because the server does not.
        pvpKnockBackVelocity = ServerVersion.isMinecraftVersionBetween("1.8", true, "1.9", false);

        // First move exploit (classic CraftBukkit or Spigot before 1.7.5). 
        if (mcVersion == GenericVersion.UNKNOWN_VERSION) {
            // Assume something where it's not an issue.
            enforceLocation = false;
        }
        else if (GenericVersion.compareVersions(mcVersion, "1.8") >= 0) {
            // Assume Spigot + fixed.
            enforceLocation = false;
        } else if (serverVersion.indexOf("spigot") >= 0 && GenericVersion.compareVersions(mcVersion, "1.7.5") >= 0) {
            // Fixed in Spigot just before 1.7.5.
            enforceLocation = false;
        } else if (serverVersion.indexOf("craftbukkit") != 0){
            // Assume classic CraftBukkit (not fixed).
            enforceLocation = true;
        } else {
            // Assume something where it's not an issue.
            enforceLocation = false;
        }
    }

    public static boolean shouldEnforceLocation() {
        return enforceLocation;
    }

    public static boolean shouldPvpKnockBackVelocity() {
        return pvpKnockBackVelocity;
    }

}
