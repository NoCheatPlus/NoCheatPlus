package fr.neatmonster.nocheatplus.clients;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.clients.motd.CJBMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ClientMOTD;
import fr.neatmonster.nocheatplus.clients.motd.MCAutoMapMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ReiMOTD;
import fr.neatmonster.nocheatplus.clients.motd.SmartMovingMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ZombeMOTD;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;

/**
 * Utilities for dealing with client mods. This is likely to by just a refactoring stage.
 * @author mc_dev
 *
 */
public class ModUtil {
	
	private static final ClientMOTD[] motdS = new ClientMOTD[]{
		new ReiMOTD(),
		new ZombeMOTD(),
		new SmartMovingMOTD(),
		new CJBMOTD(),
		new MCAutoMapMOTD()
	};

	/**
	 * Send block codes to the player according to allowed or disallowed client-mods or client-mod features.
	 * @param player
	 */
	public static void motdOnJoin(final Player player) {
		
		// TODO: Somebody test this all !
	    // TODO: add feature to check world specific (!).
	
	    // Check if we allow all the client mods.
	    final boolean allowAll = ConfigManager.getConfigFile().getBoolean(ConfPaths.MISCELLANEOUS_ALLOWCLIENTMODS);
	    
	    String message = "";
	    for (int i = 0; i < motdS.length; i++){
	    	message = motdS[i].onPlayerJoin(message, player, allowAll);
	    }
	
	    if (!message.isEmpty()){
	    	player.sendMessage(message);
	    }
	}

}
