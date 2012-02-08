package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

public class InventoryConfig implements ConfigItem {

    public final boolean    dropCheck;
    public final long       dropTimeFrame;
    public final int        dropLimit;
    public final ActionList dropActions;

    public final boolean    bowCheck;
    public final ActionList bowActions;

    public final boolean    eatCheck;
    public final ActionList eatActions;

    public InventoryConfig(NoCheatConfiguration data) {

        dropCheck = data.getBoolean(ConfPaths.INVENTORY_DROP_CHECK);
        dropTimeFrame = data.getInt(ConfPaths.INVENTORY_DROP_TIMEFRAME);
        dropLimit = data.getInt(ConfPaths.INVENTORY_DROP_LIMIT);
        dropActions = data.getActionList(ConfPaths.INVENTORY_DROP_ACTIONS);

        bowCheck = data.getBoolean(ConfPaths.INVENTORY_INSTANTBOW_CHECK);
        bowActions = data.getActionList(ConfPaths.INVENTORY_INSTANTBOW_ACTIONS);

        eatCheck = data.getBoolean(ConfPaths.INVENTORY_INSTANTEAT_CHECK);
        eatActions = data.getActionList(ConfPaths.INVENTORY_INSTANTEAT_ACTIONS);
    }
}
