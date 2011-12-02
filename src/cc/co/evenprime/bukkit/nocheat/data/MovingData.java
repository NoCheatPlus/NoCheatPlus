package cc.co.evenprime.bukkit.nocheat.data;

import java.util.Map;

/**
 * Player specific data for the moving check group
 */
public class MovingData extends Data {

    public double                 runflyVL;
    public double                 runflyTotalVL;
    public int                    runflyFailed;

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

    public final ExecutionHistory history                 = new ExecutionHistory();

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
        map.put("moving.runfly.vl", (int) runflyTotalVL);
        map.put("moving.nofall.vl", (int) nofallTotalVL);
        map.put("moving.morepackets.vl", (int) morePacketsTotalVL);
        map.put("moving.runfly.failed", runflyFailed);
        map.put("moving.nofall.failed", nofallFailed);
        map.put("moving.morepackets.failed", morePacketsFailed);
    }
}
