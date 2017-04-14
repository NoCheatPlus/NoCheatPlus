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

import java.util.UUID;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.components.registry.feature.IRemoveData;

/**
 * A factory for creating and accessing data. This design may be outdated, due
 * to PlayerData soon holding the check data instances and factories becoming
 * factories again.
 * 
 * @author asofold
 */
public interface CheckDataFactory extends IRemoveData{

    /**
     * Gets the data of the specified player. Data might get created, if not
     * present already.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public ICheckData getData(Player player);

    /**
     * Get data, but don't create if not present.
     * 
     * @param playerId
     * @param playerName
     * @return The data instance, if present. Null otherwise.
     */
    public ICheckData getDataIfPresent(UUID playerId, String playerName);

    @Override
    public ICheckData removeData(String playerName);

}
