package cc.co.evenprime.bukkit.nocheat.checks.moving;

import java.util.Map;
import cc.co.evenprime.bukkit.nocheat.DataItem;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

/**
 * Player specific data for the moving check group
 */
public class MovingData implements DataItem {

    public double                 runflyVL;

    public double                 runflyRunningTotalVL;
    public int                    runflyRunningFailed;

    public double                 runflyFlyingTotalVL;
    public int                    runflyFlyingFailed;

    public double                 runflySneakingTotalVL;
    public int                    runflySneakingFailed;

    public double                 runflySwimmingTotalVL;
    public int                    runflySwimmingFailed;

    public double                 nofallVL;
    public double                 nofallTotalVL;
    public int                    nofallFailed;

    public double                 morePacketsVL;
    public double                 morePacketsTotalVL;
    public int                    morePacketsFailed;

    public int                    jumpPhase;

    public final PreciseLocation  runflySetBackPoint      = new PreciseLocation();

    public double                 vertFreedom;
    public double                 vertVelocity;
    public int                    vertVelocityCounter;
    public double                 horizFreedom;
    public int                    horizVelocityCounter;

    public float                  fallDistance;
    public float                  lastAddedFallDistance;

    public double                 horizontalBuffer;
    public int                    bunnyhopdelay;

    public int                    morePacketsCounter;
    public int                    morePacketsBuffer       = 50;
    public int                    packets;

    public final PreciseLocation  morePacketsSetbackPoint = new PreciseLocation();

    public final PreciseLocation  teleportTo              = new PreciseLocation();

    public int                    lastElapsedIngameSeconds;

    public final PreciseLocation  from                    = new PreciseLocation();
    public final PreciseLocation  to                      = new PreciseLocation();

    public boolean                fromOnOrInGround;
    public boolean                toOnOrInGround;

    public String                 checknamesuffix         = "";

    @Override
    public void clearCriticalData() {
        teleportTo.reset();
        jumpPhase = 0;
        runflySetBackPoint.reset();
        fallDistance = 0;
        lastAddedFallDistance = 0;
        bunnyhopdelay = 0;
        morePacketsBuffer = 50;
        morePacketsSetbackPoint.reset();
        lastElapsedIngameSeconds = 0;
        morePacketsCounter = 0;
    }

    @Override
    public void collectData(Map<String, Object> map) {
        map.put("moving.running.vl", (int) runflyRunningTotalVL);
        map.put("moving.flying.vl", (int) runflyFlyingTotalVL);
        map.put("moving.sneaking.vl", (int) runflySneakingTotalVL);
        map.put("moving.swimming.vl", (int) runflySwimmingTotalVL);
        map.put("moving.nofall.vl", (int) nofallTotalVL);
        map.put("moving.morepackets.vl", (int) morePacketsTotalVL);

        map.put("moving.running.failed", runflyRunningFailed);
        map.put("moving.flying.failed", runflyFlyingFailed);
        map.put("moving.sneaking.failed", runflySneakingFailed);
        map.put("moving.swimming.failed", runflySwimmingFailed);
        map.put("moving.nofall.failed", nofallFailed);
        map.put("moving.morepackets.failed", morePacketsFailed);
    }
}
