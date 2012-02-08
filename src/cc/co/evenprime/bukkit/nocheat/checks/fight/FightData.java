package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Map;
import net.minecraft.server.Entity;
import cc.co.evenprime.bukkit.nocheat.DataItem;

public class FightData implements DataItem {

    public double  directionVL;
    public double  directionTotalVL;
    public int     directionFailed;
    public double  noswingVL;
    public double  noswingTotalVL;
    public int     noswingFailed;
    public double  reachVL;
    public double  reachTotalVL;
    public int     reachFailed;
    public int     speedVL;
    public int     speedTotalVL;
    public int     speedFailed;

    public long    directionLastViolationTime;
    public long    reachLastViolationTime;

    public Entity  damagee;
    public boolean armswung = true;
    public boolean skipNext = false;

    public long    speedTime;
    public int     speedAttackCount;

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("fight.direction.vl", (int) directionTotalVL);
        map.put("fight.noswing.vl", (int) noswingTotalVL);
        map.put("fight.reach.vl", (int) reachTotalVL);
        map.put("fight.speed.vl", (int) speedTotalVL);

     
        map.put("fight.direction.failed", (int) directionFailed);
        map.put("fight.noswing.failed", (int) noswingFailed);
        map.put("fight.reach.failed", (int) reachFailed);
        map.put("fight.speed.failed", (int) speedFailed);

    }
}
