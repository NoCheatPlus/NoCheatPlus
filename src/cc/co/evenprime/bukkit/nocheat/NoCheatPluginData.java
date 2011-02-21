package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.Location;

/**
 * Storage for data persistence between events
 *
 */
public class NoCheatPluginData {
	
	/**
	 *  Don't rely on any of these yet, they are likely going to 
	 * change their name/functionality 
	 */
	int phase = 0; // current jumpingPhase
	long lastSpeedHackCheck = System.currentTimeMillis(); // timestamp of last check for speedhacks
	int eventsSinceLastSpeedHackCheck = 0; // used to identify speedhacks
	int ignoreNextXEvents = 0;
	
	int minorViolationsInARow = 0;
	int normalViolationsInARow = 0;
	int heavyViolationsInARow = 0;
	Location movingSetBackPoint = null;
	
	NoCheatPluginData() { }
}