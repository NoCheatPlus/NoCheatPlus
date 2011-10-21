package cc.co.evenprime.bukkit.nocheat.data;

public class FightData extends Data {

    public double                 violationLevel             = 0;
    public long                   directionLastViolationTime = 0;
    public final ExecutionHistory history                    = new ExecutionHistory();

}
