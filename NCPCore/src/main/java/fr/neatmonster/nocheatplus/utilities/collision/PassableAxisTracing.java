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
package fr.neatmonster.nocheatplus.utilities.collision;

import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

public class PassableAxisTracing extends AxisTracing implements ICollidePassable {

    private BlockCache blockCache;

    // TODO: Might need another option for margins (option to skip margin for the axis-start point, or alter ignoreFirst behavior).
    // TODO: Consider an iteration margin as well (0.5 below for fences).

    public BlockCache getBlockCache() {
        return blockCache;
    }

    public void setBlockCache(BlockCache blockCache) {
        this.blockCache = blockCache;
    }

    @Override
    protected void collectInitiallyCollidingBlocks(double minX, double minY, double minZ, double maxX, double maxY,
            double maxZ, BlockPositionContainer results) {
        BlockProperties.collectInitiallyCollidingBlocks(blockCache, minX, minY, minZ, maxX, maxY, maxZ, results);
    }

    @Override
    protected boolean step(final int blockX, final int blockY, final int blockZ, 
            final double minX, final double minY, final double minZ, 
            final double maxX, final double maxY, final double maxZ, 
            final Axis axis, final int increment) {
        if (BlockProperties.isPassableBox(blockCache, blockX, blockY, blockZ, minX, minY, minZ, maxX, maxY, maxZ)) {
            /*
             * TODO: HEIGHT150 -> if not passable... how/where to test for block
             * change tracker? E.g.: y-offset from block < 0.5 -> check the
             * block underneath. (one method check box normal + opportunistic
             * past state handling in one?)
             */
            return true;
        }
        // TODO: if (blockChangeTracker != null && -- check with BlockChangeTracker and BlockChangeReference --
        else {
            collides = true;
            return false;
        }
    }

    @Override
    public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
        super.set(x0, y0, z0, x1, y1, z1);
    }

    @Override
    public void set(PlayerLocation from, PlayerLocation to) {
        set(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
        setBlockCache(from.getBlockCache());
    }

    @Override
    public boolean mightNeedSplitAxisHandling() {
        return false;
    }

    @Override
    public void cleanup() {
        blockCache = null;
    }

}
