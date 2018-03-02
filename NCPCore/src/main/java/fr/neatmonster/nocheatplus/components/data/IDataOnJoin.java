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
package fr.neatmonster.nocheatplus.components.data;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * Player join: : Data storage specific listener for explicit registration with
 * the appropriate registry.
 * 
 * @author asofold
 *
 */
public interface IDataOnJoin {

    /**
     * Called with a player join event.
     * 
     * @param player
     * @param pData
     * @return Return true to remove the data instance from the cache in
     *         question, false otherwise.
     */
    public boolean dataOnJoin(Player player, IPlayerData pData);

}
