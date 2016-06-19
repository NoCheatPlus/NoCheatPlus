package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.components.registry.feature.IWrapBlockCache;

/**
 * Wrap a BlockCache instance, and ensure on getting, that it is the latest
 * registered implementation. Default implementation returned with the
 * NoCheatPlusAPI. This might keep references to previously used
 * implementations, which could leak memory.
 * 
 * @author asofold
 *
 */
public class WrapBlockCache implements IWrapBlockCache {

    private final IGenericInstanceHandle<MCAccess> mcAccess;
    private MCAccess lastMCAccess;

    private BlockCache blockCache;

    public WrapBlockCache() {
        mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(MCAccess.class);
        lastMCAccess = mcAccess == null ? null : mcAccess.getHandle();
        blockCache = mcAccess == null ? null : lastMCAccess.getBlockCache();
    }

    private BlockCache getInstance() {
        if (lastMCAccess == mcAccess.getHandle()) {
            return blockCache;
        }
        else {
            lastMCAccess = mcAccess.getHandle();
            // TODO: This would make an initialized block cache uninitialized.
            blockCache = lastMCAccess.getBlockCache();
            return blockCache;
        }
    }

    @Override
    public BlockCache getBlockCache() {
        return getInstance();
    }

    @Override
    public BlockCache getHandle() {
        return getInstance();
    }

    @Override
    public void cleanup() {
        if (blockCache != null) {
            blockCache.cleanup();
        }
    }

}
