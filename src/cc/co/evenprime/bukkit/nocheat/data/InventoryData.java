package cc.co.evenprime.bukkit.nocheat.data;

import java.util.Map;

public class InventoryData extends Data {

    public double                 dropVL      = 0.0D;
    public double                 dropTotalVL = 0.0D;
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
