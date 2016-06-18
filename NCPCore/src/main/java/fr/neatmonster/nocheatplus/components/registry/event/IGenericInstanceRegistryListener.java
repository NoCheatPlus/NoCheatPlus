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
 * Receive Notifications about generic instance registry events. Listeners may
 * be possible to register even before an actual instance has been registered
 * (and even before it's known to the registry that anything is intended to be
 * registered) - subject to change.
 * 
 * @author asofold
 *
 */
public interface IGenericInstanceRegistryListener<T> {

    // TODO: <? extends T> ?

    /**
     * Registration, without an entry being present.
     * 
     * @param registerFor
     * @param instance
     *            Might be null, if the registry allows that.
     */
    public void onGenericInstanceRegister(Class<T> registerFor, T instance);

    /**
     * An already registered entry gets overridden.
     * 
     * @param registerFor
     * @param newInstance
     *            The instance that just got registered. Might be null, if the
     *            registry allows that.
     * @param oldInstance
     *            The instance that had been registered before. Might be null,
     *            if the registry allows that.
     */
    public void onGenericInstanceOverride(Class<T> registerFor, T newInstance, T oldInstance);

    /**
     * A registration is removed explicitly.
     * 
     * @param registerFor
     * @param oldInstance
     *            The instance that had been registered before. Might be null,
     *            if the registry allows that.
     */
    public void onGenericInstanceRemove(Class<T> registerFor, T oldInstance);

}
