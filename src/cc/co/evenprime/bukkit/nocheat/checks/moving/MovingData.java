package cc.co.evenprime.bukkit.nocheat.checks.moving;

import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

/**
 * Player specific data for the moving check group
 */
public class MovingData implements DataItem {

    public double                runflyVL;
    public double                nofallVL;
    public double                morePacketsVL;

    public int                   jumpPhase;
    public double                lastJumpAmplifier;
    public int                   onIce                   = 0;

    public final PreciseLocation runflySetBackPoint      = new PreciseLocation();

    public double                vertFreedom;
    public double                vertVelocity;
    public int                   vertVelocityCounter;
    public double                horizFreedom;
    public int                   horizVelocityCounter;

    public float                 fallDistance;
    public float                 lastAddedFallDistance;

    public double                horizontalBuffer;
    public int                   bunnyhopdelay;

    public long                  morePacketsLastTime;
    public int                   morePacketsBuffer       = 50;

    public final PreciseLocation morePacketsSetbackPoint = new PreciseLocation();

    public final PreciseLocation teleportTo              = new PreciseLocation();

    public final PreciseLocation from                    = new PreciseLocation();
    public final PreciseLocation to                      = new PreciseLocation();

    public boolean               fromOnOrInGround;
    public boolean               toOnOrInGround;

    public Id                    statisticCategory       = Id.MOV_RUNNING;

    public int                   packets;

    public void clearRunFlyData() {
        runflySetBackPoint.reset();
        jumpPhase = 0;
        fallDistance = 0;
        lastAddedFallDistance = 0;
        bunnyhopdelay = 0;
    }

    public void clearMorePacketsData() {
        morePacketsSetbackPoint.reset();
    }
}
