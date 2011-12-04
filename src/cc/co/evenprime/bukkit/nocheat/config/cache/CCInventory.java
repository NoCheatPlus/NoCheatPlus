package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;


public class CCInventory {

    public final boolean    closebeforeteleports;

    public CCInventory(Configuration data) {

        closebeforeteleports = data.getBoolean(Configuration.INVENTORY_CLOSEOBEFORETELEPORTS);
    }
}
