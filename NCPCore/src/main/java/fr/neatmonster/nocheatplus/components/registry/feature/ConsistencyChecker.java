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

import org.bukkit.entity.Player;

/**
 * This component might be called periodically. Might not be called ever.
 * @author mc_dev
 *
 */
public interface ConsistencyChecker {
	/**
	 * Perform consistency checking. Depending on configuration this should clean up inconsistent states and/or log problems.
	 * @param onlinePlayers Players as returned by Server.getOnlinePlayers, at the point of time before checking.
	 */
	public void checkConsistency(Player[] onlinePlayers);
	
	// TODO: Might add method to check consistency for single players (on join, on certain check failures).
}
