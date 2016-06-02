/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	 * Get the registered API instance. This will work after the plugin has loaded (onLoad), asynchronous calls should be possible, however calls after plugin disable or before it is loaded should fail.
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
