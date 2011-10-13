package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.entity.Player;


public class BaseData {

    public final BlockBreakData blockbreak;
    public final BlockPlaceData blockplace;
    public final ChatData chat;
    public final LogData log;
    public final MovingData moving;
    public final FightData fight;
    
    private final Data[] data;

    
    public BaseData() {
        this.blockbreak = new BlockBreakData();
        this.blockplace = new BlockPlaceData();
        this.chat = new ChatData();
        this.log = new LogData();
        this.moving = new MovingData();
        this.fight = new FightData();
        
        data = new Data[] { this.blockbreak, this.blockplace, this.chat, this.log, this.moving, this.fight };
    }

    public void clearCriticalData() {
        for(Data d : data) {
            d.clearCriticalData();
        }
    }

    public void initialize(Player player) {
        for(Data d : data) {
            d.initialize(player);
        }
    }
}
