package fr.neatmonster.nocheatplus;

import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;

/**
 * Static API provider utility.
 * @author mc_dev
 *
 */
public class NCPAPIProvider {
	private static NoCheatPlusAPI noCheatPlusAPI = null;
	
	/**
	 * Get the registered API instance. This will work after the plugin has loaded (onLoad).
	 */
	public static NoCheatPlusAPI getNoCheatPlusAPI(){
		return noCheatPlusAPI;
	}
	
	/**
	 * Setter for the NoCheatPlusAPI instance.
	 * <hr>
	 * For internal use only (onLoad).<br>
	 * Setting this to anything else than the NoCheatPlus plugin instance might lead to inconsistencies.
	 * @param noCheatPlusAPI
	 */
	protected static void setNoCheatPlusAPI(NoCheatPlusAPI noCheatPlusAPI){
		NCPAPIProvider.noCheatPlusAPI = noCheatPlusAPI;
	}
}
