package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Minimal implementation, doing nothing.
 * @author mc_dev
 *
 */
public abstract class ACheckConfig implements ICheckConfig {

    /** For on the fly debug setting. */
    public boolean debug = false;
    
    /** If to adapt to server side lag. */
    public final boolean lag;
    
    /** Permissions to hold in player data cache, not final for flexibility. */
    protected String[] cachePermissions;
    
    /**
     * 
     * @param config
     * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
     */
    public ACheckConfig(final ConfigFile config, final String pathPrefix){
        this(config, pathPrefix, null);
        }
    
    /**
     * 
     * @param config
     * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
     * @param cachePermissions  cachePermissions Permissions to hold in player data cache. Can be null.
     */
    public ACheckConfig(final ConfigFile config, final String pathPrefix, final String[] cachePermissions){
        // TODO: Path prefix construction is somewhat inconsistent with debug hierarchy ?
        debug = config.getBoolean(pathPrefix + ConfPaths.SUB_DEBUG, config.getBoolean(ConfPaths.CHECKS_DEBUG, false));
        // TODO: Use lag flag where appropriate and document it (or get rid of it).
        lag = config.getBoolean(pathPrefix + ConfPaths.SUB_LAG, true) && config.getBoolean(ConfPaths.MISCELLANEOUS_LAG, true);
        this.cachePermissions = cachePermissions;
    }
    
	@Override
	public String[] getCachePermissions() {
		return cachePermissions;
	}

    @Override
    public boolean getDebug() {
        return debug;
    }

    @Override
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }
	
}
