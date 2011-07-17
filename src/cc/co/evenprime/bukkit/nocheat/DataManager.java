package cc.co.evenprime.bukkit.nocheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.data.AirbuildData;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.data.NukeData;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.data.SpeedhackData;


public class DataManager {

	// Store data between Events
	private final Map<Player, NoCheatData> playerData = new HashMap<Player, NoCheatData>();
	
	public DataManager() { }
	
	/**
	 * Go through the playerData HashMap and remove players that are no longer online
	 * from the map. This should be called in long, regular intervals (e.g. every 10 minutes)
	 * to keep the memory footprint of the plugin low
	 */
	public void cleanPlayerDataCollection() {
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
	 * Main access to data that needs to be stored between different events.
	 * Always returns a NoCheatData object, because if there isn't one
	 * for the specified player, one will be created.
	 * 
	 * @param p
	 * @return
	 */
	public NoCheatData getPlayerData(Player p) {
		NoCheatData data = playerData.get(p);

		if(data == null) {
			synchronized(playerData) {
				// If we have no data for the player, create some
				data = new NoCheatData();
				playerData.put(p, data);
			}
		}

		return data;
	}
	
	public AirbuildData getAirbuildData(Player p) {

		NoCheatData data = getPlayerData(p);

		if(data.airbuild == null) {
			data.airbuild = new AirbuildData();
		}

		return data.airbuild;
	}
	
	public MovingData getMovingData(final Player p) {

		final NoCheatData data = getPlayerData(p);

		if(data.moving == null) {
			data.moving = new MovingData();
			data.moving.teleportedTo = p.getLocation();
		}

		return data.moving;
	}
	
	public NukeData getNukeData(Player p) {

		NoCheatData data = getPlayerData(p);

		if(data.nuke == null) {
			data.nuke = new NukeData();
		}

		return data.nuke;
	}
	
	public PermissionData getPermissionData(Player p) {

		NoCheatData data = getPlayerData(p);

		if(data.permission == null) {
			data.permission = new PermissionData();
		}

		return data.permission;
	}
	
	public SpeedhackData getSpeedhackData(Player p) {

		NoCheatData data = getPlayerData(p);

		if(data.speedhack == null) {
			data.speedhack = new SpeedhackData();
		}

		return data.speedhack;
	}
	

	/**
	 * Go through the playerData HashMap and remove players that are no longer online
	 * from the map. This should be called in long, regular intervals (e.g. every 10 minutes)
	 * to keep the memory footprint of the plugin low
	 */
	public void cancelPlayerDataTasks() {
		synchronized(playerData) {
			Iterator<Map.Entry<Player, NoCheatData>> it = playerData.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Player, NoCheatData> pairs = (Map.Entry<Player, NoCheatData>)it.next();
				
				AirbuildData d = pairs.getValue().airbuild;
				
				if(d != null) {
					int id = d.summaryTask;
					
					if(id != -1) {
						Bukkit.getServer().getScheduler().cancelTask(id);
					}
					else {
						// To prevent accidentially creating a new one while cleaning up
						d.summaryTask = 1; 
					}
				}
				
				MovingData d2 = pairs.getValue().moving;
				
				if(d2 != null) {
					int id = d2.summaryTask;
					
					if(id != -1) {
						Bukkit.getServer().getScheduler().cancelTask(id);
					}
					else {
						// To prevent accidentially creating a new one while cleaning up
						d2.summaryTask = 1; 
					}
				}
			}
		}
	}

}
