package fr.neatmonster.nocheatplus.checks.blockbreak;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.checks.CheckDataFactory;

/*
 * M#"""""""'M  dP                   dP       M#"""""""'M                             dP       
 * ##  mmmm. `M 88                   88       ##  mmmm. `M                            88       
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  #'        .M 88d888b. .d8888b. .d8888b. 88  .dP  
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   M#  MMMb.'YM 88'  `88 88ooood8 88'  `88 88888"   
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. M#  MMMM'  M 88       88.  ... 88.  .88 88  `8b. 
 * M#       .;M dP `88888P' `88888P' dP   `YP M#       .;M dP       `88888P' `88888P8 dP   `YP 
 * M#########M                                M#########M                                      
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
 * Player specific data for the block break checks.
 */
public class BlockBreakData implements CheckData {

    /** The factory creating data. */
    public static final CheckDataFactory       factory    = new CheckDataFactory() {
                                                              @Override
                                                              public final CheckData getData(final Player player) {
                                                                  return BlockBreakData.getData(player);
                                                              }
                                                          };

    /** The map containing the data per players. */
    private static Map<String, BlockBreakData> playersMap = new HashMap<String, BlockBreakData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static BlockBreakData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new BlockBreakData());
        return playersMap.get(player.getName());
    }

    // Violation levels.
    public double  directionVL;
    public double  fastBreakVL;
    public double  noSwingVL;
    public double  reachVL;

    // Data of the fast break check.
    public int     fastBreakBuffer     = 5;
    public long    fastBreakBreakTime  = System.currentTimeMillis() - 1000L;
    public long    fastBreakDamageTime = System.currentTimeMillis();

    // Data of the no swing check.
    public boolean noSwingArmSwung;

    // Data of the reach check.
    public double  reachDistance;

}
