package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

/*
 * M"""""`'"""`YM                   oo                   MM'""""'YMM                   .8888b oo          
 * M  mm.  mm.  M                                        M' .mmm. `M                   88   "             
 * M  MMM  MMM  M .d8888b. dP   .dP dP 88d888b. .d8888b. M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. 
 * M  MMM  MMM  M 88'  `88 88   d8' 88 88'  `88 88'  `88 M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 
 * M  MMM  MMM  M 88.  .88 88 .88'  88 88    88 88.  .88 M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 
 * M  MMM  MMM  M `88888P' 8888P'   dP dP    dP `8888P88 MM.     .dM `88888P' dP    dP dP     dP `8888P88 
 * MMMMMMMMMMMMMM                                    .88 MMMMMMMMMMM                                  .88 
 *                                               d8888P                                           d8888P  
 */
/**
 * Configurations specific for the moving checks. Every world gets one of these assigned to it.
 */
public class MovingConfig {

    /** The map containing the configurations per world. */
    private static Map<String, MovingConfig> worldsMap = new HashMap<String, MovingConfig>();

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
    public static MovingConfig getConfig(final Player player) {
        if (!worldsMap.containsKey(player.getWorld().getName()))
            worldsMap.put(player.getWorld().getName(),
                    new MovingConfig(ConfigManager.getConfigFile(player.getWorld().getName())));
        return worldsMap.get(player.getWorld().getName());
    }

    public final boolean    creativeFlyCheck;
    public final int        creativeFlyHorizontalSpeed;
    public final int        creativeFlyMaxHeight;
    public final int        creativeFlyVerticalSpeed;
    public final ActionList creativeFlyActions;

    public final boolean    morePacketsCheck;
    public final ActionList morePacketsActions;

    public final boolean    morePacketsVehicleCheck;
    public final ActionList morePacketsVehicleActions;

    public final boolean    noFallCheck;
    public final boolean    noFallAggressive;
    public final ActionList noFallActions;

    public final boolean    survivalFlyCheck;
    public final int        survivalFlyBlockingSpeed;
    public final int        survivalFlyCobWebSpeed;
    public final int        survivalFlyLadderSpeed;
    public final int        survivalFlyLavaSpeed;
    public final int        survivalFlyMoveSpeed;
    public final int        survivalFlySneakingSpeed;
    public final int        survivalFlySoulSandSpeed;
    public final int        survivalFlySprintingSpeed;
    public final int        survivalFlyWaterSpeed;
    public final ActionList survivalFlyActions;

    /**
     * Instantiates a new moving configuration.
     * 
     * @param data
     *            the data
     */
    public MovingConfig(final ConfigFile data) {
        creativeFlyCheck = data.getBoolean(ConfPaths.MOVING_CREATIVEFLY_CHECK);
        creativeFlyHorizontalSpeed = data.getInt(ConfPaths.MOVING_CREATIVEFLY_HORIZONTALSPEED);
        creativeFlyMaxHeight = data.getInt(ConfPaths.MOVING_CREATIVEFLY_MAXHEIGHT);
        creativeFlyVerticalSpeed = data.getInt(ConfPaths.MOVING_CREATIVEFLY_VERTICALSPEED);
        creativeFlyActions = data.getActionList(ConfPaths.MOVING_CREATIVEFLY_ACTIONS, Permissions.MOVING_CREATIVEFLY);

        morePacketsCheck = data.getBoolean(ConfPaths.MOVING_MOREPACKETS_CHECK);
        morePacketsActions = data.getActionList(ConfPaths.MOVING_MOREPACKETS_ACTIONS, Permissions.MOVING_MOREPACKETS);

        morePacketsVehicleCheck = data.getBoolean(ConfPaths.MOVING_MOREPACKETSVEHICLE_CHECK);
        morePacketsVehicleActions = data.getActionList(ConfPaths.MOVING_MOREPACKETSVEHICLE_ACTIONS,
                Permissions.MOVING_MOREPACKETS);

        noFallCheck = data.getBoolean(ConfPaths.MOVING_NOFALL_CHECK);
        noFallAggressive = data.getBoolean(ConfPaths.MOVING_NOFALL_AGGRESSIVE);
        noFallActions = data.getActionList(ConfPaths.MOVING_NOFALL_ACTIONS, Permissions.MOVING_NOFALL);

        survivalFlyCheck = data.getBoolean(ConfPaths.MOVING_SURVIVALFLY_CHECK);
        // Default values are specified here because this settings aren't showed by default into the configuration file.
        survivalFlyBlockingSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_BLOCKINGSPEED, 100);
        survivalFlyCobWebSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_COBWEBSPEED, 100);
        survivalFlyLadderSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_LADDERSPEED, 100);
        survivalFlyLavaSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_LAVASPEED, 100);
        survivalFlyMoveSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_MOVESPEED, 100);
        survivalFlySneakingSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_SNEAKINGSPEED, 100);
        survivalFlySoulSandSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_SOULSANDSPEED, 100);
        survivalFlySprintingSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_SPRINTINGSPEED, 100);
        survivalFlyWaterSpeed = data.getInt(ConfPaths.MOVING_SURVIVALFLY_WATERSPEED, 100);
        survivalFlyActions = data.getActionList(ConfPaths.MOVING_SURVIVALFLY_ACTIONS, Permissions.MOVING_SURVIVALFLY);
    }
}
