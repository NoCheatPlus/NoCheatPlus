package fr.neatmonster.nocheatplus.checks.blockbreak;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/*
 * M#"""""""'M  dP                   dP       M#"""""""'M                             dP       
 * ##  mmmm. `M 88                   88       ##  mmmm. `M                            88       
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  #'        .M 88d888b. .d8888b. .d8888b. 88  .dP  
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   M#  MMMb.'YM 88'  `88 88ooood8 88'  `88 88888"   
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. M#  MMMM'  M 88       88.  ... 88.  .88 88  `8b. 
 * M#       .;M dP `88888P' `88888P' dP   `YP M#       .;M dP       `88888P' `88888P8 dP   `YP 
 * M#########M                                M#########M                                      
 * 
 * MM'""""'YMM                   .8888b oo          
 * M' .mmm. `M                   88   "             
 * M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMM                                  .88 
 *                                          d8888P  
 */
/**
 * Configurations specific for the block break checks. Every world gets one of these assigned to it, or if a world
 * doesn't get it's own, it will use the "global" version.
 */
public class BlockBreakConfig extends ACheckConfig {

	/** The factory creating configurations. */
	public static final CheckConfigFactory factory = new CheckConfigFactory() {
		@Override
		public final ICheckConfig getConfig(final Player player) {
			return BlockBreakConfig.getConfig(player);
		}
	};

    /** The map containing the configurations per world. */
    private static final Map<String, BlockBreakConfig> worldsMap = new HashMap<String, BlockBreakConfig>();

    /**
     * Clear all the configurations.
     */
    public static void clear() {
        worldsMap.clear();
    }

