package fr.neatmonster.nocheatplus.compat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.compat.cbreflect.MCAccessCBReflect;
import fr.neatmonster.nocheatplus.compat.glowstone.MCAccessGlowstone;
import fr.neatmonster.nocheatplus.logging.StaticLog;

/**
 * Factory class to hide potentially dirty stuff.
 * @author mc_dev
 *
 */
public class MCAccessFactory {

    private final String[] updateLocs = new String[]{
            " Check for updates and support at BukkitDev: http://dev.bukkit.org/server-mods/nocheatplus/",
            " Development builds (unsupported by the Bukkit Staff, use at your own risk): http://ci.md-5.net/job/NoCheatPlus/changes",
    };

    /**
     * Get a new MCAccess instance.
     * @param bukkitOnly Set to true to force using an API-only module.
     * @return
     * @throws RuntimeException if no access can be set.
     */
    public MCAccess getMCAccess(final MCAccessConfig config) {
        final List<Throwable> throwables = new ArrayList<Throwable>();
        MCAccess mcAccess = null;
        // Try to set up native access.

        // CraftBukkit (dedicated).
        if (config.enableCBDedicated) {
            mcAccess = getMCAccessCraftBukkit(throwables);
            if (mcAccess != null) {
                return mcAccess;
            }
        }

        // CraftBukkit (reflection).
        if (config.enableCBReflect) {
            try {
                return new MCAccessCBReflect();
            }
            catch (Throwable t) {
                throwables.add(t);
            }
        }

        // Glowstone.
        try {
            return new MCAccessGlowstone();
        } catch(Throwable t) {
            throwables.add(t);
        };

        // Try to set up api-only access (since 1.4.6).
        try {
            mcAccess = new MCAccessBukkit();
            final String msg;
            msg = "Running in Bukkit-API-only mode (" + Bukkit.getServer().getVersion() + "). If this is not intended, please check for updates and consider to request support.";
            StaticLog.logWarning(msg);
            for (String uMsg : updateLocs) {
                StaticLog.logWarning(uMsg);
            }
            //            if (ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_EXTENDED_STATUS)) {
            //                log(throwables); // Maybe later activate with TRACE explicitly set
            //            }
            StaticLog.logWarning("Bukkit-API-only mode: Some features will likely not function properly, performance might suffer.");
            return mcAccess;
        }
        catch(Throwable t) {
            throwables.add(t);
        };

        // All went wrong.
        // TODO: Fall-back solution (disable plugin, disable checks).
        StaticLog.logSevere("Your version of NoCheatPlus is not compatible with the version of the server-mod (" + Bukkit.getServer().getVersion() + "). Please check for updates and consider to request support.");
        for (String msg : updateLocs) {
            StaticLog.logSevere(msg);
        }
        StaticLog.logSevere(">>> Failed to set up MCAccess <<<");
        log(throwables);
        // TODO: Schedule disabling the plugin or running in circles.
        throw new RuntimeException("Could not set up native access to the server mod, neither to the Bukkit-API.");
    }

    private static void log(Collection<Throwable> throwables) {
        for (Throwable t : throwables ) {
            StaticLog.logSevere(t);
        }
    }

    /**
     * Get MCaccess for CraftBukkit (dedicated only). Must not throw anything.
     * @param throwables
     * @return Valid MCAccess instance or null.
     */
    private MCAccess getMCAccessCraftBukkit(List<Throwable> throwables) {

        // TODO: Quick return check (note special forks and package info not being usable).

        final String[] classNames = new String[] {
                // Current DEV / LATEST: CB (Spigot)
                //"fr.neatmonster.nocheatplus.compat.cbdev.MCAccessCBDev", // future / tests.
                //"fr.neatmonster.nocheatplus.compat.cbreflect.MCAccessCBReflect", // TEST
                // Dedicated: CB (Spigot)
                "fr.neatmonster.nocheatplus.compat.spigotcb1_8_R3.MCAccessSpigotCB1_8_R3", // 1.8.4-1.8.8 (1_8_R3)
                "fr.neatmonster.nocheatplus.compat.spigotcb1_8_R2.MCAccessSpigotCB1_8_R2", // 1.8.3 (1_8_R2)
                "fr.neatmonster.nocheatplus.compat.spigotcb1_8_R1.MCAccessSpigotCB1_8_R1", // 1.8 (1_8_R1)
                // Dedicated CB (original)
                "fr.neatmonster.nocheatplus.compat.cb3100.MCAccessCB3100", // 1.7.10
                "fr.neatmonster.nocheatplus.compat.cb3043.MCAccessCB3043", // 1.7.8|1.7.9
                "fr.neatmonster.nocheatplus.compat.cb3026.MCAccessCB3026", // 1.7.5
                "fr.neatmonster.nocheatplus.compat.cb2922.MCAccessCB2922", // 1.7.2
                "fr.neatmonster.nocheatplus.compat.cb2882.MCAccessCB2882", // 1.6.4
                "fr.neatmonster.nocheatplus.compat.cb2808.MCAccessCB2808", // 1.6.2
                "fr.neatmonster.nocheatplus.compat.cb2794.MCAccessCB2794", // 1.6.1
                "fr.neatmonster.nocheatplus.compat.cb2763.MCAccessCB2763", // 1.5.2
                "fr.neatmonster.nocheatplus.compat.cb2691.MCAccessCB2691", // 1.5.1
                "fr.neatmonster.nocheatplus.compat.cb2645.MCAccessCB2645", // 1.5
                "fr.neatmonster.nocheatplus.compat.cb2602.MCAccessCB2602", // 1.4.7
                "fr.neatmonster.nocheatplus.compat.cb2545.MCAccessCB2545", // 1.4.6
                "fr.neatmonster.nocheatplus.compat.cb2512.MCAccessCB2512", // 1.4.5-R1.0
        };

        for (String className : classNames) {
            try{
                return (MCAccess) Class.forName(className).newInstance();
            }
            catch(Throwable t) {
                throwables.add(t);
            };
        }

        // None worked.
        return null;
    }

}
