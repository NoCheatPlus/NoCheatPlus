package fr.neatmonster.nocheatplus.components;


/**
 * Permission cache. Allow to query defaultPermissions (a defined set of defaultPermissions), to be registered and automatically be updated, according to registry.<br>
 * The defaultPermissions are not updated in real time but on certain events, to be specified by the registry.
 * 
 * @author mc_dev
 *
 */
public interface PermStateHolder {
	
	/**
	 * Get the defaultPermissions that are guaranteed to be held here.
	 * @return
	 */
	public String[] getDefaultPermissions();
	
	/**
	 * Test a permission. If not available the result will be false, no updating of defaultPermissions is expected on calling this.
	 * @param player
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(String player, String permission);
}
