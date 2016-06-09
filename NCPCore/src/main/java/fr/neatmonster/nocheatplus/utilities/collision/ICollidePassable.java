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

    // TODO: IRayTracing

    public void setBlockCache(BlockCache blockCache);
    public BlockCache getBlockCache();

    /**
     * Set from PlayerLocation instances. May use the BlockCache from the
     * from-location internally.
     * 
     * @param from
     * @param to
     */
    public void set(final PlayerLocation from, final PlayerLocation to);

    /**
     * Ignore the first block. Must be called after set, because set should
     * override internal state with false.
     */
    // TODO: Switch to ignoreBlock(int, int, int) rather.
    public void setIgnoreFirst();

    /**
     * Test if the first block is set to be ignored (resets to false with set).
     * 
     * @return
     */
    public boolean getIgnoreFirst();

    /**
     * 
     * @return Return false in order to prevent workarounds with split by axis
     *         checking.
     */
    public boolean mightNeedSplitAxisHandling();

    /**
     * Remove reference to objects passed from outside (BlockCache, but not
     * calling their cleanup methods).
     */
    public void cleanup();

}
