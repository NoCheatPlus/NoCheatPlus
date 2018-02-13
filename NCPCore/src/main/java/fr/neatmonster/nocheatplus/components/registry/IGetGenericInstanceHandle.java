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

import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;

/**
 * Generic instance handle getting.
 * 
 * @author asofold
 *
 */
public interface IGetGenericInstanceHandle {

    /**
     * Get a self-updating handle for conveniently getting the currently
     * registered instance.
     * 
     * @param registeredFor
     * @return
     */
    public <T> IGenericInstanceHandle<T> getGenericInstanceHandle(Class<T> registeredFor);

}
