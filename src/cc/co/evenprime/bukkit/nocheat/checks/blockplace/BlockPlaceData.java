package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import java.util.Map;
import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * 
 */
public class BlockPlaceData implements DataItem {

    public double                 reachVL                    = 0.0D;
    public double                 reachTotalVL               = 0.0D;
    public int                    reachFailed                = 0;
    public double                 directionVL                = 0.0D;
    public double                 directionTotalVL           = 0.0D;
    public int                    directionFailed            = 0;

    public long                   directionLastViolationTime = 0;

    public final SimpleLocation   blockPlacedAgainst         = new SimpleLocation();
    public final SimpleLocation   blockPlaced                = new SimpleLocation();
    public double                 reachdistance;

    public void clearCriticalData() {
        blockPlacedAgainst.reset();
        blockPlaced.reset();
        directionLastViolationTime = 0;
    }

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("blockplace.reach.vl", (int) reachTotalVL);
        map.put("blockplace.direction.vl", (int) directionTotalVL);
        map.put("blockplace.reach.failed", reachFailed);
        map.put("blockplace.direction.failed", directionFailed);
    }
}
