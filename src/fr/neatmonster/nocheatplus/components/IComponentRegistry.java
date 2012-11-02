package fr.neatmonster.nocheatplus.components;

public interface IComponentRegistry {
	/**
     * Convenience method to add components according to implemented interfaces,
     * like Listener, INotifyReload, INeedConfig.<br>
     * For the NoCheatPlus instance this must be done after the configuration has been initialized.
     * @param obj
     */
	public void addComponent(final Object onj);
	
	/**
	 * Remove a registered component. <br>
	 * Does not unregister listeners currently.
	 * @param obj
	 */
	public void removeComponent(final Object obj);
}
