package fr.neatmonster.nocheatplus.checks;

import org.bukkit.entity.Player;

/*
 * MM'""""'YMM dP                         dP       M""""""'YMM            dP            
 * M' .mmm. `M 88                         88       M  mmmm. `M            88            
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. 88  .dP  M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `"" 88888"   M  MMMMM  M 88'  `88   88   88'  `88 
 * M. `MMM' .M 88    88 88.  ... 88.  ... 88  `8b. M  MMMM' .M 88.  .88   88   88.  .88 
 * MM.     .dM dP    dP `88888P' `88888P' dP   `YP M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMM                                     MMMMMMMMMMM                          
 * 
 *  * MM""""""""`M                     dP                              
 * MM  mmmmmmmM                     88                              
 * M'      MMMM .d8888b. .d8888b. d8888P .d8888b. 88d888b. dP    dP 
 * MM  MMMMMMMM 88'  `88 88'  `""   88   88'  `88 88'  `88 88    88 
 * MM  MMMMMMMM 88.  .88 88.  ...   88   88.  .88 88       88.  .88 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   `88888P' dP       `8888P88 
 * MMMMMMMMMMMM                                                 .88 
 *                                                          d8888P  
 */
/**
 * A factory for creating and accessing data.
 * 
 * @author asofold
 */
public interface CheckDataFactory {

    /**
     * Gets the data of the specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public CheckData getData(final Player player);

}
