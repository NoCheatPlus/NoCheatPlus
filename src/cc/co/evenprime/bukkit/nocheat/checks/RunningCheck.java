package cc.co.evenprime.bukkit.nocheat.checks;

import org.bukkit.Location;

import cc.co.evenprime.bukkit.nocheat.data.MovingData;

public class RunningCheck {

	public final static double stepWidth = 0.22D;
	public final static double sneakWidth = 0.14D;
	public final static double swimWidth = 0.18D;
		
	public RunningCheck() {	}
 
	public double check(final Location from, final Location to, final boolean isSneaking, final boolean isSwimming, final MovingData data) {

		// How much further did the player move than expected??
		double distanceAboveLimit = 0.0D;
		
		// First calculate the distance the player has moved horizontally
		final double xDistance = from.getX()-to.getX();
		final double zDistance = from.getZ()-to.getZ();

		final double totalDistance = Math.sqrt((xDistance*xDistance + zDistance*zDistance));

		// TODO: Also ask cc which to apply
		if(isSneaking) {
			distanceAboveLimit = totalDistance - sneakWidth;
		}
		else if(isSwimming) {
			distanceAboveLimit = totalDistance - swimWidth;
		}
		else {
			distanceAboveLimit = totalDistance - stepWidth;
		}
		
		return distanceAboveLimit;
	}
}
