package cc.co.evenprime.bukkit.nocheat;

import java.util.logging.Level;

import org.bukkit.Location;

/**
 * Storage for data persistence between events
 * 
 * @author Evenprime
 *
 */
public class NoCheatData {

	/**
	 * Don't rely on any of these yet, they are likely going to change their name/functionality 
	 */

	public int movingJumpPhase = 0;
	public int movingViolationsInARow[] =  { 0, 0, 0 }; 
	public double movingHorizFreedom = 0.0D;
	public int movingHorizFreedomCounter = 0;
	public double movingVertFreedom = 0.0D;
	public int movingVertFreedomCounter = 0;
	public Location movingSetBackPoint = null;
	public Runnable movingSummaryTask = null;
	public Level movingHighestLogLevel = null;
	
	// WORKAROUND for changed PLAYER_MOVE logic
	public Location movingTeleportTo = null;
	
	public boolean reset = false;

	public long speedhackLastCheck = System.currentTimeMillis(); // timestamp of last check for speedhacks
	public Location speedhackSetBackPoint = null;
	public int speedhackEventsSinceLastCheck = 0; // used to identify speedhacks
	public int speedhackViolationsInARow = 0;

	public int airbuildPerSecond = 0;
	public Runnable airbuildSummaryTask = null;

	public NoCheatData() { }
}