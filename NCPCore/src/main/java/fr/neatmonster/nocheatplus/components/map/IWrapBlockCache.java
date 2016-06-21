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
package fr.neatmonster.nocheatplus.components.map;

import fr.neatmonster.nocheatplus.components.registry.event.IHandle;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

/**
 * Wrap a BlockCache instance, update with changes of underlying
 * BlockCache/MCAccess implementations or other providers, store the same
 * instance as long as possible. It can not be guaranteed that the returned
 * instances are the same for two subsequent calls, references to overridden
 * MCAccess or BlockCache might be stored until the next call to getBlockCache
 * or getHandle().
 * 
 * @author asofold
 *
 */
public interface IWrapBlockCache extends IGetBlockCache, IHandle<BlockCache> {

    /** Fail-safe convenience call for BlockCache.cleanup. */
    public void cleanup();

}
