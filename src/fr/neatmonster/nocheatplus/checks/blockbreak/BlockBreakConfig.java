package fr.neatmonster.nocheatplus.checks.blockbreak;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

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
    public static final CheckConfigFactory       factory   = new CheckConfigFactory() {
                                                               @Override
                                                               public final ICheckConfig getConfig(final Player player) {
                                                                   return BlockBreakConfig.getConfig(player);
                                                               }
                                                           };

    /** The map containing the configurations per world. */
    private static Map<String, BlockBreakConfig> worldsMap = new HashMap<String, BlockBreakConfig>();

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
    public final int        fastBreakBuffer;
    public final int        fastBreakInterval;
    public final ActionList fastBreakActions;
    
	public boolean improbableFastBreakCheck;

    public final boolean    noSwingCheck;
    public final ActionList noSwingActions;

    public final boolean    reachCheck;
    public final ActionList reachActions;

    /**
     * Instantiates a new block break configuration.
     * 
     * @param data
     *            the data
     */
    public BlockBreakConfig(final ConfigFile data) {
        directionCheck = data.getBoolean(ConfPaths.BLOCKBREAK_DIRECTION_CHECK);
        directionActions = data.getActionList(ConfPaths.BLOCKBREAK_DIRECTION_ACTIONS, Permissions.BLOCKBREAK_DIRECTION);

        fastBreakCheck = data.getBoolean(ConfPaths.BLOCKBREAK_FASTBREAK_CHECK);
        fastBreakBuffer = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_BUFFER);
        fastBreakInterval = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_INTERVAL);
        fastBreakActions = data.getActionList(ConfPaths.BLOCKBREAK_FASTBREAK_ACTIONS, Permissions.BLOCKBREAK_FASTBREAK);

        improbableFastBreakCheck = data.getBoolean(ConfPaths.COMBINED_IMPROBABLE_FASTBREAK_CHECK);
        
        noSwingCheck = data.getBoolean(ConfPaths.BLOCKBREAK_NOSWING_CHECK);
        noSwingActions = data.getActionList(ConfPaths.BLOCKBREAK_NOSWING_ACTIONS, Permissions.BLOCKBREAK_NOSWING);

        reachCheck = data.getBoolean(ConfPaths.BLOCKBREAK_REACH_CHECK);
        reachActions = data.getActionList(ConfPaths.BLOCKBREAK_REACH_ACTIONS, Permissions.BLOCKBREAK_REACH);
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
        case BLOCKBREAK_NOSWING:
            return noSwingCheck;
        case BLOCKBREAK_REACH:
            return reachCheck;
        default:
            return true;
        }
    }
}
