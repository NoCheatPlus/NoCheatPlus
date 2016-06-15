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

/**
 * Can be registered with the TickTask. Ensure that registered objects get unregistered before the plugin gets enabled again. You can ensure it by using NoCheatPlusAPI to register a TickListener. 
 * @author mc_dev
 *
 */
public interface TickListener {
	/**
	 * 
	 * @param tick Current tick count. This might start over at 0 if reset in onEnable.
	 * @param timeLast Last time after processing loop. Allows to check how long the tick already took (roughly). No "system time ran backwards" check for this value.
	 */
	public void onTick(int tick, long timeLast);
}
