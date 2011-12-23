package cc.co.evenprime.bukkit.nocheat.data;

import java.util.Map;

public class InventoryData extends Data {

    public int                    dropVL      = 0;
    public double                 dropTotalVL = 0;
    public int                    dropFailed  = 0;
    public long                   dropLastTime;
    public int                    dropCount;
    public final ExecutionHistory history     = new ExecutionHistory();

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("inventory.drop.vl", (int) dropTotalVL);
        map.put("inventory.drop.failed", (int) dropFailed);
    }
}
