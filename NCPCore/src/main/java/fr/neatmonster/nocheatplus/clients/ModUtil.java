package fr.neatmonster.nocheatplus.clients;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.clients.motd.CJBMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ClientMOTD;
import fr.neatmonster.nocheatplus.clients.motd.MCAutoMapMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ReiMOTD;
import fr.neatmonster.nocheatplus.clients.motd.SmartMovingMOTD;
import fr.neatmonster.nocheatplus.clients.motd.ZombeMOTD;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
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
		final ConfigFile config = ConfigManager.getConfigFile(); 
		if (!config.getBoolean(ConfPaths.PROTECT_CLIENTS_MOTD_ACTIVE)){
			// No message is to be sent.
			return;
		}
		// TODO: Somebody test this all !
	    // TODO: add feature to check world specific (!).
	
	    // Check if we allow all the client mods.
	    final boolean allowAll = config.getBoolean(ConfPaths.PROTECT_CLIENTS_MOTD_ALLOWALL);
	    
	    String message = "";
	    for (int i = 0; i < motdS.length; i++){
	    	message = motdS[i].onPlayerJoin(message, player, allowAll);
	    }
	
	    if (!message.isEmpty()){
	    	player.sendMessage(message);
	    }
	}

}
