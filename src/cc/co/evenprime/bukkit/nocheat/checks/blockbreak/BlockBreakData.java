package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import java.util.Map;

import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * Playerspecific data for the blockbreak check group
 * 
 */
public class BlockBreakData implements DataItem {

    public double                 reachVL                    = 0.0D;
    public double                 reachTotalVL               = 0.0D;
    public int                    reachFailed                = 0;
    public double                 directionVL                = 0.0D;
    public double                 directionTotalVL           = 0.0D;
    public int                    directionFailed            = 0;
    public double                 noswingVL                  = 0.0D;
    public double                 noswingTotalVL             = 0.0D;
    public int                    noswingFailed              = 0;

    public long                   directionLastViolationTime = 0;
    public final SimpleLocation   instaBrokenBlockLocation   = new SimpleLocation();
    public final SimpleLocation   brokenBlockLocation        = new SimpleLocation();

    public final ExecutionHistory history                    = new ExecutionHistory();

    public double                 reachDistance;
    public boolean                armswung                   = true;

    @Override
    public void clearCriticalData() {
        instaBrokenBlockLocation.reset();
        brokenBlockLocation.reset();
        directionLastViolationTime = 0;
        armswung = true;
    }

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("blockbreak.reach.vl", (int) reachTotalVL);
        map.put("blockbreak.direction.vl", (int) directionTotalVL);
        map.put("blockbreak.noswing.vl", (int) noswingTotalVL);
        map.put("blockbreak.reach.failed", reachFailed);
        map.put("blockbreak.direction.failed", directionFailed);
        map.put("blockbreak.noswing.failed", noswingFailed);
    }
}
