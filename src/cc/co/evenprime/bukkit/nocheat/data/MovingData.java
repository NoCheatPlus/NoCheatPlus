package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.Location;

/**
 * Playerspecific data for the moving check group
 * 
 * @author Evenprime
 * 
 */
public class MovingData {

    public int      jumpPhase                 = 0;

    public Location runflySetBackPoint        = null;

    public double   runflyViolationLevel      = 0.0D;

    public double   vertFreedom               = 0.0D;
    public double   vertVelocity              = 0.0D;
    public int      vertVelocityCounter       = 0;
    public double   horizFreedom              = 0.0D;
    public int      horizVelocityCounter      = 0;

    public double   nofallViolationLevel      = 0.0D;
    public float    fallDistance              = 0.0F;
    public float    lastAddedFallDistance     = 0.0F;

    public int      noclipX;
    public int      noclipY;
    public int      noclipZ;

    public double   horizontalBuffer;
    public int      bunnyhopdelay             = 0;

    public int      morePacketsCounter;
    public double   morePacketsBuffer         = 50;
    public Location morePacketsSetbackPoint;
    public double   morePacketsViolationLevel = 0;

    public Location teleportTo;

    public int      lastElapsedIngameSeconds  = 0;

}
