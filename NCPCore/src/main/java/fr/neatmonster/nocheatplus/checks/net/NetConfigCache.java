package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigCache;

/**
 * Copy-on-write per-world configuration cache.
 * @author web4web1
 *
 */
public class NetConfigCache extends WorldConfigCache<NetConfig> implements CheckConfigFactory {

    public NetConfigCache() {
        super(true);
    }

    @Override
    protected NetConfig newConfig(String key, ConfigFile configFile) {
        return new NetConfig(configFile);
    }

    @Override
    public NetConfig getConfig(final Player player) {
        return getConfig(player.getWorld());
    }

    @Override
    public void removeAllConfigs() {
        clearAllConfigs();
    }

}
