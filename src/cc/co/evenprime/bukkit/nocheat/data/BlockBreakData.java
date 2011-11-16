package cc.co.evenprime.bukkit.nocheat.data;

/**
 * Playerspecific data for the blockbreak check group
 * 
 */
public class BlockBreakData extends Data {

    public double                 reachVL                    = 0.0D;
    public double                 directionVL                = 0.0D;

    public long                   directionLastViolationTime = 0;
    public final SimpleLocation   instaBrokenBlockLocation   = new SimpleLocation();
    public final SimpleLocation   brokenBlockLocation        = new SimpleLocation();

    public final ExecutionHistory history                    = new ExecutionHistory();
    public double                 noswingVL                  = 0.0D;
    public double                 reachDistance;
    public boolean                armswung                   = true;
}
