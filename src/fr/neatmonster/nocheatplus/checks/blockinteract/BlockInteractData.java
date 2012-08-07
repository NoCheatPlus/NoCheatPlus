package fr.neatmonster.nocheatplus.checks.blockinteract;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

/*
 * M#"""""""'M  dP                   dP       M""M            dP                                         dP   
 * ##  mmmm. `M 88                   88       M  M            88                                         88   
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M  M 88d888b. d8888P .d8888b. 88d888b. .d8888b. .d8888b. d8888P 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   M  M 88'  `88   88   88ooood8 88'  `88 88'  `88 88'  `""   88   
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. M  M 88    88   88   88.  ... 88       88.  .88 88.  ...   88   
 * M#       .;M dP `88888P' `88888P' dP   `YP M  M dP    dP   dP   `88888P' dP       `88888P8 `88888P'   dP   
 * M#########M                                MMMM                                                            
 * 
 * M""""""'YMM            dP            
 * M  mmmm. `M            88            
 * M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  MMMMM  M 88'  `88   88   88'  `88 
 * M  MMMM' .M 88.  .88   88   88.  .88 
 * M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMM                          
 */
/**
 * Player specific data for the block interact checks.
 */
public class BlockInteractData {

    /** The map containing the data per players. */
    private static Map<String, BlockInteractData> playersMap = new HashMap<String, BlockInteractData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static BlockInteractData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new BlockInteractData());
        return playersMap.get(player.getName());
    }

    // Violation levels.
    public double  directionVL;
    public double  noSwingVL;
    public double  reachVL;

    // Data of the no swing check.
    public boolean noSwingArmSwung;
    public long    noSwingLastTime;

    // Data of the reach check.
    public double  reachDistance;
}
