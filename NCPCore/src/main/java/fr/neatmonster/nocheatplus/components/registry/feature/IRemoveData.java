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
package fr.neatmonster.nocheatplus.components.registry.feature;

import fr.neatmonster.nocheatplus.components.data.IData;

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
