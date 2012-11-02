package fr.neatmonster.nocheatplus.components;

/**
 * Interface for component registration to allow cleanup for player data. 
 * @author mc_dev
 *
 */
public interface IRemoveData {
	/**
	 * Remove the data of one player.
	 * @param playerName
	 * @return IData instance, if it was present.
	 */
	public IData removeData(String playerName);
	
	/**
	 * Remove the data of all players.
	 */
	public void removeAllData();
}
