package cc.co.evenprime.bukkit.nocheat.data;

/**
 * Playerspecific data for the blockbreak check group
 * 
 */
public class BlockBreakData extends Data {

    public double                 reachViolationLevel        = 0.0D;
    public double                 directionViolationLevel    = 0.0D;

    public long                   directionLastViolationTime = 0;
    public final SimpleLocation   instaBrokeBlockLocation    = new SimpleLocation();

    public final ExecutionHistory history                    = new ExecutionHistory();
    public double                 noswingVL                  = 0.0D;

}
