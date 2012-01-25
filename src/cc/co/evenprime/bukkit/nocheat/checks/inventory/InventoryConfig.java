package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;

public class InventoryConfig implements ConfigItem {

    public final boolean    check;

    public final boolean    dropCheck;
    public final long       dropTimeFrame;
    public final int        dropLimit;
    public final ActionList dropActions;

    public InventoryConfig(Configuration data) {

        check = data.getBoolean(Configuration.INVENTORY_CHECK);
        dropCheck = data.getBoolean(Configuration.INVENTORY_DROP_CHECK);
        dropTimeFrame = data.getInteger(Configuration.INVENTORY_DROP_TIMEFRAME);
        dropLimit = data.getInteger(Configuration.INVENTORY_DROP_LIMIT);
        dropActions = data.getActionList(Configuration.INVENTORY_DROP_ACTIONS);
    }
}
