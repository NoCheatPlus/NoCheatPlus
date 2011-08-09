package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;

import cc.co.evenprime.bukkit.nocheat.data.MovingData;

public class RunningCheck {

    public RunningCheck() {}

    public double check(final Location from, final Location to, final boolean isSneaking, final boolean isSwimming, final MovingData data, MovingCheck check) {

        // How much further did the player move than expected??
        double distanceAboveLimit = 0.0D;

        // First calculate the distance the player has moved horizontally
        final double xDistance = from.getX() - to.getX();
        final double zDistance = from.getZ() - to.getZ();

        final double totalDistance = Math.sqrt((xDistance * xDistance + zDistance * zDistance));

        if(isSneaking) {
            distanceAboveLimit = totalDistance - check.sneakWidth;
        } else if(isSwimming) {
            distanceAboveLimit = totalDistance - check.swimWidth;
        } else {
            distanceAboveLimit = totalDistance - check.stepWidth;
        }

        return distanceAboveLimit - data.horizFreedom;
    }
}
