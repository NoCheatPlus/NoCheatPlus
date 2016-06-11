package fr.neatmonster.nocheatplus.utilities.collision;

import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

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
            return true;
        }
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
