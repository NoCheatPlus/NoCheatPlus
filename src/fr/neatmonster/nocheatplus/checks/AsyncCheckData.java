package fr.neatmonster.nocheatplus.checks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ChecData for async checks like chat, actually implementing cached permissions.
 * @author mc_dev
 *
 */
public class AsyncCheckData extends ACheckData {

	// TODO: consider using a PermissionEntry class with a timestamp to schedule renewing it.
	
	// TODO: consider using a normal HashMap and ensure by contract that the permissions get filled at login, so updates are thread safe.
	
	/** The permissions that are actually cached. */
	protected final Map<String, Boolean> cachedPermissions = Collections.synchronizedMap(new HashMap<String, Boolean>());
	
	@Override
	public boolean hasCachedPermissionEntry(final String permission) {
		return cachedPermissions.containsKey(permission);
	}

	@Override
	public boolean hasCachedPermission(final String permission) {
		final Boolean has = cachedPermissions.get(permission);
		return (has == null) ? false : has.booleanValue();
	}
	
	@Override
	public void setCachedPermission(final String permission, final boolean has){
		cachedPermissions.put(permission, has);
	}
	
}
