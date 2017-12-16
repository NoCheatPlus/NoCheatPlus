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
package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import org.bukkit.Material;

/**
 * Reflection based block bounds fetching.
 * @author asofold
 *
 */
public interface IReflectBlock {

    /**
     * Static method: Get the block by id.
     * 
     * @param id
     * @return
     */
    public Object nms_getByMaterial(Material id);

    /**
     * Get the material for a Block instance.
     * 
     * @param block
     * @return
     */
    public Object nms_getMaterial(Object block);

    /**
     * Fetch bounds for a block instance (minX, minY, minZ, maxX, maxY, maxZ).
     * 
     * @param nmsWorld
     * @param nmsBlock
     * @param x
     * @param y
     * @param z
     * @return
     */
    public double[] nms_fetchBounds(final Object nmsWorld, final Object nmsBlock,
            final int x, final int y, final int z);

    /**
     * Indicate if nms_fetchBounds could work.
     * 
     * @return
     */
    public boolean isFetchBoundsAvailable();

}
