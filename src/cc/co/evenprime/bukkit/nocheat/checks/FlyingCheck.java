package cc.co.evenprime.bukkit.nocheat.checks;


import org.bukkit.Location;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.data.MovingData;

/**
 * Check if the player should be allowed to make that move, e.g. is he allowed to jump here or move that far in one step
 * 
 * @author Evenprime
 *
 */
public class FlyingCheck {

	public FlyingCheck() {	}

	// How many move events can a player have in air before he is expected to lose altitude (or eventually land somewhere)
	private final static int jumpingLimit = 5;

	// How high may a player get compared to his last location with ground contact
	private final static double jumpHeight = 1.31D;

	// How much points should hovering attempts cause?
	private final static double hoveringPunishment = 0.2D;
	
	// How high may a player move in one event on ground
	private final static double stepHeight = 0.501D;


	/**
	 * Calculate if and how much the player "failed" this check. The check should not
	 * modify any data
	 * 
	 */
	public double check(final Player player, final Location from, final boolean fromOnGround, final Location to, final boolean toOnGround, final MovingData data) {

		// How much higher did the player move than expected??
		double distanceAboveLimit = 0.0D;
		
		final double toY = to.getY();
		final double fromY = from.getY();

		double limit = calculateVerticalLimit(data, fromOnGround) + jumpHeight;
		
		// Walk or start Jump
		if(fromOnGround)
		{
			distanceAboveLimit = toY - Math.floor(fromY) - limit;
		}
		// Land or Fly/Fall
		else
		{
			final Location l;

			if(data.setBackPoint == null)
				l = from;
			else
				l = data.setBackPoint;

			if(data.jumpPhase > jumpingLimit) {
				limit -= (data.jumpPhase-jumpingLimit) * 0.2D;
			}

			if(toOnGround) limit += stepHeight;				

			distanceAboveLimit = toY - Math.floor(l.getY()) - limit;

			// Always give some bonus points in case of identical Y values in midair (hovering player)
			if(fromY == toY && !toOnGround) {
				distanceAboveLimit = Math.max(hoveringPunishment, distanceAboveLimit+hoveringPunishment);
			}
		}

		return distanceAboveLimit;	
	}

	private double calculateVerticalLimit(final MovingData data, final boolean onGroundFrom) {

		// A halfway lag-resistant method of allowing vertical acceleration without allowing blatant cheating

		// FACT: Minecraft server sends player "velocity" to the client and lets the client calculate the movement
		// PROBLEM: There may be an arbitrary amount of other move events between the server sending the data
		//          and the client accepting it/reacting to it. The server can't know when the client starts to
		//          consider the sent "velocity" in its movement.
		// SOLUTION: Give the client at least 10 events after sending "velocity" to actually use the velocity for
		//           its movement, plus additional events if the "velocity" was big and can cause longer flights

		// The server sent the player a "velocity" packet a short time ago

		// consume a counter for this client
		if(data.vertFreedomCounter > 0) {
			data.vertFreedomCounter--;
			data.vertFreedom += data.maxYVelocity*2D;
			data.maxYVelocity *= 0.90D;
		}
		
		final double limit = data.vertFreedom;

		// If the event counter has been consumed, remove the vertical movement limit increase when landing the next time
		if(onGroundFrom && data.vertFreedomCounter <= 0) {
			data.vertFreedom = 0.0D;
		}

		return limit;
	}
	
}
