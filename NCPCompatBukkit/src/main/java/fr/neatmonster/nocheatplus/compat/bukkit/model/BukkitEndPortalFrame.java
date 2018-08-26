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
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.EndPortalFrame;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public class BukkitEndPortalFrame implements BukkitShapeModel {

    @Override
    public double[] getShape(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        final Block block = world.getBlockAt(x, y, z);
        final BlockState state = block.getState();
        final BlockData blockData = state.getBlockData();
        if (blockData instanceof EndPortalFrame) {
            return ((EndPortalFrame) blockData).hasEye()
                    ? new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0}
            : new double[] {0.0, 0.0, 0.0, 1.0, 0.8125, 1.0};
        }
        else {
            return new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0};
        }
    }

    @Override
    public int getFakeData(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        final Block block = world.getBlockAt(x, y, z);
        final BlockState state = block.getState();
        final BlockData blockData = state.getBlockData();
        if (blockData instanceof EndPortalFrame) {
            return ((EndPortalFrame) blockData).hasEye() ? 0x4 : 0;
        }
        else {
            return 0;
        }
    }

}
