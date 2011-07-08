package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;

public class NukeData {
	
	public int counter = 0;

	public static NukeData get(Player p) {

		NoCheatData data = NoCheatData.getPlayerData(p);

		if(data.nuke == null) {
			data.nuke = new NukeData();
		}

		return data.nuke;
	}
}
