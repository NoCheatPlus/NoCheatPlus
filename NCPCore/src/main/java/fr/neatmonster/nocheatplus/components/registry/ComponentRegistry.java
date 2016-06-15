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
package fr.neatmonster.nocheatplus.components.registry;

/**
 * A ComponentRegistry allows registering components, that then should be delegated to where they belong or just be ignored.<br>
 * Notes:
 * <li>Implementations should somehow specify what components can be registered.</li>
 * <li>Implementations should somehow specify if/when they are unregistered automatically.</li>
 * @author mc_dev
 *
 */
public interface ComponentRegistry<T>{
	
	/**
     * Register a component.
     * @param component
	 * @return If (newly) added. Adding an already present component should do nothing.
     */
	public boolean addComponent(final T component);
	
	/**
	 * Remove a registered component. <br>
	 * Does not unregister listeners currently.
	 * @param component
	 */
	public void removeComponent(final T component);
}
