package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;

public class AirbuildData {
	public int perSecond = 0;
	public Runnable summaryTask = null;

	public static AirbuildData get(Player p) {

		NoCheatData data = NoCheatData.getPlayerData(p);

		if(data.airbuild == null) {
			data.airbuild = new AirbuildData();
		}

		return data.airbuild;
	}
}
