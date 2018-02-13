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
package fr.neatmonster.nocheatplus.players;

import fr.neatmonster.nocheatplus.worlds.IWorldData;

/**
 * Arguments for factories for types applicable for PlayerData generic instance
 * storage.
 * 
 * @author asofold
 *
 */
public class PlayerFactoryArgument {

    public final IPlayerData playerData;
    public final IWorldData worldData;

    public PlayerFactoryArgument(final IPlayerData playerData, IWorldData worldData) {
        this.playerData = playerData;
        this.worldData = worldData;
    }

}
