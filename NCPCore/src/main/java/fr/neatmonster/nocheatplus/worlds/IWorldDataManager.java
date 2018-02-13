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
package fr.neatmonster.nocheatplus.worlds;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;

public interface IWorldDataManager {

    /**
     * Get the default world data, which applies for all worlds for which no
     * specific raw configuration exists.
     * <hr/>
     * Thread-safe.
     * <hr/>
     * 
     * @return
     */
    public IWorldData getDefaultWorldData();

    /**
     * Get the world data for a specific world name (case insensitive!). Creates
     * a new (inherited from default) WorlData instance, if none is set yet.
     * <hr/>
     * Thread-safe.
     * <hr/>
     * 
     * @param worldName
     * @return
     */
    public IWorldData getWorldData(String worldName);

    /**
     * Bukkit specific convenience method. Further see:
     * {@link #getWorldData(String)}
     * <hr/>
     * The touched plugin internals are thread-safe, the thread-safety depends
     * on the Bukkit world methods getName() and getUID(). If the world instance
     * is present, this likely is no problem - in order to be sure to fetch the
     * appropriate (existing) world data for proxy Player instances that don't
     * support worlds, use
     * PlayerData.getWorldIdentifier().getLowerCaseWorldName().
     * <hr/>
     * 
     * @param newWorld
     * @return
     */
    public IWorldData getWorldData(World world);

    /**
     * Thread-safe read-only iterator, see:
     * {@link fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW#iterator()}
     * 
     * @return
     */
    public Iterator<Entry<String, IWorldData>> getWorldDataIterator();

    /**
     * Thread-safe Iterable, see:
     * {@link fr.neatmonster.nocheatplus.utilities.ds.map.HashMapLOW#iterator()}
     * 
     * @return
     */
    public Iterable<Entry<String, IWorldData>> getWorldDataIterable();

    /**
     * Update with the underlying raw configurations as reference (stored
     * instances).
     * <hr/>
     * Thread-safety uncertain: better regard as registry functionality, thus
     * primary thread only.
     * <hr/>
     */
    public void updateAllWorldData();

    /**
     * Override check activation for all stored configurations. It's not
     * necessary to call updateAllWorldData afterwards.
     * <hr/>
     * Assume to be typical registry functionality, thus primary thread only.
     * <hr/>
     * 
     * @param checkType
     * @param active
     *            MAYBE means that the super type is checked for activation.
     * @param overrideType
     *            Set to CUSTOM or PERMANENT to prevent any resetting with
     *            reloading the configuration. Use VOLATILE to ensure it's reset
     *            the next time an update is done on that node.
     * @param overrideChildren
     *            If set to true, all children will be attempted to be
     *            overridden too. If set to false, children will just be
     *            updated.
     * @throws IllegalArgumentException
     *             If the configuration path for the activation flag of the
     *             given check type is not set explicitly.
     */
    public void overrideCheckActivation(CheckType type, AlmostBoolean active,
            OverrideType overrideType, boolean overrideChildren);

    /**
     * Test if the check is activated in any stored world data.
     * <hr>
     * <b>TBD: This includes data for already unloaded worlds.</b>
     * <hr/>
     * Thread-safe.
     * <hr/>
     * 
     * @param checkType
     * @return
     */
    public boolean isActiveAnywhere(CheckType checkType);

    /**
     * Some implementations throw an UnsupportedOperationException for
     * {@link org.bukkit.entity.Player#getWorld()}. - this does not check for
     * IWorldData instances already stored within PlayerData.
     * 
     * @param player
     * @return
     */
    public IWorldData getWorldDataSafe(Player player);

}
