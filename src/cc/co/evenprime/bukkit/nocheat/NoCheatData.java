package cc.co.evenprime.bukkit.nocheat;

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


	
	public int movingJumpPhase = 0; // current jumpingPhase
	public int movingMinorViolationsInARow = 0; 
	public int movingNormalViolationsInARow = 0;
	public int movingHeavyViolationsInARow = 0;
	public Location movingSetBackPoint = null;
	
	public int movingIgnoreNextXEvents = 0;
	
	public long speedhackLastCheck = System.currentTimeMillis(); // timestamp of last check for speedhacks
	public int speedhackEventsSinceLastCheck = 0; // used to identify speedhacks
	public int speedhackViolationsInARow = 0;
	
	NoCheatData() { }
}