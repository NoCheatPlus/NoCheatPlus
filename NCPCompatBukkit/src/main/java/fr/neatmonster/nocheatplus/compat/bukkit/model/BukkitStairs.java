package fr.neatmonster.nocheatplus.compat.bukkit.model;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public class BukkitStairs implements BukkitShapeModel {

    @Override
    public double[] getShape(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {

        // TODO: With fake data, this could simply return full bounds.

        //        final Block block = world.getBlockAt(x, y, z);
        //        final BlockState state = block.getState();
        //        final BlockData blockData = state.getBlockData();
        //
        //        if (blockData instanceof Stairs) {
        //            final Stairs stairs = (Stairs) blockData;
        //            final Half half = stairs.getHalf();
        //            //final Shape shape = stairs.getShape();
        //            // TODO: Refine later, with sub shapes.
        //            switch (half) {
        //                case BOTTOM:
        //                    return new double[] {0.0, 0.0, 0.0, 1.0, 0.5, 1.0};
        //                case TOP:
        //                    return new double[] {0.0, 0.5, 0.0, 1.0, 1.0, 1.0};
        //                default:
        //                    break;
        //
        //            }
        //        }
        return new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0};
    }

    @Override
    public int getFakeData(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        final Block block = world.getBlockAt(x, y, z);
        final BlockState state = block.getState();
        final BlockData blockData = state.getBlockData();

        if (blockData instanceof Bisected) {
            final Bisected stairs = (Bisected) blockData;
            final Half half = stairs.getHalf();
            //final Shape shape = stairs.getShape();
            // TODO: Refine later, with sub shapes.
            switch (half) {
                case TOP:
                    return 0x4;
                default:
                    break;
            }
        }
        return 0;
    }

}
