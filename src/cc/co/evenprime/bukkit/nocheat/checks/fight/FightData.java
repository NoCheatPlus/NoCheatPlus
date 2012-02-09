package cc.co.evenprime.bukkit.nocheat.checks.fight;

import net.minecraft.server.Entity;
import cc.co.evenprime.bukkit.nocheat.DataItem;

public class FightData implements DataItem {

    public double  directionVL;
    public double  noswingVL;
    public double  reachVL;
    public int     speedVL;

    public long    directionLastViolationTime;
    public long    reachLastViolationTime;

    public Entity  damagee;
    public boolean armswung = true;
    public boolean skipNext = false;

    public long    speedTime;
    public int     speedAttackCount;
}
