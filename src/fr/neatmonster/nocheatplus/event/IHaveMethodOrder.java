package fr.neatmonster.nocheatplus.event;

import fr.neatmonster.nocheatplus.event.GenericListener.MethodEntry.MethodOrder;

/**
 * Implement to register Listeners via delegation that does not allow for passinf MethodOrder directly.
 * @author mc_dev
 *
 */
public interface IHaveMethodOrder {
	/**
	 * 
	 * @return
	 */
	public MethodOrder getMethodOrder();
}
