package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;

/**
 * Playerspecific data for the blockbreak check group
 * 
 */
public class BlockBreakData extends Data {

    public double                 reachViolationLevel        = 0.0D;
    public double                 directionViolationLevel    = 0.0D;

    public long                   directionLastViolationTime = 0;
    public Location               instaBrokeBlockLocation    = null;
    public final ExecutionHistory history                    = new ExecutionHistory();

}
