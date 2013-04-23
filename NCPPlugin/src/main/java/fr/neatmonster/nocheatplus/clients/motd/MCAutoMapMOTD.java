package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.permissions.Permissions;

public class MCAutoMapMOTD extends ClientMOTD {

	@Override
	public String onPlayerJoin(String message, Player player, boolean allowAll) {

		if (allowAll){
			return message;
		}
		
		String mcAutoMap = "";
		
		// TODO: Is there a compact version (just one prefix)?
		
		// Disable Minecraft AutoMap's ores.
	    if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_ORES)){
	    	mcAutoMap += "§0§0§1§f§e";
	    }
	
	    // Disable Minecraft AutoMap's cave mode.
	    if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_CAVE)){
	    	mcAutoMap += "§0§0§2§f§e";
	    }
	
	    // Disable Minecraft AutoMap's radar.
	    if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_RADAR)){
	    	mcAutoMap += "§0§0§3§4§5§6§7§8§f§e";
	    }
		
		if (mcAutoMap.isEmpty()){
			return message;
		}
		else{
			return message + mcAutoMap;
		}
	}

}
