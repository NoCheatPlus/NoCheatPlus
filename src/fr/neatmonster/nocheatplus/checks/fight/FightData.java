package fr.neatmonster.nocheatplus.checks.fight;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

/*
 * MM""""""""`M oo          dP         dP   M""""""'YMM            dP            
 * MM  mmmmmmmM             88         88   M  mmmm. `M            88            
 * M'      MMMM dP .d8888b. 88d888b. d8888P M  MMMMM  M .d8888b. d8888P .d8888b. 
 * MM  MMMMMMMM 88 88'  `88 88'  `88   88   M  MMMMM  M 88'  `88   88   88'  `88 
 * MM  MMMMMMMM 88 88.  .88 88    88   88   M  MMMM' .M 88.  .88   88   88.  .88 
 * MM  MMMMMMMM dP `8888P88 dP    dP   dP   M       .MM `88888P8   dP   `88888P8 
 * MMMMMMMMMMMM         .88                 MMMMMMMMMMM                          
 *                  d8888P                                                       
 */
/**
 * Player specific data for the fight checks.
 */
public class FightData extends ACheckData {

	/** The factory creating data. */
	public static final CheckDataFactory factory = new CheckDataFactory() {
		@Override
		public final ICheckData getData(final Player player) {
			return FightData.getData(player);
		}

		@Override
		public ICheckData removeData(final String playerName) {
			return FightData.removeData(playerName);
		}
	};

    /** The map containing the data per players. */
    private static final Map<String, FightData> playersMap = new HashMap<String, FightData>();

    /**
     * Gets the data of a specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public static FightData getData(final Player player) {
        if (!playersMap.containsKey(player.getName()))
            playersMap.put(player.getName(), new FightData(FightConfig.getConfig(player)));
        return playersMap.get(player.getName());
    }

    public static ICheckData removeData(final String playerName) {
		return playersMap.remove(playerName);
	}

	// Violation levels.
    public double                  angleVL;
    public double                  criticalVL;
    public double                  directionVL;
    public double                  godModeVL;
    public double                  instantHealVL;
    public double                  knockbackVL;
    public double                  noSwingVL;
    public double                  reachVL;
    public double                  speedVL;

    public boolean                 skipNext;
    
    public long                    damageTakenTick;
    
    // Shared
    
    public String lastWorld = "";

    // Data of the angle check.
    public TreeMap<Long, Location> angleHits = new TreeMap<Long, Location>();

    // Data of the direction check.
    public long                    directionLastViolationTime;

    // Data of the god mode check.
    public int                     godModeBuffer;
    public int                     godModeLastAge;
    public long                    godModeLastTime;

    // Data of the instant heal check.
    public long                    instantHealBuffer;
    public long                    instantHealLastTime;

    // Data of the knockback check.
    public long                    knockbackSprintTime;

    // Data of the no swing check.
    public boolean                 noSwingArmSwung;

    // Data of the reach check.
    public long                    reachLastViolationTime;
    
    // Data of the SelfHit check.
    public ActionFrequency selfHitVL = new ActionFrequency(6, 5000);

    // Data of the frequency check.
    public final ActionFrequency   speedBuckets;
	public int                     speedShortTermCount;
	public int                     speedShortTermTick;
	                   
	
	public FightData(final FightConfig cc){
		speedBuckets = new ActionFrequency(cc.speedBuckets, cc.speedBucketDur);
	}
}
