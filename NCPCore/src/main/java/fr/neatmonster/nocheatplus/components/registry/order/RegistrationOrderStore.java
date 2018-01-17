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
package fr.neatmonster.nocheatplus.components.registry.order;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an auxiliary class that allows more convenient implementations of
 * IRegisterWithOrder, it's not really meant to be registered anywhere. Methods
 * for adding default and mappings are provided for chaining.
 * 
 * @author asofold
 *
 */
public class RegistrationOrderStore implements IRegisterWithOrder {
    // (No generics this time.)

    private final Map<Class<?>, RegistrationOrder> orderMap = new HashMap<Class<?>, RegistrationOrder>();
    private RegistrationOrder defaultOrder = null;

    public RegistrationOrderStore() {
    }

    /**
     * Set the default order to apply when no mapping is present for a given
     * key.
     * 
     * @param order
     *            Allows setting to null. The default order applies, if no
     *            mapping is present. It does not apply if a mapping to null is
     *            present.
     * @return
     */
    public RegistrationOrderStore defaultOrder(RegistrationOrder order) {
        this.defaultOrder = order;
        return this;
    }

    /**
     * Set a mapping from type to register for and order.
     * 
     * @param registerForType
     * @param order
     *            Can be set to null, to prevent the return of the defaultOrder.
     * @return
     */
    public RegistrationOrderStore order(Class<?> registerForType, RegistrationOrder order) {
        this.orderMap.put(registerForType, order);
        return this;
    }

    /**
     * Convenience: set up defaultOrder with the constructor.
     * 
     * @param defaultOrder
     */
    public RegistrationOrderStore(RegistrationOrder order) {
        this.defaultOrder = order;
    }

    @Override
    public RegistrationOrder getRegistrationOrder(final Class<?> registerForType) {
        // Prefer set null entries over the default type.
        return orderMap.containsKey(registerForType) ? orderMap.get(registerForType) : defaultOrder;
    }

    /**
     * Remove all mappings and set the defaultOrder to null.
     */
    public void clear() {
        orderMap.clear();
        defaultOrder = null;
    }

}
