package fr.neatmonster.nocheatplus.clients.motd;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Rei's Minimap v3.3_04
 * 
 * http://www.minecraftforum.net/topic/482147-151-mar21-reis-minimap-v33-04/
 * 
 * &0&0: prefix
 * &e&f: suffix
 * &1: cave mapping
 * &2: entities radar (player)
 * &3: entities radar (animal)
 * &4: entities radar (mob)
 * &5: entities radar (slime)
 * &6: entities radar (squid)
 * &7: entities radar (other living)
 * 
 * @author mc_dev
 *
 */
public class ReiMOTD extends ClientMOTD {

	@Override
	public String onPlayerJoin(String message, Player player, boolean allowAll) {
		String rei = "";
		
		// Allow Rei's Minimap's cave mode.
	    if (allowAll || player.hasPermission(Permissions.REI_CAVE)){
	    	rei += "§1";
	    }
	
	    // Allow Rei's Minimap's radar.
	    if (allowAll || player.hasPermission(Permissions.REI_RADAR)){
	    	// TODO: Does this allow all radar features?
	    	rei += "§2§3§4§5§6§7";
	    }
	    else{
	        // Allow Rei's Minimap's player radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_PLAYER)){
	        	rei += "§2";
	        }

	        // Allow Rei's Minimap's animal radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_ANIMAL)){
	        	rei += "§3";
	        }

	        // Allow Rei's Minimap's mob radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_MOB)){
	        	rei += "§4";
	        }

	        // Allow Rei's Minimap's slime radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_SLIME)){
	        	rei += "§5";
	        }

	        // Allow Rei's Minimap's squid radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_SQUID)){
	        	rei += "§6";
	        }

	        // Allow Rei's Minimap's other radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_OTHER)){
	        	rei += "§7";
	        }
	    }
				
		if (rei.isEmpty()){
			return message;
		}
		else{
			rei = "§0§0" + rei + "§e§f";
			return message + rei;
		}
	}

}
