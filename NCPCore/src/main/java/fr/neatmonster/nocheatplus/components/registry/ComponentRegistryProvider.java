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

import java.util.Collection;

/**
 * An object allowing to get ComponentRegistry implementations of a specific type.
 * This class is not specialized to maintain flexibility.
 * @author mc_dev
 *
 */
public interface ComponentRegistryProvider{
	
	/**
	 * Get all available specialized ComponentFactory instances matching the given signature. This is not meant as a factory method but for more efficient registration for the case of the regestry being present. 
	 * @param clazz
	 * @return Some collection, empty collection in case no matches are found.
	 */
	public <T> Collection<ComponentRegistry<T>> getComponentRegistries(Class<ComponentRegistry<T>> clazz);
	
}
