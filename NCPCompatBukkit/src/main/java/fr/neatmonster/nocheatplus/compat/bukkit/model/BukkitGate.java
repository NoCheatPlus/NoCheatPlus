package fr.neatmonster.nocheatplus.compat.bukkit.model;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Openable;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public class BukkitGate implements BukkitShapeModel {

    private final double minXZ;
    private final double maxXZ;
    private final double height;

    public BukkitGate(double minXZ, double maxXZ, double height) {
        this.minXZ = minXZ;
        this.maxXZ = maxXZ;
        this.height = height;
    }

    @Override
    public double[] getShape(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        final Block block = world.getBlockAt(x, y, z);
        final BlockState state = block.getState();
        final BlockData blockData = state.getBlockData();
        if (blockData instanceof Directional) {
            switch (((Directional) blockData).getFacing()) {
                case WEST:
                    return new double[] {minXZ, 0.0, 0.0, maxXZ, height, 1.0};
                case EAST:
                    return new double[] {minXZ, 0.0, 0.0, maxXZ, height, 1.0};
                case NORTH:
                    return new double[] {0.0, 0.0, minXZ, 1.0, height, maxXZ};
                case SOUTH:
                    return new double[] {0.0, 0.0, minXZ, 1.0, height, maxXZ};
                default:
                    break;
            }
        }
        return new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0};
    }

    @Override
    public int getFakeData(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {
        final Block block = world.getBlockAt(x, y, z);
        final BlockState state = block.getState();
        final BlockData blockData = state.getBlockData();
        if (blockData instanceof Openable) {
            return ((Openable) blockData).isOpen() ? 0x4 : 0;
        }
        else {
            return 0;
        }
    }

}
