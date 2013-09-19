package fr.neatmonster.nocheatplus.components;

/**
 * Component to listen to plugin/onDisable.
 * @author mc_dev
 *
 */
public interface DisableListener {
	/**
	 * Called in the plugin in onDisable, before unregistration of all components.
	 */
	public void onDisable();
}
