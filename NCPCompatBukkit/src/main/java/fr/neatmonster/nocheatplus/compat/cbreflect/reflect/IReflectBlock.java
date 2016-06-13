package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

/**
 * Reflection based block bounds fetching.
 * @author asofold
 *
 */
public interface IReflectBlock {

    /**
     * Static method: Get the block by id.
     * 
     * @param id
     * @return
     */
    public Object nms_getById(int id);

    /**
     * Get the material for a Block instance.
     * 
     * @param block
     * @return
     */
    public Object nms_getMaterial(Object block);

    /**
     * Fetch bounds for a block instance (minX, minY, minZ, maxX, maxY, maxZ).
     * 
     * @param nmsWorld
     * @param nmsBlock
     * @param x
     * @param y
     * @param z
     * @return
     */
    public double[] nms_fetchBounds(final Object nmsWorld, final Object nmsBlock,
            final int x, final int y, final int z);

    /**
     * Indicate if nms_fetchBounds could work.
     * 
     * @return
     */
    public boolean isFetchBoundsAvailable();

}
