package cc.co.evenprime.bukkit.nocheat.checks.blockbreak;

import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * Playerspecific data for the blockbreak checks
 * 
 */
public class BlockBreakData implements DataItem {

    // Keep track of violation levels for the three checks
    public double               reachVL                    = 0.0D;
    public double               directionVL                = 0.0D;
    public double               noswingVL                  = 0.0D;

    // Used for the penalty time feature of the direction check
    public long                 directionLastViolationTime = 0;

    // Have a nicer/simpler way to work with block locations instead of
    // Bukkits own "Location" class
    public final SimpleLocation instaBrokenBlockLocation   = new SimpleLocation();
    public final SimpleLocation brokenBlockLocation        = new SimpleLocation();
    public final SimpleLocation lastDamagedBlock           = new SimpleLocation();

    // indicate if the player swung his arm since he got checked last time
    public boolean              armswung                   = true;

    // For logging, remember the reachDistance that was calculated in the 
    // reach check
    public double               reachDistance;

}
