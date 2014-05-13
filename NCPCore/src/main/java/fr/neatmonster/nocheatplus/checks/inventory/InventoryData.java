package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

/**
 * Player specific dataFactory for the inventory checks.
 */
public class InventoryData extends ACheckData {

	/** The factory creating data. */
	public static final CheckDataFactory factory = new CheckDataFactory() {
		@Override
		public final ICheckData getData(final Player player) {
			return InventoryData.getData(player);
		}

		@Override
		public ICheckData removeData(final String playerName) {
			return InventoryData.removeData(playerName);
		}

		@Override
		public void removeAllData() {
			clear();
		}
	};

    /** The map containing the data per players. */
    private static final Map<String, InventoryData> playersMap = new HashMap<String, InventoryData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static InventoryData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new InventoryData());
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
		return playersMap.remove(playerName);
	}
    
    public static void clear(){
    	playersMap.clear();
    }

	// Violation levels.
    public double   dropVL;
    public double   fastClickVL;
    public double   instantBowVL;
    public double   instantEatVL;
    
    // General.
    public long     lastClickTime = 0;

    // Data of the drop check.
    public int      dropCount;
    public long     dropLastTime;

    // Data of the fast click check.
//    public boolean  fastClickLastCancelled;
    public final ActionFrequency fastClickFreq = new ActionFrequency(5, 200L);
	public Material fastClickLastCursor = null;
	public Material fastClickLastClicked = null;
	public int fastClickLastCursorAmount = 0;

    // Data of the instant bow check.
	/** Last time right click interact on bow. A value of 0 means 'invalid'.*/
    public long     instantBowInteract = 0;
    public long     instantBowShoot;

    // Data of the instant eat check.
    public Material instantEatFood;
    public long     instantEatInteract;

}
