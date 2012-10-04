package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.config.ConfigFile;

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
	 * @param config
	 * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
	 * @param cachePermissions  cachePermissions Permissions to hold in player data cache.
	 */
	public AsyncCheckConfig(final ConfigFile config, final String pathPrefix, final String[] cachePermissions){
	    super(config, pathPrefix);
		this.cachePermissions = cachePermissions;
	}
	
	@Override
	public String[] getCachePermissions() {
		return cachePermissions;
	}

}
