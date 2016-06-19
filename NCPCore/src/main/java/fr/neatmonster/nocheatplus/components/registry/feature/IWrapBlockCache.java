package fr.neatmonster.nocheatplus.components.registry.feature;

import fr.neatmonster.nocheatplus.components.registry.event.IHandle;
import fr.neatmonster.nocheatplus.utilities.BlockCache;

/**
 * Wrap a BlockCache instance, update with changes of underlying
 * BlockCache/MCAccess implementations or other providers, store the same
 * instance as long as possible. It can not be guaranteed that the returned
 * instances are the same for two subsequent calls, references to overridden
 * MCAccess or BlockCache might be stored until the next call to getBlockCache
 * or getHandle().
 * 
 * @author asofold
 *
 */
public interface IWrapBlockCache extends IGetBlockCache, IHandle<BlockCache> {

    /** Fail-safe convenience call for BlockCache.cleanup. */
    public void cleanup();

}
