package fr.neatmonster.nocheatplus.components;

/**
 * A ComponentRegistry allows registering components, that then are delegated to where they belong.<br>
 * Notes:
 * <li>Implementations should somehow specify when components can be registered and when they are unregistered automatically.</li>
 * <li>Implementations should somehow specify what components can be registered.</li>
 * @author mc_dev
 *
 */
public interface ComponentRegistry {
	/**
     * Convenience method to add components according to implemented interfaces,
     * like Listener, INotifyReload, INeedConfig.<br>
     * For the NoCheatPlus instance this must be done after the configuration has been initialized.
     * @param obj
     */
	public void addComponent(final Object obj);
	
	/**
	 * Remove a registered component. <br>
	 * Does not unregister listeners currently.
	 * @param obj
	 */
	public void removeComponent(final Object obj);
}
