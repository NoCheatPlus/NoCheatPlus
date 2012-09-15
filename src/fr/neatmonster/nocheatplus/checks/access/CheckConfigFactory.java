package fr.neatmonster.nocheatplus.checks.access;

import org.bukkit.entity.Player;


/*
 * MM'""""'YMM dP                         dP       MM'""""'YMM                   .8888b oo          
 * M' .mmm. `M 88                         88       M' .mmm. `M                   88   "             
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. 88  .dP  M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `"" 88888"   M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * M. `MMM' .M 88    88 88.  ... 88.  ... 88  `8b. M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * MM.     .dM dP    dP `88888P' `88888P' dP   `YP MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMM                                     MMMMMMMMMMM                                  .88 
 *                                                                                          d8888P  
 * MM""""""""`M                     dP                              
 * MM  mmmmmmmM                     88                              
 * M'      MMMM .d8888b. .d8888b. d8888P .d8888b. 88d888b. dP    dP 
 * MM  MMMMMMMM 88'  `88 88'  `""   88   88'  `88 88'  `88 88    88 
 * MM  MMMMMMMM 88.  .88 88.  ...   88   88.  .88 88       88.  .88 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   `88888P' dP       `8888P88 
 * MMMMMMMMMMMM                                                 .88 
 *                                                          d8888P  
 */
/**
 * A factory for creating and accessing configurations.
 * 
 * @author asofold
 */
public interface CheckConfigFactory {

    /**
     * Gets the configuration for a specified player.
     * 
     * @param player
     *            the player
     * @return the configuration
     */
    public ICheckConfig getConfig(Player player);

}
