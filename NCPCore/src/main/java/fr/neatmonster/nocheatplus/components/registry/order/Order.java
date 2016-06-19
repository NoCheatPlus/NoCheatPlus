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

import java.util.Comparator;

/**
 * Utilities for sorting out order.
 * @author asofold
 *
 */
public class Order {

    /**
     * Comparator for sorting SetupOrder.
     */
    public static Comparator<Object> cmpSetupOrder = new Comparator<Object>() {
        @Override
        public int compare(final Object obj1, final Object obj2) {
            int prio1 = 0;
            int prio2 = 0;
            final SetupOrder order1 = obj1.getClass().getAnnotation(SetupOrder.class);
            if (order1 != null) {
                prio1 = order1.priority();
            }
            final SetupOrder order2 = obj2.getClass().getAnnotation(SetupOrder.class);
            if (order2 != null) {
                prio2 = order2.priority();
            }
            if (prio1 < prio2) {
                return -1;
            }
            else if (prio1 == prio2){
                return 0;
            }
            else {
                return 1;
            }
        }
    };

}
