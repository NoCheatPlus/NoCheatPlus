package cc.co.evenprime.bukkit.nocheat.data;

public class TimedData extends Data {

    public int                    ticksLived;
    public int                    ticksBehind;
    public double                 godmodeVL;
    public final ExecutionHistory history         = new ExecutionHistory(); ;

    public TimedData() {}

    @Override
    public void clearCriticalData() {
        ticksBehind = 0;
        ticksLived = 0;
    }
}
