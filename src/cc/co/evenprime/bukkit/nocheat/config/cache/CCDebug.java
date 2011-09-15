package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;


public class CCDebug {

    public final boolean showchecks;
    

    public CCDebug(Configuration data) {

        showchecks = data.getBoolean("debug.showactivechecks");
    }
}
