package fr.neatmonster.nocheatplus.components;


/**
 * Permission cache. Allow to query permissions (a defined set of permissions), to be registered and automatically be updated, according to registry.<br>
 * The permissions are not updated in real time but on certain events, to be specified by the registry.
 * 
 * @author mc_dev
 *
 */
public interface PermStateHolder {
	
	/**
	 * Get the default permissions that are guaranteed to be held here.
	 * @return
	 */
	public String[] getDefaultPermissions();
	
	/**
	 * Test a permission. If not available the result will be false, no updating of permissions is expected on calling this.
	 * @param player
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(String player, String permission);
}
