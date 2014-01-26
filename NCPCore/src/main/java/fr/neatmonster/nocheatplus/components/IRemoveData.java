package fr.neatmonster.nocheatplus.components;

/**
 * Interface for component registration to allow cleanup for player data.<br>
 * NOTES:
 * <li>For CheckType-specific data removal, IHaveCheckType should be implemented, otherwise this data might get ignored until plugin-disable.</li>
 * <li>In case of data removal for CheckType.ALL this might get called for either a certain player or all.</li>
 * @author mc_dev
 *
 */
public interface IRemoveData {
	/**
	 * Remove the data of one player.
	 * @param playerName
	 * @return IData instance if something was changed. Note that this should also return an existing data instance, if it is only partially cleared and not actually removed.
	 */
	public IData removeData(String playerName);
	
	/**
	 * Remove the data of all players.
	 */
	public void removeAllData();
}
