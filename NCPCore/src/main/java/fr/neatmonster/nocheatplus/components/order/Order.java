package fr.neatmonster.nocheatplus.components.order;

import java.util.Comparator;

/**
 * Utilities for sorting out order.
 * @author mc_dev
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
