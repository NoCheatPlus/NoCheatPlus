package fr.neatmonster.nocheatplus.utilities.collision;

import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Test for collision testing for passing through blocks.
 * 
 * @author asofold
 *
 */
public interface ICollidePassable extends ICollide, ISetMargins {

    public void setBlockCache(BlockCache blockCache);
    public BlockCache getBlockCache();

    /**
     * Convenience: Call set and setBlockCache with the data from the
     * PlayerLocation instances. Should use from.getBlockCache() as BlockCache
     * instance.
     * 
     * @param from
     * @param to
     */
    public void set(final PlayerLocation from, final PlayerLocation to);

    /**
     * Indicate if extra workarounds may be necessary, such as running y-axis
     * collision before xz-axes collision in case the default run yields a
     * collision.
     * 
     * @return Return false in order to prevent workarounds with split by axis
     *         checking. Typically should return true with ray-tracing and false
     *         with axis-tracing.
     */
    public boolean mightNeedSplitAxisHandling();

    /**
     * Remove reference to objects passed from outside (BlockCache, but not
     * calling their cleanup methods).
     */
    public void cleanup();

}
