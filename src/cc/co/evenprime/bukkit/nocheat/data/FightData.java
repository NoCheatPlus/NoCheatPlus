package cc.co.evenprime.bukkit.nocheat.data;

import net.minecraft.server.Entity;

public class FightData extends Data {

    public double                   directionVL                = 0;
    public long                     directionLastViolationTime = 0;
    public final ExecutionHistory   history                    = new ExecutionHistory();
    public double                   selfhitVL                  = 0;
    public double                   noswingVL                  = 0.0D;
    public Entity damagee;

}
