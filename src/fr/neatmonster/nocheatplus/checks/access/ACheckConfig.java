package fr.neatmonster.nocheatplus.checks.access;


/**
 * Minimal implementation, doing nothing.
 * @author mc_dev
 *
 */
public abstract class ACheckConfig implements ICheckConfig {

	@Override
	public String[] getCachePermissions() {
		return null;
	}



}
