package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

/*
 * M#"""""""'M  dP                   dP       MM"""""""`YM dP                            
 * ##  mmmm. `M 88                   88       MM  mmmmm  M 88                            
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M'        .M 88 .d8888b. .d8888b. .d8888b. 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   MM  MMMMMMMM 88 88'  `88 88'  `"" 88ooood8 
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. MM  MMMMMMMM 88 88.  .88 88.  ... 88.  ... 
 * M#       .;M dP `88888P' `88888P' dP   `YP MM  MMMMMMMM dP `88888P8 `88888P' `88888P' 
 * M#########M                                MMMMMMMMMMMM                               
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
 * Player specific data for the block place checks.
 */
public class BlockPlaceData {

    /** The map containing the data per players. */
    private static Map<String, BlockPlaceData> playersMap = new HashMap<String, BlockPlaceData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static BlockPlaceData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new BlockPlaceData());
        return playersMap.get(player.getName());
    }

    // Violation levels.
    public double  directionVL;
    public double  fastPlaceVL;
    public double  reachVL;
    public double  speedVL;

    // Data of the fast place check.
    public long    fastPlaceLastTime;
    public boolean fastPlaceLastRefused;

    // Data of the reach check.
    public double  reachDistance;

    // Data of the speed check;
    public boolean speedLastRefused;
    public long    speedLastTime;
}
