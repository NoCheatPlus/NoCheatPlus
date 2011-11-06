package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Material;

/**
 * 
 */
public class BlockPlaceData extends Data {

    public double                 onliquidViolationLevel     = 0.0D;
    public double                 reachViolationLevel        = 0.0D;
    public final ExecutionHistory history                    = new ExecutionHistory();
    public double                 directionViolationLevel    = 0.0D;
    public long                   directionLastViolationTime = 0;

    public final SimpleLocation   blockPlacedAgainst         = new SimpleLocation();
    public final SimpleLocation   blockPlaced                = new SimpleLocation();
    public Material               placedType;
    public double                 reachdistance;

    public void clearCriticalData() {
        blockPlacedAgainst.reset();
        blockPlaced.reset();
        placedType = null;
    }
}
