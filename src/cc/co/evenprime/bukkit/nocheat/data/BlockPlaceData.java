package cc.co.evenprime.bukkit.nocheat.data;

/**
 * 
 */
public class BlockPlaceData extends Data {

    public double                 reachVL                    = 0.0D;
    public final ExecutionHistory history                    = new ExecutionHistory();
    public double                 directionVL                = 0.0D;
    public long                   directionLastViolationTime = 0;

    public final SimpleLocation   blockPlacedAgainst         = new SimpleLocation();
    public final SimpleLocation   blockPlaced                = new SimpleLocation();
    public double                 reachdistance;

    public void clearCriticalData() {
        blockPlacedAgainst.reset();
        blockPlaced.reset();
        directionLastViolationTime = 0;
    }
}
