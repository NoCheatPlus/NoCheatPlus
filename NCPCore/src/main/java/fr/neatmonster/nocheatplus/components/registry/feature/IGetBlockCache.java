package fr.neatmonster.nocheatplus.components.registry.feature;

import fr.neatmonster.nocheatplus.utilities.BlockCache;

/**
 * Get a BlockCache instance. This may or may not be a new instance, to be
 * specified by the implementing class.
 * 
 * @author asofold
 *
 */
public interface IGetBlockCache {

    /**
     * Retrieve a BlockCache instance. If this is always the same one, depends
     * on the implementation.
     * 
     * @return
     */
    public BlockCache getBlockCache();

}
