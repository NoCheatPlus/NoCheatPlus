package fr.neatmonster.nocheatplus.net;

import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigCache;

/**
 * Copy-on-write per-world configuration cache.
 * @author web4web1
 *
 */
public class NetConfigCache extends WorldConfigCache<NetConfig> {

    public NetConfigCache() {
        super(true);
    }

    @Override
    protected NetConfig newConfig(String key, ConfigFile configFile) {
        return new NetConfig(configFile);
    }

}
