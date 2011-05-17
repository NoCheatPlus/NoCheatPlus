package cc.co.evenprime.bukkit.nocheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.data.AirbuildData;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.data.SpeedhackData;

/**
 * per player storage for data persistence between events 
 * 
 * @author Evenprime
 *
 */
public class NoCheatData {

	/**
	 * Don't rely on any of these yet, they are likely going to change their name/functionality 
	 */
	public MovingData moving; 
	public SpeedhackData speedhack; 
	public AirbuildData airbuild;

	public PermissionData permission;



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
		NoCheatData data = playerData.get(p);

		if(data == null) {
			// If we have no data for the player, create some
			data = new NoCheatData();
			playerData.put(p, data);
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
	
	/**
	 * Go through the playerData HashMap and remove players that are no longer online
	 * from the map. This should be called in long, regular intervals (e.g. every 10 minutes)
	 * to keep the memory footprint of the plugin low
	 */
	public static void cancelPlayerDataTasks() {
		synchronized(playerData) {
			Iterator<Map.Entry<Player, NoCheatData>> it = playerData.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Player, NoCheatData> pairs = (Map.Entry<Player, NoCheatData>)it.next();
				
				int id;
				id = pairs.getValue().airbuild != null ? pairs.getValue().airbuild.summaryTask : -1;
				
				if(id != -1)
					Bukkit.getServer().getScheduler().cancelTask(id);
				
				id = pairs.getValue().moving != null ? pairs.getValue().moving.summaryTask : -1;
				
				if(id != -1)
					Bukkit.getServer().getScheduler().cancelTask(id);
			}
		}
	}

}