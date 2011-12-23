package cc.co.evenprime.bukkit.nocheat.data;

import java.util.Map;

public class BaseData extends Data {

    public final BlockBreakData blockbreak;
    public final BlockPlaceData blockplace;
    public final ChatData       chat;
    public final MovingData     moving;
    public final FightData      fight;
    public final InventoryData  inventory;

    private final Data[]        data;      // for convenience

    private final long          timestamp;

    public BaseData() {
        this.blockbreak = new BlockBreakData();
        this.blockplace = new BlockPlaceData();
        this.chat = new ChatData();
        this.moving = new MovingData();
        this.fight = new FightData();
        this.inventory = new InventoryData();

        data = new Data[] {this.blockbreak, this.blockplace, this.chat,
                this.moving, this.fight, this.inventory};

        this.timestamp = System.currentTimeMillis();

    }

    public void clearCriticalData() {
        for(Data d : data) {
            d.clearCriticalData();
        }
    }

    public void collectData(Map<String, Object> map) {
        for(Data d : data) {
            d.collectData(map);
        }

        map.put("nocheat.starttime", timestamp);
        map.put("nocheat.endtime", System.currentTimeMillis());
    }
}
