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

import java.util.Collection;

/**
 * This component queues other components to automatically register "later", i.e. with an unspecified but finite delay, depending on specification coming with the implementation.
 * The convention is to add this as a component "officially", even if only the sub-components are used.
 * @author mc_dev
 *
 */
public interface IHoldSubComponents {
	
	/**
	 * This is to be called after the specified delay after registering the implementation in a registry.
	 * It is recommended to not return the same elements again on a second call, for the case of delegating to further registries supporting this interface.
	 * @return Always a collection, may be empty, should be empty on the second call.
	 */
	public Collection<Object> getSubComponents();
}
