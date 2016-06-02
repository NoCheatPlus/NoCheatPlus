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
package fr.neatmonster.nocheatplus.checks.access;

import java.util.Collection;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Quick and dirty way to add factories for sub checks for more precise data removal from a more general data object.
 * @author mc_dev
 *
 */
public abstract class SubCheckDataFactory<D extends ICheckData> implements CheckDataFactory {
	protected final CheckDataFactory parentFactory;
	protected final CheckType checkType;

	public SubCheckDataFactory(CheckType checkType, CheckDataFactory parentFactory) {
		this.checkType = checkType;
		this.parentFactory = parentFactory;
	}

	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.components.IRemoveData#removeAllData()
	 */
	@Override
	public void removeAllData() {
		for (String playerName : getPresentData()) {
			D data = getData(playerName);
			if (data != null) {
				removeFromData(playerName, data);
			}
		}
	}

	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.checks.access.CheckDataFactory#getData(org.bukkit.entity.Player)
	 */
	@Override
	public ICheckData getData(Player player) {
		return parentFactory.getData(player);
	}

	/* (non-Javadoc)
	 * @see fr.neatmonster.nocheatplus.checks.access.CheckDataFactory#removeData(java.lang.String)
	 */
	@Override
	public ICheckData removeData(String playerName) {
		if (!hasData(playerName)) {
			return null;
		}
		D data = getData(playerName);
		if (data != null) {
			if (removeFromData(playerName, data)) {
				// Return data instance, if changed.
				return data;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param playerName Exact case lookup.
	 * @return Can return null.
	 */
	protected abstract D getData(String playerName);
	
	/**
	 * Names are expected to be exact case. This collection is demanded to be iterable (eclipse: adorable) in case the check runs asynchronously.<br>
	 * This method might change "a lot" with time. 
	 * @return
	 */
	protected abstract Collection<String> getPresentData();
	
	/**
	 * Fast check, if there is data for the player.
	 * @param playerName Exact case lookup.
	 * @return
	 */
	protected abstract boolean hasData(String playerName);
	
	/**
	 * Remove the specific data from the given data instance.<br>
	 * TODO: Might add timestamp as argument (ms).
	 * @param playerName Exact case. Just for reference.
	 * @param data The data from which to remove the checkType-specific parts. This will never be null.
	 * @return If changed.
	 */
	protected abstract boolean removeFromData(String playerName, D data);
}
