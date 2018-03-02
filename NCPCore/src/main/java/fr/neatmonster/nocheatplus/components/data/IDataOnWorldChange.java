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

import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * Player changed world: Data storage specific listener for explicit
 * registration with the appropriate registry.
 * 
 * @author asofold
 *
 */
public interface IDataOnWorldChange {

    /**
     * Called with player having changed the world.
     * 
     * @param player
     * @param pData
     * @param previousWorld
     * @param newWorld
     * @return Return true to remove the data instance from the cache in
     *         question, false otherwise.
     */
    public boolean dataOnWorldChange(Player player, IPlayerData pData, 
            World previousWorld, World newWorld);

}
