package cc.co.evenprime.bukkit.nocheat.data;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;

public class PermissionData {

	public long permissionsLastUpdate = 0;
	public boolean permissionsCache[] = new boolean[8];

	public static final int PERMISSION_MOVING = 0;
	public static final int PERMISSION_FLYING = 1;
	public static final int PERMISSION_SPEEDHACK = 2;
	public static final int PERMISSION_AIRBUILD = 3;
	public static final int PERMISSION_BEDTELEPORT = 4;
	public static final int PERMISSION_BOGUSITEMS = 5;
	public static final int PERMISSION_NOTIFY = 6;
	public static final int PERMISSION_ITEMDUPE = 7;

	public static PermissionData get(Player p) {

		NoCheatData data = NoCheatData.getPlayerData(p);

		if(data.permission == null) {
			data.permission = new PermissionData();
		}

		return data.permission;
	}

}
