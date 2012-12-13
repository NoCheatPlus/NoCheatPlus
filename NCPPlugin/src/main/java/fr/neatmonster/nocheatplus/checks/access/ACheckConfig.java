package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.config.ConfigFile;


/**
 * Minimal implementation, doing nothing.
 * @author mc_dev
 *
 */
public abstract class ACheckConfig implements ICheckConfig {

    /** For on the fly debug setting. */
    public boolean debug = false;
    
    /**
     * 
     * @param config
     * @param pathPrefix Path prefix for the check section (example for use: prefix+"debug").
     */
    public ACheckConfig(final ConfigFile config, final String pathPrefix){
        debug = config.getBoolean(pathPrefix + "debug", false);
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
