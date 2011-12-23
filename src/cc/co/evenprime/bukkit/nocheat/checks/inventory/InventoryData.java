package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;

public class InventoryData implements DataItem {

    public int                    dropVL           = 0;
    public double                 dropTotalVL      = 0;
    public int                    dropFailed       = 0;
    public long                   dropLastTime;
    public int                    dropCount;
    public final ExecutionHistory history          = new ExecutionHistory();

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("inventory.drop.vl", (int) dropTotalVL);
        map.put("inventory.drop.failed", (int) dropFailed);
    }

    @Override
    public void clearCriticalData() {

    }
}
