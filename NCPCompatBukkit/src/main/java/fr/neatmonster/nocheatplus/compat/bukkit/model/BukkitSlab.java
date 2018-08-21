package fr.neatmonster.nocheatplus.compat.bukkit.model;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public class BukkitSlab implements BukkitShapeModel {

    @Override
    public double[] getShape(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        final Block block = world.getBlockAt(x, y, z);
        final BlockState state = block.getState();
        final BlockData blockData = state.getBlockData();

        if (blockData instanceof Slab) {
            final Slab slab = (Slab) blockData;
            switch (slab.getType()) {
                case BOTTOM:
                    return new double[] {0.0, 0.0, 0.0, 1.0, 0.5, 1.0};
                case TOP:
                    return new double[] {0.0, 0.5, 0.0, 1.0, 1.0, 1.0};
                case DOUBLE:
                    break;
                default:
                    break;
            }
        }
        return new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0};
    }

    @Override
    public int getFakeData(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        return 0;
    }

}
