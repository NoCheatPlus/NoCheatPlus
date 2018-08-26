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
