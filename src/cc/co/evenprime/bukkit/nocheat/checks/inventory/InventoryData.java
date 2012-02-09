package cc.co.evenprime.bukkit.nocheat.checks.inventory;

import org.bukkit.Material;
import cc.co.evenprime.bukkit.nocheat.DataItem;

public class InventoryData implements DataItem {

    public int      dropVL;
    public long     dropLastTime;
    public int      dropCount;

    public int      instantBowVL;
    public long     lastBowInteractTime;

    public double   instantEatVL;
    public long     lastEatInteractTime;
    public Material foodMaterial;
}
