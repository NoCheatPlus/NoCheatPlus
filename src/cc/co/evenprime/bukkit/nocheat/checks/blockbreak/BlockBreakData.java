package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * Playerspecific data for the blockbreak check group
 * 
 */
public class BlockBreakData implements DataItem {

    public double                 reachVL                    = 0.0D;
    public double                 directionVL                = 0.0D;
    public double                 noswingVL                  = 0.0D;

    public long                   directionLastViolationTime = 0;
    public final SimpleLocation   instaBrokenBlockLocation   = new SimpleLocation();
    public final SimpleLocation   brokenBlockLocation        = new SimpleLocation();

    public double                 reachDistance;
    public boolean                armswung                   = true;
    public final SimpleLocation   lastDamagedBlock           = new SimpleLocation();
}
