package cc.co.evenprime.bukkit.nocheat.data;

/**
 * 
 */
public class BlockPlaceData extends Data {

    public double                 onliquidViolationLevel     = 0.0D;
    public double                 reachViolationLevel        = 0.0D;
    public final ExecutionHistory history                    = new ExecutionHistory();
    public double                 noswingVL                  = 0.0D;
    public double                 directionViolationLevel    = 0.0D;
    public long                   directionLastViolationTime = 0;

    public final SimpleLocation   blockPlacedAgainst         = new SimpleLocation();
    public final SimpleLocation   blockPlaced                = new SimpleLocation();
}
