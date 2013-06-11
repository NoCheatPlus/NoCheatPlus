package fr.neatmonster.nocheatplus.checks.blockplace;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

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
 * Player specific dataFactory for the block place checks.
 */
public class BlockPlaceData extends ACheckData {

	/** The factory creating data. */
	public static final CheckDataFactory factory = new CheckDataFactory() {
		@Override
		public final ICheckData getData(final Player player) {
			return BlockPlaceData.getData(player);
		}

		@Override
		public ICheckData removeData(final String playerName) {
			return BlockPlaceData.removeData(playerName);
		}

		@Override
		public void removeAllData() {
			clear();
		}
	};

    /** The map containing the data per players. */
    private static final Map<String, BlockPlaceData> playersMap = new HashMap<String, BlockPlaceData>();

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

    public static ICheckData removeData(final String playerName) {
		return playersMap.remove(playerName);
	}
    
    public static void clear(){
    	playersMap.clear();
    }

	// Violation levels.
	public double autoSignVL = 0;
	public double directionVL = 0;
	public double fastPlaceVL = 0;
	public double noSwingVL = 0;
	public double reachVL = 0;
	public double speedVL = 0;
    
    // AutoSign.
    public long autoSignPlacedTime = 0;
    public long autoSignPlacedHash = 0;
    
    // Data of the fast place check.
    public final ActionFrequency fastPlaceBuckets	= new ActionFrequency(2, 1000);
    public int fastPlaceShortTermTick				= 0;
    public int fastPlaceShortTermCount				= 0;

    // Data of the no swing check.
    public boolean noSwingArmSwung = true;

    // Data of the reach check.
    public double  reachDistance;

    // Data of the speed check;
    public boolean speedLastRefused;
    public long    speedLastTime;
}
