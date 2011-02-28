package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.Location;

/**
 * Storage for data persistence between events
 * 
 * @author Evenprime
 *
 */
public class NoCheatPluginData {
	
	/**
	 * Don't rely on any of these yet, they are likely going to change their name/functionality 
	 */


	int movingIgnoreNextXEvents = 0;
	int movingJumpPhase = 0; // current jumpingPhase
	int movingMinorViolationsInARow = 0; 
	int movingNormalViolationsInARow = 0;
	int movingHeavyViolationsInARow = 0;
	Location movingSetBackPoint = null;
	
	long speedhackLastCheck = System.currentTimeMillis(); // timestamp of last check for speedhacks
	int speedhackEventsSinceLastCheck = 0; // used to identify speedhacks
	int speedhackViolationsInARow = 0;
	
	NoCheatPluginData() { }
}