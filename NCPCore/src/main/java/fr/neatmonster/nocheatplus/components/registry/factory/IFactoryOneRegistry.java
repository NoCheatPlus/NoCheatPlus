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
package fr.neatmonster.nocheatplus.components.registry.factory;

public interface IFactoryOneRegistry<A> {

    /**
     * Register a factory.
     * 
     * @param registerFor
     * @param factory
     */
    public <T> void registerFactory(final Class<T> registerFor, 
            final IFactoryOne<A, T> factory);

    /**
     * Fetch a new instance from a registered factory.
     * 
     * @param registeredFor
     * @param arg
     * @return
     * @throws RuntimeException
     *             (might get changed to a registry type of exception), in case
     *             a factory throws something-
     */
    public <T> T getNewInstance(final Class<T> registeredFor, final A arg);

}
