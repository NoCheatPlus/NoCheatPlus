package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.permissions.Permissions;

public class ZombeMOTD extends ClientMOTD {

	@Override
	public String onPlayerJoin(String message, Player player, boolean allowAll) {
		
		String zombe = "";
		
		// TODO: Is there a compact version (just one prefix)?
		
		// Disable Zombe's noclip.
	    if (allowAll || player.hasPermission(Permissions.ZOMBE_NOCLIP)){
	    	zombe += "§f §f §4 §0 §9 §6";
	    }
	    
	    if (!allowAll){
	    	// Disable Zombe's fly mod.
		    if (!player.hasPermission(Permissions.ZOMBE_FLY)){
		    	zombe += "§f §f §1 §0 §2 §4";
		    }
		
		    // Disable Zombe's cheat.
		    if (!player.hasPermission(Permissions.ZOMBE_CHEAT)){
		    	zombe += "§f §f §2 §0 §4 §8";
		    }
	    }
	    
	    if (zombe.isEmpty()){
	    	return message;
	    }
	    else{
	    	return message + zombe;
	    }
	}

}
