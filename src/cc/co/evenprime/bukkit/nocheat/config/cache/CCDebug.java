package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;

public class CCDebug {

    public final boolean showchecks;
    public final boolean overrideIdiocy;

    public CCDebug(Configuration data) {

        showchecks = data.getBoolean(Configuration.DEBUG_SHOWACTIVECHECKS);
        overrideIdiocy = data.getBoolean(Configuration.DEBUG_COMPATIBILITY);
    }
}
