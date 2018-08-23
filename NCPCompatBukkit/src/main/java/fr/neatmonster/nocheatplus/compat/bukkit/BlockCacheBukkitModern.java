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
package fr.neatmonster.nocheatplus.compat.bukkit;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;

import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitShapeModel;


/**
 * BlockCache for MCAccessBukkitModern.
 * 
 * @author asofold
 *
 */
public class BlockCacheBukkitModern extends BlockCacheBukkit {

    private Map<Material, BukkitShapeModel> shapeModels;

    public BlockCacheBukkitModern(Map<Material, BukkitShapeModel> shapeModels) {
        super(null);
        this.shapeModels = shapeModels;
    }

    public BlockCacheBukkitModern(World world) {
        super(world);
    }

    @Override
    public int fetchData(int x, int y, int z) {
        Material mat = getType(x, y, z);

        final BukkitShapeModel shapeModel = shapeModels.get(mat);
        if (shapeModel != null) {
            final int data = shapeModel.getFakeData(this, world, x, y, z);
            if (data != Integer.MAX_VALUE) {
                return data;
            }
        }
        return super.fetchData(x, y, z);
    }

    @Override
    public double[] fetchBounds(int x, int y, int z) {
        // minX, minY, minZ, maxX, maxY, maxZ

        // TODO: Fetch what's possible to fetch/guess (...).

        // TODO: Consider to store the last used block/stuff within BlockCacheBukkit already.
        //final Block block = world.getBlockAt(x, y, z);
        //final BlockState state = block.getState();
        //final MaterialData materialData = state.getData();
        //final BlockData blockData = state.getBlockData();
        Material mat = getType(x, y, z);

        final BukkitShapeModel shapeModel = shapeModels.get(mat);
        if (shapeModel == null) {
            return super.fetchBounds(x, y, z);
        }
        else {
            return shapeModel.getShape(this, world, x, y, z);
        }

    }
    
    // TODO: Might refine standsOnEntity as well.

}
