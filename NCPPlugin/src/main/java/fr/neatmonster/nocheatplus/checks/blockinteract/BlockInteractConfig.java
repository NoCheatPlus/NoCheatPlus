package fr.neatmonster.nocheatplus.checks.blockinteract;

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
 * M#"""""""'M  dP                   dP       M""M            dP                                         dP   
 * ##  mmmm. `M 88                   88       M  M            88                                         88   
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M  M 88d888b. d8888P .d8888b. 88d888b. .d8888b. .d8888b. d8888P 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   M  M 88'  `88   88   88ooood8 88'  `88 88'  `88 88'  `""   88   
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. M  M 88    88   88   88.  ... 88       88.  .88 88.  ...   88   
 * M#       .;M dP `88888P' `88888P' dP   `YP M  M dP    dP   dP   `88888P' dP       `88888P8 `88888P'   dP   
 * M#########M                                MMMM                                                            
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
 * Configurations specific for the block interact checks. Every world gets one of these assigned to it, or if a world
 * doesn't get it's own, it will use the "global" version.
 */
public class BlockInteractConfig extends ACheckConfig {

	/** The factory creating configurations. */
	public static final CheckConfigFactory factory = new CheckConfigFactory() {
		@Override
		public final ICheckConfig getConfig(final Player player) {
			return BlockInteractConfig.getConfig(player);
		}
	};

    /** The map containing the configurations per world. */
    private static final Map<String, BlockInteractConfig> worldsMap = new HashMap<String, BlockInteractConfig>();

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
    public static BlockInteractConfig getConfig(final Player player) {
        if (!worldsMap.containsKey(player.getWorld().getName()))
            worldsMap.put(player.getWorld().getName(),
                    new BlockInteractConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
        return worldsMap.get(player.getWorld().getName());
    }

    public final boolean    directionCheck;
    public final ActionList directionActions;

    public final boolean    reachCheck;
    public final ActionList reachActions;
    
    public final boolean    speedCheck;
    public final long		speedInterval;
    public final int		speedLimit;
    public final ActionList speedActions;
    
    public final boolean    visibleCheck;
    public final ActionList visibleActions;

    /**
     * Instantiates a new block interact configuration.
     * 
     * @param data
     *            the data
     */
    public BlockInteractConfig(final ConfigFile data) {
        super(data, ConfPaths.BLOCKINTERACT);
        directionCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_DIRECTION_CHECK);
        directionActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_DIRECTION_ACTIONS,
                Permissions.BLOCKINTERACT_DIRECTION);

        reachCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_REACH_CHECK);
        reachActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_REACH_ACTIONS, Permissions.BLOCKINTERACT_REACH);
        
        speedCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_SPEED_CHECK);
        speedInterval = data.getLong(ConfPaths.BLOCKINTERACT_SPEED_INTERVAL);
        speedLimit = data.getInt(ConfPaths.BLOCKINTERACT_SPEED_LIMIT);
        speedActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_SPEED_ACTIONS, Permissions.BLOCKINTERACT_SPEED);
        
        visibleCheck = data.getBoolean(ConfPaths.BLOCKINTERACT_VISIBLE_CHECK);
        visibleActions = data.getOptimizedActionList(ConfPaths.BLOCKINTERACT_VISIBLE_ACTIONS, Permissions.BLOCKINTERACT_VISIBLE);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
     */
    @Override
    public final boolean isEnabled(final CheckType checkType) {
        switch (checkType) {
        case BLOCKINTERACT_SPEED:
        	return speedCheck;
        case BLOCKINTERACT_DIRECTION:
            return directionCheck;
        case BLOCKINTERACT_REACH:
            return reachCheck;
        case BLOCKINTERACT_VISIBLE:
        	return visibleCheck;
        default:
            return true;
        }
    }
}
