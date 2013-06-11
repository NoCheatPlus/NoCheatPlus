package fr.neatmonster.nocheatplus.checks.blockbreak;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
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

		@Override
		public void removeAllData() {
			clear();
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
    
    public static void clear(){
    	playersMap.clear();
    }

	// Violation levels.
    public double  directionVL;
    public double  fastBreakVL;
    public double  frequencyVL;
    public double  noSwingVL;
    public double  reachVL;
    public final ActionFrequency  wrongBlockVL;
    
    // Shared data.
	public int     clickedX;
	public int     clickedY;
	public int     clickedZ;
	public int     clickedTick;
	
	// TODO: use tick here too  ?
	public long    wasInstaBreak;
	
	public final Stats stats;

    // Data of the fast break check.
	public final ActionFrequency fastBreakPenalties;
    public int     fastBreakBuffer;
    public long    fastBreakBreakTime  = System.currentTimeMillis() - 1000L;
    /** First time interaction with a block. */
    public long    fastBreakfirstDamage = System.currentTimeMillis();
    
    public final ActionFrequency frequencyBuckets;
	public int     frequencyShortTermCount;
	public int     frequencyShortTermTick;

    // Data of the no swing check.
    public boolean noSwingArmSwung     = true;

    // Data of the reach check.
    public double  reachDistance;
    

    public BlockBreakData(final BlockBreakConfig cc) {
		stats = cc.fastBreakDebug?(new Stats("NCP/FASTBREAK")):null;
		fastBreakPenalties = new ActionFrequency(cc.fastBreakBuckets, cc.fastBreakBucketDur);
		frequencyBuckets = new ActionFrequency(cc.frequencyBuckets, cc.frequencyBucketDur);
		wrongBlockVL = new ActionFrequency(6, 20000);
	}

}
