package cc.co.evenprime.bukkit.nocheat;

import java.util.List;

import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;

public interface EventManager {

    public List<String> getActiveChecks(ConfigurationCacheStore cc);
}
