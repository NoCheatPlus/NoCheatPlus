package cc.co.evenprime.bukkit.nocheat;

import java.util.List;
import org.bukkit.event.Listener;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;

public interface EventManager extends Listener {

    public List<String> getActiveChecks(ConfigurationCacheStore cc);
}
