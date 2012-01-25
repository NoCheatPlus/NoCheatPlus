package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;

public abstract class InventoryCheck extends Check {

    private static final String id = "inventory";

    public InventoryCheck(NoCheat plugin, String name, String permission) {
        super(plugin, id, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, InventoryData data, CCInventory cc);

    public abstract boolean isEnabled(CCInventory cc);

    public static InventoryData getData(DataStore base) {
        InventoryData data = base.get(id);
        if(data == null) {
            data = new InventoryData();
            base.set(id, data);
        }
        return data;
    }

    public static CCInventory getConfig(ConfigurationCacheStore cache) {
        CCInventory config = cache.get(id);
        if(config == null) {
            config = new CCInventory(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }
}
