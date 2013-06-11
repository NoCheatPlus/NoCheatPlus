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
    
    /** For adaption to server side lag. */
    public final boolean lag;
    
    /**
     * 
     * @param config
     * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
     */
    public ACheckConfig(final ConfigFile config, final String pathPrefix){
    	// TODO: Path prefix construction is somewhat inconsistent with debug hierarchy ?
        debug = config.getBoolean(pathPrefix + ConfPaths.SUB_DEBUG, config.getBoolean(ConfPaths.CHECKS_DEBUG, false));
        // TODO: Use lag flag where appropriate and document it (or get rid of it).
        lag = config.getBoolean(pathPrefix + ConfPaths.SUB_LAG, true) && config.getBoolean(ConfPaths.MISCELLANEOUS_LAG, true);
    }
    
	@Override
	public String[] getCachePermissions() {
		return null;
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