    /**
     * Gets the configuration for a specified player.
     * 
     * @param player
     *            the player
     * @return the configuration
     */
    public static BlockBreakConfig getConfig(final Player player) {
        if (!worldsMap.containsKey(player.getWorld().getName()))
            worldsMap.put(player.getWorld().getName(),
                    new BlockBreakConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
        return worldsMap.get(player.getWorld().getName());
    }

    public final boolean    directionCheck;
    public final ActionList directionActions;

    public final boolean    fastBreakCheck;
    public final boolean    fastBreakStrict;
	public final boolean    fastBreakDebug;
	public final int        fastBreakBuckets;
	public final long       fastBreakBucketDur;
	public final float      fastBreakBucketFactor;
	public final long       fastBreakGrace;
	public final long       fastBreakDelay;	
	public final int        fastBreakModSurvival;
	public final int        fastBreakModCreative;
    public final ActionList fastBreakActions;
    
    
	public final boolean    frequencyCheck;
	public final int        frequencyBuckets;
	public final long       frequencyBucketDur;
	public final float      frequencyBucketFactor;  
	public final int        frequencyIntervalCreative;
	public final int        frequencyIntervalSurvival;

    public final int        frequencyShortTermLimit;
	public final int        frequencyShortTermTicks;
	public final ActionList frequencyActions;
    
	public boolean          improbableFastBreakCheck;

    public final boolean    noSwingCheck;
    public final ActionList noSwingActions;

    public final boolean    reachCheck;
    public final ActionList reachActions;

    public final boolean    wrongBlockCheck;
	public final float      wrongBLockLevel;
	public final ActionList wrongBlockActions;
	
    /**
     * Instantiates a new block break configuration.
     * 
     * @param data
     *            the data
     */
    public BlockBreakConfig(final ConfigFile data) {
        super(data, ConfPaths.BLOCKBREAK);
        directionCheck = data.getBoolean(ConfPaths.BLOCKBREAK_DIRECTION_CHECK);
        directionActions = data.getOptimizedActionList(ConfPaths.BLOCKBREAK_DIRECTION_ACTIONS, Permissions.BLOCKBREAK_DIRECTION);

        // Fastbreak.
        fastBreakCheck = data.getBoolean(ConfPaths.BLOCKBREAK_FASTBREAK_CHECK);
        fastBreakStrict = data.getBoolean(ConfPaths.BLOCKBREAK_FASTBREAK_STRICT);
        fastBreakDebug = data.getBoolean(ConfPaths.BLOCKBREAK_FASTBREAK_DEBUG, false);
        fastBreakDelay = data.getLong(ConfPaths.BLOCKBREAK_FASTBREAK_DELAY);
        fastBreakGrace = Math.max(data.getLong(ConfPaths.BLOCKBREAK_FASTBREAK_BUCKETS_CONTENTION, 2000), data.getLong(ConfPaths.BLOCKBREAK_FASTBREAK_GRACE));
        fastBreakBucketDur = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_BUCKETS_DUR, 4000);
        fastBreakBucketFactor = (float) data.getDouble(ConfPaths.BLOCKBREAK_FASTBREAK_BUCKETS_FACTOR, 0.99);
        fastBreakBuckets = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_BUCKETS_N, 30);
        fastBreakModCreative = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_MOD_CREATIVE, 0);
        fastBreakModSurvival = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_MOD_SURVIVAL);
        // Fastbreak  actions, shared.
        fastBreakActions = data.getOptimizedActionList(ConfPaths.BLOCKBREAK_FASTBREAK_ACTIONS, Permissions.BLOCKBREAK_FASTBREAK);

        frequencyCheck = data.getBoolean(ConfPaths.BLOCKBREAK_FREQUENCY_CHECK);
        frequencyBuckets = data.getInt(ConfPaths.BLOCKBREAK_FREQUENCY_BUCKETS_N, 2);
        frequencyBucketDur = data.getLong(ConfPaths.BLOCKBREAK_FREQUENCY_BUCKETS_DUR, 1000);
        frequencyBucketFactor = (float) data.getDouble(ConfPaths.BLOCKBREAK_FREQUENCY_BUCKETS_FACTOR, 1f);
        frequencyIntervalCreative = data.getInt(ConfPaths.BLOCKBREAK_FREQUENCY_MOD_CREATIVE);
        frequencyIntervalSurvival = data.getInt(ConfPaths.BLOCKBREAK_FREQUENCY_MOD_SURVIVAL);
        frequencyShortTermLimit = data.getInt(ConfPaths.BLOCKBREAK_FREQUENCY_SHORTTERM_LIMIT);
    	frequencyShortTermTicks = data.getInt(ConfPaths.BLOCKBREAK_FREQUENCY_SHORTTERM_TICKS);
        frequencyActions = data.getOptimizedActionList(ConfPaths.BLOCKBREAK_FREQUENCY_ACTIONS, Permissions.BLOCKBREAK_FREQUENCY);
        
        noSwingCheck = data.getBoolean(ConfPaths.BLOCKBREAK_NOSWING_CHECK);
        noSwingActions = data.getOptimizedActionList(ConfPaths.BLOCKBREAK_NOSWING_ACTIONS, Permissions.BLOCKBREAK_NOSWING);

        reachCheck = data.getBoolean(ConfPaths.BLOCKBREAK_REACH_CHECK);
        reachActions = data.getOptimizedActionList(ConfPaths.BLOCKBREAK_REACH_ACTIONS, Permissions.BLOCKBREAK_REACH);
        
        wrongBlockCheck = data.getBoolean(ConfPaths.BLOCKBREAK_WRONGBLOCK_CHECK);
        wrongBLockLevel = data.getInt(ConfPaths.BLOCKBREAK_WRONGBLOCK_LEVEL);
        wrongBlockActions = data.getOptimizedActionList(ConfPaths.BLOCKBREAK_WRONGBLOCK_ACTIONS, Permissions.BLOCKBREAK_WRONGBLOCK);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
     */
    @Override
    public final boolean isEnabled(final CheckType checkType) {
        switch (checkType) {
        case BLOCKBREAK_DIRECTION:
            return directionCheck;
        case BLOCKBREAK_FASTBREAK:
            return fastBreakCheck;
        case BLOCKBREAK_FREQUENCY:
        	return frequencyCheck; 
        case BLOCKBREAK_NOSWING:
            return noSwingCheck;
        case BLOCKBREAK_REACH:
            return reachCheck;
        case BLOCKBREAK_WRONGBLOCK:
        	return wrongBlockCheck;
        default:
            return true;
        }
    }
}
