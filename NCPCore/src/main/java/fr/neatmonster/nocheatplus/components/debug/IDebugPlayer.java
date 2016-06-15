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
package fr.neatmonster.nocheatplus.components.debug;

import org.bukkit.entity.Player;

/**
 * Convenient player-specific debug messages with standard format.
 * 
 * @author asofold
 *
 */
public interface IDebugPlayer {

    /**
     * Output a message for a player with the standard format (see
     * CheckUtils.debug(Player, CheckType, String).
     * 
     * @param player
     *            May be null.
     * @param message
     */
    public void debug(Player player, String message);

}
