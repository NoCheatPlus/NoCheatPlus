package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * This interface must be implemented by all configuration classes.
 * 
 * @author asofold
 */
public interface ICheckConfig {

    /**
     * Checks if a check is enabled.
     * 
     * @param checkType
     *            the check type
     * @return true, if the check is enabled
     */
    public boolean isEnabled(CheckType checkType);
    
    /** On the fly debug flags, to be set by commands and similar. */
    public boolean getDebug();
    
    /** On the fly debug flags, to be set by commands and similar. */ 
    public void setDebug(boolean debug);
    
    /**
     * Retrieve the permissions that have to be updated for this check.
     * @return An array of permissions, may be null.
     */
    public String[] getCachePermissions();

}
