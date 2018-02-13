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
 * Generic instance fetching (minimal getter).
 * 
 * @author asofold
 *
 */
public interface IGetGenericInstance {

    /**
     * Retrieve the instance registered for the given class.
     * 
     * @param registeredFor
     * @return The instance, or null, if none is registered.
     */
    public <T> T getGenericInstance(Class<T> registeredFor);

}
