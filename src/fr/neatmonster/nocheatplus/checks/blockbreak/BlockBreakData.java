package fr.neatmonster.nocheatplus.checks.blockbreak;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.ACheckData;
import fr.neatmonster.nocheatplus.checks.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;
import fr.neatmonster.nocheatplus.utilities.Stats;

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
public class BlockBreakData extends ACheckData {

	/** The factory creating data. */
	public static final CheckDataFactory factory = new CheckDataFactory() {
		@Override
		public final ICheckData getData(final Player player) {
			return BlockBreakData.getData(player);
		}

		@Override
		public ICheckData removeData(final String playerName) {
			return BlockBreakData.removeData(playerName);
		}
	};

    /** The map containing the data per players. */
    private static final Map<String, BlockBreakData> playersMap = new HashMap<String, BlockBreakData>();

	/**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static BlockBreakData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new BlockBreakData(BlockBreakConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
		return playersMap.remove(playerName);
	}

	// Violation levels.
    public double  directionVL;
    public double  fastBreakVL;
    public double  frequencyVL;
    public double  noSwingVL;
    public double  reachVL;
    public final ActionFrequency  wrongBlockVL;
    
    // Shared data.
	public int clickedX;
	public int clickedY;
	public int clickedZ;
	
	public final Stats stats;

    // Data of the fast break check.
	public final ActionFrequency fastBreakPenalties;
    public int     fastBreakBuffer;
    public long    fastBreakBreakTime  = System.currentTimeMillis() - 1000L;
    /** Old check sets this to the last interact time, new check sets to first interact time for one block. */
    public long    fastBreakDamageTime = System.currentTimeMillis();
    
    public final ActionFrequency frequencyBuckets;

    // Data of the no swing check.
    public boolean noSwingArmSwung     = true;

    // Data of the reach check.
    public double  reachDistance;
    

    public BlockBreakData(final BlockBreakConfig cc) {
		stats = cc.fastBreakDebug?(new Stats("NCP/FASTBREAK")):null;
		fastBreakPenalties = cc.fastBreakCheck ? new ActionFrequency(cc.fastBreakBuckets, cc.fastBreakBucketDur) : null;
		frequencyBuckets = cc.frequencyCheck ? new ActionFrequency(cc.frequencyBuckets, cc.frequencyBucketDur) : null;
		wrongBlockVL = cc.wrongBlockCheck ? new ActionFrequency(6, 20000) : null;
	}

}
