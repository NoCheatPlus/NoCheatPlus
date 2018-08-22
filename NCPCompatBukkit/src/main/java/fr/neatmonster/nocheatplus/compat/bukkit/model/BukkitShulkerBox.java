package fr.neatmonster.nocheatplus.compat.bukkit.model;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public class BukkitShulkerBox implements BukkitShapeModel {

    @Override
    public double[] getShape(final BlockCache blockCache, 
            final World world, final int x, final int y, final int z) {

        final Block block = world.getBlockAt(x, y, z);
        final BlockState state = block.getState();
        //final BlockData blockData = state.getBlockData();

        if (state instanceof Container) {
            if (!((Container) state).getInventory().getViewers().isEmpty()) {
                return new double[] {0.0, 0.0, 0.0, 1.0, 1.5, 1.0};
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
