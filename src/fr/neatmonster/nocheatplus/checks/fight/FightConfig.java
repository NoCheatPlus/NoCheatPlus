package fr.neatmonster.nocheatplus.checks.fight;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.checks.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * MM""""""""`M oo          dP         dP   MM'""""'YMM                   .8888b oo          
 * MM  mmmmmmmM             88         88   M' .mmm. `M                   88   "             
 * M'      MMMM dP .d8888b. 88d888b. d8888P M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * MM  MMMMMMMM 88 88'  `88 88'  `88   88   M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * MM  MMMMMMMM 88 88.  .88 88    88   88   M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * MM  MMMMMMMM dP `8888P88 dP    dP   dP   MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMMM         .88                 MMMMMMMMMMM                                  .88 
 *                  d8888P                                                           d8888P  
 */
/**
 * Configurations specific for the "fight" checks. Every world gets one of these assigned to it, or if a world doesn't
 * get it's own, it will use the "global" version.
 */
public class FightConfig implements CheckConfig {
	
	public static final CheckConfigFactory factory = new CheckConfigFactory(){
		@Override
		public final CheckConfig getConfig(final Player player) {
			return FightConfig.getConfig(player);
		}
	};

    /** The map containing the configurations per world. */
    private static Map<String, FightConfig> worldsMap = new HashMap<String, FightConfig>();

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
    public static FightConfig getConfig(final Player player) {
        if (!worldsMap.containsKey(player.getWorld().getName()))
            worldsMap.put(player.getWorld().getName(),
                    new FightConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
        return worldsMap.get(player.getWorld().getName());
    }

    public final boolean    angleCheck;
    public final int        angleThreshold;
    public final ActionList angleActions;

    public final boolean    criticalCheck;
    public final double     criticalFallDistance;
    public final double     criticalVelocity;
    public final ActionList criticalActions;

    public final boolean    directionCheck;
    public final long       directionPenalty;
    public final ActionList directionActions;

    public final boolean    godModeCheck;
    public final ActionList godModeActions;

    public final boolean    instantHealCheck;
    public final ActionList instantHealActions;

    public final boolean    knockbackCheck;
    public final long       knockbackInterval;
    public final ActionList knockbackActions;

    public final boolean    noSwingCheck;
    public final ActionList noSwingActions;

    public final boolean    reachCheck;
    public final long       reachPenalty;
    public final ActionList reachActions;

    public final boolean    speedCheck;
    public final int        speedLimit;
    public final ActionList speedActions;

    /**
     * Instantiates a new fight configuration.
     * 
     * @param dataFactory
     *            the dataFactory
     */
    public FightConfig(final ConfigFile data) {
        angleCheck = data.getBoolean(ConfPaths.FIGHT_ANGLE_CHECK);
        angleThreshold = data.getInt(ConfPaths.FIGHT_ANGLE_THRESHOLD);
        angleActions = data.getActionList(ConfPaths.FIGHT_ANGLE_ACTIONS, Permissions.FIGHT_ANGLE);

        criticalCheck = data.getBoolean(ConfPaths.FIGHT_CRITICAL_CHECK);
        criticalFallDistance = data.getDouble(ConfPaths.FIGHT_CRITICAL_FALLDISTANCE);
        criticalVelocity = data.getDouble(ConfPaths.FIGHT_CRITICAL_VELOCITY);
        criticalActions = data.getActionList(ConfPaths.FIGHT_CRITICAL_ACTIONS, Permissions.FIGHT_CRITICAL);

        directionCheck = data.getBoolean(ConfPaths.FIGHT_DIRECTION_CHECK);
        directionPenalty = data.getLong(ConfPaths.FIGHT_DIRECTION_PENALTY);
        directionActions = data.getActionList(ConfPaths.FIGHT_DIRECTION_ACTIONS, Permissions.FIGHT_DIRECTION);

        godModeCheck = data.getBoolean(ConfPaths.FIGHT_GODMODE_CHECK);
        godModeActions = data.getActionList(ConfPaths.FIGHT_GODMODE_ACTIONS, Permissions.FIGHT_GODMODE);

        instantHealCheck = data.getBoolean(ConfPaths.FIGHT_INSTANTHEAL_CHECK);
        instantHealActions = data.getActionList(ConfPaths.FIGHT_INSTANTHEAL_ACTIONS, Permissions.FIGHT_INSTANTHEAL);

        knockbackCheck = data.getBoolean(ConfPaths.FIGHT_KNOCKBACK_CHECK);
        knockbackInterval = data.getLong(ConfPaths.FIGHT_KNOCKBACK_INTERVAL);
        knockbackActions = data.getActionList(ConfPaths.FIGHT_KNOCKBACK_ACTIONS, Permissions.FIGHT_KNOCKBACK);

        noSwingCheck = data.getBoolean(ConfPaths.FIGHT_NOSWING_CHECK);
        noSwingActions = data.getActionList(ConfPaths.FIGHT_NOSWING_ACTIONS, Permissions.FIGHT_NOSWING);

        reachCheck = data.getBoolean(ConfPaths.FIGHT_REACH_CHECK);
        reachPenalty = data.getLong(ConfPaths.FIGHT_REACH_PENALTY);
        reachActions = data.getActionList(ConfPaths.FIGHT_REACH_ACTIONS, Permissions.FIGHT_REACH);

        speedCheck = data.getBoolean(ConfPaths.FIGHT_SPEED_CHECK);
        speedLimit = data.getInt(ConfPaths.FIGHT_SPEED_LIMIT);
        speedActions = data.getActionList(ConfPaths.FIGHT_SPEED_ACTIONS, Permissions.FIGHT_SPEED);
    }
    
    @Override
	public final boolean isEnabled(final CheckType checkType) {
		switch(checkType){
		case FIGHT_ANGLE:
			return angleCheck;
		case FIGHT_CRITICAL:
			return criticalCheck;
		case FIGHT_DIRECTION:
			return directionCheck;
		case FIGHT_GODMODE:
			return godModeCheck;
		case FIGHT_INSTANTHEAL:
			return instantHealCheck;
		case FIGHT_KNOCKBACK:
			return knockbackCheck;
		case FIGHT_NOSWING:
			return noSwingCheck;
		case FIGHT_REACH:
			return reachCheck;
		case FIGHT_SPEED:
			return speedCheck;
		default:
			return true;
		}
	}
}
