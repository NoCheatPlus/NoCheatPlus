package fr.neatmonster.nocheatplus.hooks.allviolations;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Configuration for the AllViolationsHook.
 * @author asofold
 *
 */
public class AllViolationsConfig {

    /** Log all players to console. */
    public final boolean allToTrace;

    /** Log all violations to the in-game notification channel. */
    public final boolean allToNotify;

    /** Log all violations for players/checks for which debug flags are set. */
    public final boolean debug;

    /** Only log if the player is being "debugged". Currently incomplete. */
    public final boolean debugOnly;

    // TODO: More config on what to print (uuid, etc., default tags). 
    // TODO: More filtering like in TestNCP.

    public AllViolationsConfig(ConfigFile config) {
        allToTrace = config.getBoolean(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_BACKEND_TRACE);
        allToNotify = config.getBoolean(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_BACKEND_NOTIFY);
        debug = config.getBoolean(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_DEBUG);
        debugOnly = config.getBoolean(ConfPaths.LOGGING_EXTENDED_ALLVIOLATIONS_DEBUGONLY) || debug && !allToTrace && !allToNotify;
    }

    public boolean doesLogAnything() {
        return allToTrace || allToNotify || debug;
    }

}
