package cc.co.evenprime.bukkit.nocheat.data;

public class BaseData extends Data {

    public final BlockBreakData blockbreak;
    public final BlockPlaceData blockplace;
    public final ChatData       chat;
    public final LogData        log;
    public final MovingData     moving;
    public final FightData      fight;
    public final TimedData      timed;

    private final Data[]        data;        // for convenience

    public long                 lastUsedTime;

    public boolean              armswung;    // This technically does belong to
                                              // many other checks, so I'll put
                                              // it here for convenience

    public BaseData() {
        this.blockbreak = new BlockBreakData();
        this.blockplace = new BlockPlaceData();
        this.chat = new ChatData();
        this.log = new LogData();
        this.moving = new MovingData();
        this.fight = new FightData();
        this.timed = new TimedData();

        data = new Data[] {this.blockbreak, this.blockplace, this.chat,
                this.log, this.moving, this.fight, this.timed};
    }

    public void clearCriticalData() {
        for(Data d : data) {
            d.clearCriticalData();
        }
    }

    public boolean shouldBeRemoved(long currentTimeInMilliseconds) {
        return lastUsedTime + 60000L < currentTimeInMilliseconds;
    }
}
