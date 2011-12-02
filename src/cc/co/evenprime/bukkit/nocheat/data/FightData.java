package cc.co.evenprime.bukkit.nocheat.data;

import java.util.Map;

import net.minecraft.server.Entity;

public class FightData extends Data {

    public double                 directionVL                = 0.0D;
    public double                 directionTotalVL           = 0.0D;
    public int                    directionFailed            = 0;
    public double                 selfhitVL                  = 0.0D;
    public double                 selfhitTotalVL             = 0.0D;
    public int                    selfhitFailed              = 0;
    public double                 noswingVL                  = 0.0D;
    public double                 noswingTotalVL             = 0.0D;
    public int                    noswingFailed              = 0;

    public long                   directionLastViolationTime = 0;
    public final ExecutionHistory history                    = new ExecutionHistory();

    public Entity                 damagee;
    public boolean                armswung                   = true;

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("fight.direction.vl", (int)directionTotalVL);
        map.put("fight.selfhit.vl", (int)selfhitTotalVL);
        map.put("fight.noswing.vl", (int)noswingTotalVL);
        map.put("fight.direction.failed", directionFailed);
        map.put("fight.selfhit.failed", selfhitFailed);
        map.put("fight.noswing.failed", noswingFailed);
    }
}
