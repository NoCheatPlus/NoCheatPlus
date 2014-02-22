package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.components.IData;

/**
 * This is for future purposes. Might remove...<br>
 * Some checks in chat synchronize over data, so using this from exectueActions can deadlock.<br>
 * One might think of making this an interface not for the internally used data, but for copy of data for external use
 * only. Then sync could go over other objects for async access.
 * 
 * @author asofold
 */
public interface ICheckData extends IData{
	
	/**
	 * Check if an entry for the given permission exists.
	 * @param permission
	 * @return
	 */
	public boolean hasCachedPermissionEntry(String permission);
	/**
	 * Check if the user has the permission. If no entry is present, a false result is assumed an after failure check is made and the cache must be registered for updating.
	 * @param permission
	 * @return
	 */
	public boolean hasCachedPermission(String permission);
	
	/**
	 * Set a cached permission.
	 * @param permission
	 * @param value
	 */
	public void setCachedPermission(String permission, boolean value);
}
