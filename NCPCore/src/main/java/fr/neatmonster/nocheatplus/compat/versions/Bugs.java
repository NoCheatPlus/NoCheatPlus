package fr.neatmonster.nocheatplus.compat.versions;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;

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
        final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        final List<String> noteWorthy = new LinkedList<String>();

        // Need to add velocity (only internally) because the server does not.
        pvpKnockBackVelocity = ServerVersion.isMinecraftVersionBetween("1.8", true, "1.9", false);
        if (pvpKnockBackVelocity) {
            noteWorthy.add("pvpKnockBackVelocity");
        }

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
        if (enforceLocation) {
            noteWorthy.add("enforceLocation");
        }
        if (!noteWorthy.isEmpty()) {
            api.addFeatureTags("defaults", noteWorthy); // Not sure how to name these.
            // Consider Bukkit.getLogger, or put to status after post-enable.
        }
    }

    public static boolean shouldEnforceLocation() {
        return enforceLocation;
    }

    public static boolean shouldPvpKnockBackVelocity() {
        return pvpKnockBackVelocity;
    }

}
