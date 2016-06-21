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

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

/**
 * Get a BlockCache instance. This may or may not be a new instance, to be
 * specified by the implementing class.
 * 
 * @author asofold
 *
 */
public interface IGetBlockCache {

    /**
     * Retrieve a BlockCache instance. If this is always the same one, depends
     * on the implementation.
     * 
     * @return
     */
    public BlockCache getBlockCache();

}
