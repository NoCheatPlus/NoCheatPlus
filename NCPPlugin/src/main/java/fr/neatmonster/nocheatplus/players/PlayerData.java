package fr.neatmonster.nocheatplus.players;

import fr.neatmonster.nocheatplus.components.IData;

/**
 * Central player data object.
 * <hr>
 * On the medium run this is intended to carry all data for the player...
 * <li>Checks data objects.</li>
 * <li>Time stamps for logged out players</li>
 * <li>Data to be persisted, like set-backs, xray.</li>
 * <br>Might contain...
 * <li>References of configs.</li>
 * <li>Exemption entries.</li>
 * <hr>
 * Main reasons are...
 * <li>Faster cross-check data access both for check and data management.</li>
 * <li>Have the data in one place, easy to control and manage.</li>
 * <li>Easier transition towards non-static access, if it should ever happen.</li>
 * <hr>
 * (not complete)
 * @author mc_dev
 *
 */
public class PlayerData implements IData {
	
}
