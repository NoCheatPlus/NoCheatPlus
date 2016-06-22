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
package fr.neatmonster.nocheatplus.components.registry.event;

/**
 * Allow to unregister listeners, should also disable internally created handles
 * if they are this listener. Rather an internal interface.
 * 
 * @author asofold
 *
 */
public interface IUnregisterGenericInstanceRegistryListener {

    public <T> void unregisterGenericInstanceRegistryListener(Class<T> registeredFor, IGenericInstanceRegistryListener<T> listener);

}
