package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.DefaultConfiguration;


public class CCDebug {

    public final boolean showchecks;
    

    public CCDebug(Configuration data) {

        showchecks = data.getBoolean(DefaultConfiguration.DEBUG_SHOWACTIVECHECKS);
    }
}
