/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.utilities.map;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.map.IWrapBlockCache;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;

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
        this(NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(MCAccess.class));
    }

    public WrapBlockCache(IGenericInstanceHandle<MCAccess> mcAccess) {
        this.mcAccess = mcAccess;
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
