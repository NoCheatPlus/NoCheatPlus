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
package fr.neatmonster.nocheatplus.components.location;

import fr.neatmonster.nocheatplus.utilities.ds.map.CoordHash;

/**
 * Simple immutable block position. Both hashCode and equals are implemented,
 * with equals accepting any IGetBlockPosition instance for comparison of block
 * coordinates.
 * 
 * @author asofold
 *
 */
public class BlockPositionGet implements IGetBlockPosition {

    private final int x;
    private final int y;
    private final int z;

    public BlockPositionGet(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getBlockX() {
        return x;
    }

    @Override
    public int getBlockY() {
        return y;
    }

    @Override
    public int getBlockZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return CoordHash.hashCode3DPrimes(x, y, z);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof IGetBlockPosition) {
            final IGetBlockPosition other = (IGetBlockPosition) obj;
            return x == other.getBlockX() && y == other.getBlockY() && z == other.getBlockZ();
        }
        return false;
    }

}
