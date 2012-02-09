package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * 
 */
public class BlockPlaceData implements DataItem {

    public double               reachVL                    = 0.0D;
    public double               directionVL                = 0.0D;

    public long                 directionLastViolationTime = 0;

    public final SimpleLocation blockPlacedAgainst         = new SimpleLocation();
    public final SimpleLocation blockPlaced                = new SimpleLocation();
    public double               reachdistance;
}
