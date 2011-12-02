package cc.co.evenprime.bukkit.nocheat.data;

import java.util.Map;

/**
 * 
 */
public class BlockPlaceData extends Data {

    public double                 reachVL                    = 0.0D;
    public double                 reachTotalVL               = 0.0D;
    public int                    reachFailed                = 0;
    public double                 directionVL                = 0.0D;
    public double                 directionTotalVL           = 0.0D;
    public int                    directionFailed            = 0;

    public final ExecutionHistory history                    = new ExecutionHistory();
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
