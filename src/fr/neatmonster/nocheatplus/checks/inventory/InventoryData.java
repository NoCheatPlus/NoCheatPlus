package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.checks.CheckDataFactory;

/*
 * M""M                                       dP                              M""""""'YMM            dP            
 * M  M                                       88                              M  mmmm. `M            88            
 * M  M 88d888b. dP   .dP .d8888b. 88d888b. d8888P .d8888b. 88d888b. dP    dP M  MMMMM  M .d8888b. d8888P .d8888b. 
 * M  M 88'  `88 88   d8' 88ooood8 88'  `88   88   88'  `88 88'  `88 88    88 M  MMMMM  M 88'  `88   88   88'  `88 
 * M  M 88    88 88 .88'  88.  ... 88    88   88   88.  .88 88       88.  .88 M  MMMM' .M 88.  .88   88   88.  .88 
 * M  M dP    dP 8888P'   `88888P' dP    dP   dP   `88888P' dP       `8888P88 M       .MM `88888P8   dP   `88888P8 
 * MMMM                                                                   .88 MMMMMMMMMMM                          
 *                                                                    d8888P                                       
 */
/**
 * Player specific dataFactory for the inventory checks.
 */
public class InventoryData implements CheckData {
	
	public static final CheckDataFactory factory = new CheckDataFactory(){
		@Override
		public final CheckData getData(final Player player) {
			return InventoryData.getData(player);
		}
	};

    /** The map containing the dataFactory per players. */
    private static Map<String, InventoryData> playersMap = new HashMap<String, InventoryData>();

    /**
     * Gets the dataFactory of a specified player.
     * 
     * @param player
     *            the player
     * @return the dataFactory
     */
    public static InventoryData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new InventoryData());
        return playersMap.get(player.getName());
    }

    // Violation levels.
    public double   dropVL;
    public double   instantBowVL;
    public double   instantEatVL;

    // Data of the drop check.
    public int      dropCount;
    public long     dropLastTime;

    // Data of the instant bow check.
    public long     instantBowLastTime;

    // Data of the instant eat check.
    public Material instantEatFood;
    public long     instantEatLastTime;
}
