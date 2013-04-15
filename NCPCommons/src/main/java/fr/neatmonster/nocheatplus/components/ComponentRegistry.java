package fr.neatmonster.nocheatplus.components;

/**
 * A ComponentRegistry allows registering components, that then should be delegated to where they belong or just be ignored.<br>
 * Notes:
 * <li>Implementations should somehow specify what components can be registered.</li>
 * <li>Implementations should somehow specify if/when they are unregistered automatically.</li>
 * @author mc_dev
 *
 */
public interface ComponentRegistry<T>{
	
	/**
     * Register a component.
     * @param component
	 * @return If (newly) added. Adding an already present component should do nothing.
     */
	public boolean addComponent(final T component);
	
	/**
	 * Remove a registered component. <br>
	 * Does not unregister listeners currently.
	 * @param component
	 */
	public void removeComponent(final T component);
}
