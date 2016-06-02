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
