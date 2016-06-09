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
package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectEntityLastPositionAndLook;
import fr.neatmonster.nocheatplus.components.location.IEntityAccessLastPositionAndLook;
import fr.neatmonster.nocheatplus.logging.Streams;

/**
 * Set up more fine grained entity access providers, registered as generic
 * instances for interfaces for now. Namely:
 * <ul>
 * <li>IEntityAccessPositionAndLook</li>
 * </ul>
 * 
 * @author asofold
 *
 */
public class EntityAccessFactory {

    /**
     * Set up alongside with MCAccess. This is called before setMCAccess is used
     * internally, so the MCAccess instance is passed here.
     * 
     * @param mcAccess
     * @param config
     */
    public void setupEntityAccess(final MCAccess mcAccess, final MCAccessConfig config) {
        setupLastPositionWithLook();
    }

    private void setupLastPositionWithLook() {
        IEntityAccessLastPositionAndLook res = null;
        // Reference by class name (native access).
        final String[] names = new String[] {
                "fr.neatmonster.nocheatplus.compat.cbdev.EntityAccessLastPositionAndLook",
                "fr.neatmonster.nocheatplus.compat.spigotcb1_9_R2.EntityAccessLastPositionAndLook",
                "fr.neatmonster.nocheatplus.compat.spigotcb1_9_R1.EntityAccessLastPositionAndLook"
        };
        for (final String name : names) {
            try {
                res = (IEntityAccessLastPositionAndLook) Class.forName(name).newInstance();
                if (res != null) {
                    break;
                }
            }
            catch (Throwable t) {
                // Skip.
            }
        }
        // Reflection based.
        if (res == null) {
            try {
                res = new ReflectEntityLastPositionAndLook();
            }
            catch (Throwable t) {
                // Ignore.
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, t);
            }
        }
        // Register / log.
        register(IEntityAccessLastPositionAndLook.class, res);
    }

    private <T> void register(Class<T> registerFor, T result) {
        if (result != null) {
            NCPAPIProvider.getNoCheatPlusAPI().registerGenericInstance(registerFor, result);
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Registered for " + registerFor.getName() + ": " + result.getClass().getName());
        }
        else {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Could not register an instance for: " + registerFor.getName());
        }
    }

}
