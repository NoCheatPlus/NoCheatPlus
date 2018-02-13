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

import fr.neatmonster.nocheatplus.components.registry.exception.RegistrationLockedException;

/**
 * A registry for unique instances of any class type.<br>
 * Currently there is no specification for what happens with registering for an
 * already registered class, neither if exceptions are thrown, nor if
 * dependencies will use those then.
 * <hr>
 * Note getters: {@link IGetGenericInstance}
 * 
 * @author asofold
 *
 */
public interface GenericInstanceRegistry extends IGetGenericInstance, IGetGenericInstanceHandle {

    /**
     * Register the instance by its own class. This demands type parameters to
     * be aligned to the actual class.
     * 
     * @param instance
     * @throws RegistrationLockedException
     *             If the registration of the class of instance is locked.
     */
    public <T> T registerGenericInstance(T instance);

    /**
     * Register an instance under for a super-class.
     * 
     * @param registerAs
     * @param instance
     * @return The previously registered instance. If none was registered, null
     *         is returned.
     * @throws RegistrationLockedException
     *             If the registration of registerFor is locked.
     */
    public <T, TI extends T> T registerGenericInstance(Class<T> registerFor, TI instance);

    /**
     * Remove a registration. The registry implementation might specify id
     * removing is allowed.
     * 
     * @param registeredFor
     * @return The previously registered instance. If none was registered, null
     *         is returned.
     * @throws RegistrationLockedException
     *             If the registration of registerFor is locked.
     */
    public <T> T unregisterGenericInstance(Class<T> registeredFor);

}
