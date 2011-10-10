package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Player specific data for the moving check group
 */
public class MovingData extends Data {

    public int      jumpPhase;

    public Location runflySetBackPoint;

    public double   runflyViolationLevel;

    public double   vertFreedom;
    public double   vertVelocity;
    public int      vertVelocityCounter;
    public double   horizFreedom;
    public int      horizVelocityCounter;

    public double   nofallViolationLevel;
    public float    fallDistance;
    public float    lastAddedFallDistance;

    public double   horizontalBuffer;
    public int      bunnyhopdelay;

    public int      morePacketsCounter;
    public int      morePacketsBuffer;
    public Location morePacketsSetbackPoint;
    public double   morePacketsViolationLevel;

    public Location teleportTo;

    public int      lastElapsedIngameSeconds;

    @Override
    public void initialize(Player player) {
        runflySetBackPoint = player.getLocation();
        morePacketsBuffer = 50;
        morePacketsSetbackPoint = player.getLocation();
    }

    @Override
    public void clearCriticalData() {
        teleportTo = null;
        jumpPhase = 0;
        runflySetBackPoint = null;
        fallDistance = 0;
        lastAddedFallDistance = 0;
        bunnyhopdelay = 0;
        morePacketsBuffer = 50;
        morePacketsSetbackPoint = null;
        lastElapsedIngameSeconds = 0;
        morePacketsCounter = 0;
    }
}
