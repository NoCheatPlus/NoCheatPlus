package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;

import cc.co.evenprime.bukkit.nocheat.data.MovingData;

public class RunningCheck {

    private final static double  maxBonus           = 1D;
    
    public RunningCheck() {}

    public double check(final Location from, final Location to, final boolean isSneaking, final boolean isSwimming, final MovingData data, MovingCheck check) {

        // How much further did the player move than expected??
        double distanceAboveLimit = 0.0D;

        // First calculate the distance the player has moved horizontally
        final double xDistance = from.getX() - to.getX();
        final double zDistance = from.getZ() - to.getZ();

        final double totalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        if(isSneaking) {
            distanceAboveLimit = totalDistance - check.sneakWidth - data.horizFreedom;
        } else if(isSwimming) {
            distanceAboveLimit = totalDistance - check.swimWidth - data.horizFreedom;
        } else {
            distanceAboveLimit = totalDistance - check.stepWidth - data.horizFreedom;
        }
        
        // Did he go too far?
        if(distanceAboveLimit > 0) {
            // Try to consume the "buffer"
            distanceAboveLimit -= data.horizontalBuffer;
            data.horizontalBuffer = 0;

            // Put back the "overconsumed" buffer
            if(distanceAboveLimit < 0) {
                data.horizontalBuffer = -distanceAboveLimit;

            }
        }
        // He was within limits, give the difference as buffer
        else {
            data.horizontalBuffer = Math.min(maxBonus, data.horizontalBuffer - distanceAboveLimit);
        }
        return distanceAboveLimit;
    }
}
