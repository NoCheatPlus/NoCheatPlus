package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

public class InventoryConfig implements ConfigItem {

    public final boolean    check;

    public final boolean    dropCheck;
    public final long       dropTimeFrame;
    public final int        dropLimit;
    public final ActionList dropActions;

    public InventoryConfig(NoCheatConfiguration data) {

        dropCheck = data.getBoolean(ConfPaths.INVENTORY_DROP_CHECK);
        dropTimeFrame = data.getInt(ConfPaths.INVENTORY_DROP_TIMEFRAME);
        dropLimit = data.getInt(ConfPaths.INVENTORY_DROP_LIMIT);
        dropActions = data.getActionList(ConfPaths.INVENTORY_DROP_ACTIONS);

        check = dropCheck;
    }
}
