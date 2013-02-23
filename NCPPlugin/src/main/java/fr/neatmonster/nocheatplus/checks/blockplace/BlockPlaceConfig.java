package fr.neatmonster.nocheatplus.checks.blockplace;

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
 * M#"""""""'M  dP                   dP       MM"""""""`YM dP                            
 * ##  mmmm. `M 88                   88       MM  mmmmm  M 88                            
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M'        .M 88 .d8888b. .d8888b. .d8888b. 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   MM  MMMMMMMM 88 88'  `88 88'  `"" 88ooood8 
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. MM  MMMMMMMM 88 88.  .88 88.  ... 88.  ... 
 * M#       .;M dP `88888P' `88888P' dP   `YP MM  MMMMMMMM dP `88888P8 `88888P' `88888P' 
 * M#########M                                MMMMMMMMMMMM                               
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
 * Configurations specific for the block place checks. Every world gets one of these assigned to it, or if a world
 * doesn't get it's own, it will use the "global" version.
 */
public class BlockPlaceConfig extends ACheckConfig {

	/** The factory creating configurations. */
	public static final CheckConfigFactory factory = new CheckConfigFactory() {
		@Override
		public final ICheckConfig getConfig(final Player player) {
			return BlockPlaceConfig.getConfig(player);
		}
	};

    /** The map containing the configurations per world. */
    private static final Map<String, BlockPlaceConfig> worldsMap = new HashMap<String, BlockPlaceConfig>();

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
    public static BlockPlaceConfig getConfig(final Player player) {
        if (!worldsMap.containsKey(player.getWorld().getName()))
            worldsMap.put(player.getWorld().getName(),
                    new BlockPlaceConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
        return worldsMap.get(player.getWorld().getName());
    }

    public final boolean    directionCheck;
    public final ActionList directionActions;

    public final boolean    fastPlaceCheck;
    public final int        fastPlaceLimit;
    public final int        fastPlaceShortTermTicks;
    public final int        fastPlaceShortTermLimit;
    public final ActionList fastPlaceActions;

    public final boolean    noSwingCheck;
    public final ActionList noSwingActions;

    public final boolean    reachCheck;
    public final ActionList reachActions;

    public final boolean    speedCheck;
    public final long       speedInterval;
    public final ActionList speedActions;

    /**
     * Instantiates a new block place configuration.
     * 
     * @param data
     *            the data
     */
    public BlockPlaceConfig(final ConfigFile data) {
        super(data, ConfPaths.BLOCKPLACE);
        directionCheck = data.getBoolean(ConfPaths.BLOCKPLACE_DIRECTION_CHECK);
        directionActions = data.getOptimizedActionList(ConfPaths.BLOCKPLACE_DIRECTION_ACTIONS, Permissions.BLOCKPLACE_DIRECTION);

        fastPlaceCheck = data.getBoolean(ConfPaths.BLOCKPLACE_FASTPLACE_CHECK);
        fastPlaceLimit = data.getInt(ConfPaths.BLOCKPLACE_FASTPLACE_LIMIT);
        fastPlaceShortTermTicks = data.getInt(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_TICKS);
        fastPlaceShortTermLimit = data.getInt(ConfPaths.BLOCKPLACE_FASTPLACE_SHORTTERM_LIMIT);
        fastPlaceActions = data.getOptimizedActionList(ConfPaths.BLOCKPLACE_FASTPLACE_ACTIONS, Permissions.BLOCKPLACE_FASTPLACE);

        noSwingCheck = data.getBoolean(ConfPaths.BLOCKPLACE_NOSWING_CHECK);
        noSwingActions = data.getOptimizedActionList(ConfPaths.BLOCKPLACE_NOSWING_ACTIONS, Permissions.BLOCKPLACE_NOSWING);

        reachCheck = data.getBoolean(ConfPaths.BLOCKPLACE_REACH_CHECK);
        reachActions = data.getOptimizedActionList(ConfPaths.BLOCKPLACE_REACH_ACTIONS, Permissions.BLOCKPLACE_REACH);

        speedCheck = data.getBoolean(ConfPaths.BLOCKPLACE_SPEED_CHECK);
        speedInterval = data.getLong(ConfPaths.BLOCKPLACE_SPEED_INTERVAL);
        speedActions = data.getOptimizedActionList(ConfPaths.BLOCKPLACE_SPEED_ACTIONS, Permissions.BLOCKPLACE_SPEED);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
     */
    @Override
    public final boolean isEnabled(final CheckType checkType) {
        switch (checkType) {
        case BLOCKPLACE_DIRECTION:
            return directionCheck;
        case BLOCKPLACE_FASTPLACE:
            return fastPlaceCheck;
        case BLOCKPLACE_NOSWING:
            return noSwingCheck;
        case BLOCKPLACE_REACH:
            return reachCheck;
        case BLOCKPLACE_SPEED:
            return speedCheck;
        case BLOCKPLACE_AGAINST:
        	return true;
        default:
            return true;
        }
    }
}
