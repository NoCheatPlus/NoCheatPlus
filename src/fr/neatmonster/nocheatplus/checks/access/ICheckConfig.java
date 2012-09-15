package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * MM'""""'YMM dP                         dP       MM'""""'YMM                   .8888b oo          
 * M' .mmm. `M 88                         88       M' .mmm. `M                   88   "             
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. 88  .dP  M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `"" 88888"   M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * M. `MMM' .M 88    88 88.  ... 88.  ... 88  `8b. M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * MM.     .dM dP    dP `88888P' `88888P' dP   `YP MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMM                                     MMMMMMMMMMM                                  .88 
 *                                                                                          d8888P  
 */
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
    
    /**
     * Retrieve the permissions that have to be updated for this check.
     * @return An array of permissions, may be null.
     */
    public String[] getCachePermissions();

}
