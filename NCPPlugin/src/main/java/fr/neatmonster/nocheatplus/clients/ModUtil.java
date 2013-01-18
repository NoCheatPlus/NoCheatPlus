package fr.neatmonster.nocheatplus.clients;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Utilities for dealing with client mods. This is likely to by just a refactoring stage.
 * @author mc_dev
 *
 */
public class ModUtil {

	/**
	 * Send block codes to the player according to allowed or disallowed client-mods or client-mod features.
	 * @param player
	 */
	public static void checkModsMessage(Player player) {
		// TODO: Somebody test this all !
		
	    String message = "";
	    
	    // TODO: add feature to check world specific (!).
	
	    // Check if we allow all the client mods.
	    final boolean allowAll = ConfigManager.getConfigFile().getBoolean(ConfPaths.MISCELLANEOUS_ALLOWCLIENTMODS);
	
	    // Allow Rei's Minimap's cave mode.
	    if (allowAll || player.hasPermission(Permissions.REI_CAVE))
	        message += "§0§0§1§e§f";
	
	    // Allow Rei's Minimap's radar.
	    if (allowAll || player.hasPermission(Permissions.REI_RADAR)){
	    	// TODO: Does this allow all radar features?
	    	message += "§0§0§2§3§4§5§6§7§e§f";
	    }
	    else{
	        // Allow Rei's Minimap's player radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_PLAYER))
	           message += "§0§0§2§e§f";

	        // Allow Rei's Minimap's animal radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_ANIMAL))
	           message += "§0§0§3§e§f";

	        // Allow Rei's Minimap's mob radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_MOB))
	           message += "§0§0§4§e§f";

	        // Allow Rei's Minimap's slime radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_SLIME))
	           message += "§0§0§5§e§f";

	        // Allow Rei's Minimap's squid radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_SQUID))
	           message += "§0§0§6§e§f";

	        // Allow Rei's Minimap's other radar
	        if (allowAll || player.hasPermission(Permissions.REI_RADAR_OTHER))
	           message += "§0§0§7§e§f";
	    }
	
	    // If all the client mods are allowed, no need to go any further.
	    if (allowAll) {
	        if (!message.equals(""))
	            player.sendMessage(message);
	        return;
	    }
	
	    // Disable Zombe's fly mod.
	    if (!player.hasPermission(Permissions.ZOMBE_FLY))
	        message += "§f §f §1 §0 §2 §4";
	
	    // Disable Zombe's noclip.
	    if (!player.hasPermission(Permissions.ZOMBE_NOCLIP))
	        message += "§f §f §4 §0 §9 §6";
	
	    // Disable Zombe's cheat.
	    if (!player.hasPermission(Permissions.ZOMBE_CHEAT))
	        message += "§f §f §2 §0 §4 §8";
	
	    // Disable CJB's fly mod.
	    if (!player.hasPermission(Permissions.CJB_FLY))
	        message += "§3 §9 §2 §0 §0 §1";
	
	    // Disable CJB's xray.
	    if (!player.hasPermission(Permissions.CJB_XRAY))
	        message += "§3 §9 §2 §0 §0 §2";
	
	    // Disable CJB's radar.
	    if (!player.hasPermission(Permissions.CJB_RADAR))
	        message += "§3 §9 §2 §0 §0 §3";
	
	    // Disable Minecraft AutoMap's ores.
	    if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_ORES))
	        message += "§0§0§1§f§e";
	
	    // Disable Minecraft AutoMap's cave mode.
	    if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_CAVE))
	        message += "§0§0§2§f§e";
	
	    // Disable Minecraft AutoMap's radar.
	    if (!player.hasPermission(Permissions.MINECRAFTAUTOMAP_RADAR))
	        message += "§0§0§3§4§5§6§7§8§f§e";
	
	    // Disable Smart Moving's climbing.
	    if (!player.hasPermission(Permissions.SMARTMOVING_CLIMBING))
	        message += "§0§1§0§1§2§f§f";
	
	    // Disable Smart Moving's climbing.
	    if (!player.hasPermission(Permissions.SMARTMOVING_SWIMMING))
	        message += "§0§1§3§4§f§f";
	
	    // Disable Smart Moving's climbing.
	    if (!player.hasPermission(Permissions.SMARTMOVING_CRAWLING))
	        message += "§0§1§5§f§f";
	
	    // Disable Smart Moving's climbing.
	    if (!player.hasPermission(Permissions.SMARTMOVING_SLIDING))
	        message += "§0§1§6§f§f";
	
	    // Disable Smart Moving's climbing.
	    if (!player.hasPermission(Permissions.SMARTMOVING_JUMPING))
	        message += "§0§1§8§9§a§b§f§f";
	
	    // Disable Smart Moving's climbing.
	    if (!player.hasPermission(Permissions.SMARTMOVING_FLYING))
	        message += "§0§1§7§f§f";
	
	    if (!message.equals(""))
	        player.sendMessage(message);
	}

}
