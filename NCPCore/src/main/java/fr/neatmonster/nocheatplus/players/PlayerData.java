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
 * <li>Player references<li>
 * <hr>
 * Main reasons are...
 * <li>Faster cross-check data access both for check and data management.</li>
 * <li>Have the data in one place, easy to control and manage.</li>
 * <li>Easier transition towards non-static access, if it should ever happen.</li>
 * <hr>
 * (not complete)<br>
 * Might contain individual settings such as debug flags, exemption, notification settings, task references.
 * @author mc_dev
 *
 */
public class PlayerData implements IData {
	
	public final PlayerTask task;
	
	/** Lower case name of the player. */
	final String lcName;
	
	/**
	 * 
	 * @param playerName Accurate case not (yet) demanded.
	 */
	public PlayerData(final String playerName){
		this.lcName = playerName.toLowerCase();
		this.task = new PlayerTask(this.lcName);
	}
	
	
}
