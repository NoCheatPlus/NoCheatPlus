package cc.co.evenprime.bukkit.nocheat.data;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;

public class MovingData {
	public int jumpPhase = 0;
	public int violationsInARow[] =  { 0, 0, 0 }; 
	public double horizFreedom = 0.0D;
	public int horizFreedomCounter = 0;
	public double vertFreedom = 0.0D;
	public int vertFreedomCounter = 0;
	public Location setBackPoint = null;
	public Runnable summaryTask = null;
	public Level highestLogLevel = null;
	public double maxYVelocity = 0.0D;

	public boolean worldChanged = false;
	public boolean respawned = false;

	// WORKAROUND for changed PLAYER_MOVE logic
	public Location teleportTo = null;
	public Location lastLocation = null;

	public Location teleportInitializedByMe = null;

	public static MovingData get(Player p) {

		NoCheatData data = NoCheatData.getPlayerData(p);

		if(data.moving == null) {
			data.moving = new MovingData();
		}

		return data.moving;
	}

}
