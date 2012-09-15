package fr.neatmonster.nocheatplus.checks.access;

/**
 * CheckConfig for async checks such as chat, adding permissions to cache.
 * @author mc_dev
 *
 */
public abstract class AsyncCheckConfig extends ACheckConfig {
	
	/** Permissions to hold in player data cache, not final for flexibility. */
	protected String[] cachePermissions;

	/**
	 * 
	 * @param cachePermissions Permissions to hold in player data cache.
	 */
	public AsyncCheckConfig(String[] cachePermissions){
		this.cachePermissions = cachePermissions;
	}
	
	@Override
	public String[] getCachePermissions() {
		return cachePermissions;
	}

}
