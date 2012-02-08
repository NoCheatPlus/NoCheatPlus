package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import java.util.Map;
import org.bukkit.Material;
import cc.co.evenprime.bukkit.nocheat.DataItem;

public class InventoryData implements DataItem {

    public int      dropVL;
    public double   dropTotalVL;
    public int      dropFailed;
    public long     dropLastTime;
    public int      dropCount;

    public int      instantBowVL;
    public int      instantBowTotalVL;
    public int      instantBowFailed;

    public double   instantEatVL;
    public int      instantEatTotalVL;
    public int      instantEatFailed;

    public long     lastBowInteractTime;
    public int      lastEatInteractTime;
    public Material foodMaterial;
    public long     lastFoodInteractTime;
    public int      newFoodLevel;

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("inventory.drop.vl", (int) dropTotalVL);
        map.put("inventory.drop.failed", (int) dropFailed);

        map.put("inventory.instantbow.vl", (int) instantBowTotalVL);
        map.put("inventory.instantbow.failed", (int) instantBowFailed);

        map.put("inventory.instanteat.vl", (int) instantEatTotalVL);
        map.put("inventory.instanteat.failed", (int) instantEatFailed);
    }
}
