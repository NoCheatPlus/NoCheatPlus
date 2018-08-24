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
package fr.neatmonster.nocheatplus.compat.bukkit.model;

import org.bukkit.World;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

/**
 * Bottom center cuboid.
 * 
 * @author asofold
 *
 */
public class BukkitBottomCentered implements BukkitShapeModel {

    private final double minXZ;
    private final double maxXZ;
    private final double height;

    // TODO: Add modifications (shape alteration interface).

    public BukkitBottomCentered(double inset, double height) {
        this(inset, 1.0 - inset, height);
    }

    public BukkitBottomCentered(double minXZ, double maxXZ, double height) {
        this.minXZ = minXZ;
        this.maxXZ = maxXZ;
        this.height = height;
    }

    @Override
    public double[] getShape(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        return new double[] {minXZ, 0.0, minXZ, maxXZ, height, maxXZ};
    }

    @Override
    public int getFakeData(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        return 0;
    }

}
