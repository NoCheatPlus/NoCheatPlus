package cc.co.evenprime.bukkit.nocheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
	public Location movingLastLocation = null;

	public Location teleportInitializedByMe = null;
	public boolean worldChanged = false;
	public boolean respawned = false;

	public long speedhackLastCheck = System.currentTimeMillis(); // timestamp of last check for speedhacks
	public Location speedhackSetBackPoint = null;
	public int speedhackEventsSinceLastCheck = 0; // used to identify speedhacks
	public int speedhackViolationsInARow = 0;

	public int airbuildPerSecond = 0;
	public Runnable airbuildSummaryTask = null;
	public double maxYVelocity = 0.0D;

	public long permissionsLastUpdate = 0;
	public boolean permissionsCache[] = new boolean[8];


	public static final int PERMISSION_MOVING = 0;
	public static final int PERMISSION_FLYING = 1;
	public static final int PERMISSION_SPEEDHACK = 2;
	public static final int PERMISSION_AIRBUILD = 3;
	public static final int PERMISSION_BEDTELEPORT = 4;
	public static final int PERMISSION_P = 5;
	public static final int PERMISSION_NOTIFY = 6;
	public static final int PERMISSION_ITEMDUPE = 7;

	// Store data between Events
	private static final Map<Player, NoCheatData> playerData = new HashMap<Player, NoCheatData>();

	/**
	 * Main access to data that needs to be stored between different events.
	 * Always returns a NoCheatData object, because if there isn't one
	 * for the specified player, one will be created.
	 * 
	 * @param p
	 * @return
	 */
	public static NoCheatData getPlayerData(Player p) {
		NoCheatData data = null;

		if((data = playerData.get(p)) == null ) {
			synchronized(playerData) {
				data = playerData.get(p);
				if(data == null) {
					// If we have no data for the player, create some
					data = new NoCheatData();
					playerData.put(p, data);
				}
			}
		}

		return data;
	}

	/**
	 * Go through the playerData HashMap and remove players that are no longer online
	 * from the map. This should be called in long, regular intervals (e.g. every 10 minutes)
	 * to keep the memory footprint of the plugin low
	 */
	public static void cleanPlayerDataCollection() {
		synchronized(playerData) {
			Iterator<Map.Entry<Player, NoCheatData>> it = playerData.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Player, NoCheatData> pairs = (Map.Entry<Player, NoCheatData>)it.next();
				if(!pairs.getKey().isOnline())
					it.remove();
			}
		}
	}

}