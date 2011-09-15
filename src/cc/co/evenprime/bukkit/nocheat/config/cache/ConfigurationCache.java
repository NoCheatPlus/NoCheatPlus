package cc.co.evenprime.bukkit.nocheat.config.cache;

import java.util.logging.Logger;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;

/**
 * A class to keep all configurables of the plugin associated with
 * a world, everything unmodifiable.
 * 
 * @author Evenprime
 * 
 */
public class ConfigurationCache {

    public final CCMoving     moving;
    public final CCLogging    logging;
    public final CCBlockBreak blockbreak;
    public final CCInteract   interact;
    public final CCBlockPlace blockplace;
    public final CCChat       chat;
    public final CCDebug      debug;

    /**
     * Instantiate a config cache and populate it with the data of a
     * Config tree (and its parent tree)
     * 
     * @param data
     */
    public ConfigurationCache(Configuration data, Logger worldSpecificFileLogger) {

        moving = new CCMoving(data);
        blockbreak = new CCBlockBreak(data);
        blockplace = new CCBlockPlace(data);
        interact = new CCInteract(data);
        chat = new CCChat(data);
        logging = new CCLogging(data, worldSpecificFileLogger);
        debug = new CCDebug(data);

    }
}
