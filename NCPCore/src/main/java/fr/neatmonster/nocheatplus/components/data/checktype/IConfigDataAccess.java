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
package fr.neatmonster.nocheatplus.components.data.checktype;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Data node representing an already processed state above a raw configuration.
 * Contained are per check type nodes containing further information, at least
 * activation and debug flags. Methods for overriding flags are also contained.
 * This can be per-world basic configuration, but it might also be the common
 * super interface for both worlds and players at some point.
 * <hr/>
 * Subject to change.
 * 
 * @author asofold
 *
 */
public interface IConfigDataAccess extends IBaseDataAccess {
    // TODO: Naming.

    /**
     * Get the applicable raw configuration file.
     * 
     * @return
     */
    public ConfigFile getRawConfiguration();

    /**
     * Test if a check is activated by configuration (respecting overrides).
     * <hr/>
     * Thread-safe.
     * <hr/>
     * 
     * @param checkType
     * @return
     */
    public boolean isCheckActive(CheckType checkType);

    /**
     * Override the activation of a check (/group, including all descendant
     * checks). This does not change the underlying raw configuration, and may
     * be reset with reloading the configuration, due to automatic overrides
     * which get applied after reloading the raw configuration, independently of
     * what is set in there - depends on the argument overrideType.
     * <hr/>
     * TODO: With overrideType CUSTOM this is persistent over reloading the
     * configuration.
     * <hr/>
     * <h3>IWorldData</h3> In order to reliably override check activation for
     * all stored WorldData instances, use
     * {@link fr.neatmonster.nocheatplus.worlds.WorldDataManager#overrideCheckActivation(CheckType, AlmostBoolean, boolean)}
     * <hr/>
     * Assume this to be registry functionality, thus primary-thread only.
     * <hr/>
     * 
     * @param checkType
     * @param active
     *            The desired activation state. MAYBE means to use the
     *            activation state of the super type.
     * @param overrideType
     *            The priority/type for overriding. Only values with same or
     *            lower priority can be overridden.
     * @param overrideChildren
     *            Explicitly attempt to override child nodes too, if set to
     *            true. Otherwise child nodes are just updated to the current
     *            state.
     * @TODO Registration priorities and tags to relate to, for custom
     *       overrides.
     */
    // TODO: IWorldData - move to IWorldData ?
    public void overrideCheckActivation(CheckType checkType, AlmostBoolean active,
            OverrideType overrideType, boolean overrideChildren);

    // TODO: resetCheckActivation -> reset to config value, except if set to PERMANENT.


    /**
     * Thread-safe.
     * 
     * @param checkType
     * @return
     */
    public IConfigCheckNode getCheckNode(CheckType checkType);


}
