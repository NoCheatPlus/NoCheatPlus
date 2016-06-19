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
 * Convenience to retrieve the currently registered instance. Note that
 * registrations by other plugins might be problematic, thus removing
 * registrations and stored IGenericInstanceHandle instances is within the
 * responsibility of the hooking plugin.
 * 
 * @author asofold
 *
 * @param <T>
 *            The type instances are registered for.
 */
public interface IGenericInstanceHandle<T> extends IHandle<T> {

    // TODO: <? extends T> ?

    /**
     * Get the currently registered instance.
     * 
     * @return
     * @throws RuntimeException,
     *             if disableHandle has been called.
     */
    public T getHandle();

    /**
     * Unlink from the registry. Subsequent calls to getHandle will yield a
     * RuntimeException, while disableHandle can still be called without effect.
     * This may not be necessary, if the registration lasts during an entire
     * runtime, however if an object that holds IGenericInstanceHandle instances
     * gets overridden on reloading the configuration of the plugin, keeping
     * handles may leak a little bit of memory and increase CPU load with each
     * time such happens. Often changing registration is not a typical use-case.
     * This can not be undone.
     */
    public void disableHandle();

}
