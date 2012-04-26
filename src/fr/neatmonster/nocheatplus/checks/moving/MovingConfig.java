package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.players.informations.Permissions;

/**
 * Configurations specific for the Move Checks. Every world gets one of these
 * assigned to it.
 * 
 */
public class MovingConfig extends CheckConfig {

    public final boolean    runflyCheck;
    public final double     jumpheight;
    public final int        maxCooldown;
    public final boolean    identifyCreativeMode;
    public final double     walkingSpeedLimit;
    public final double     sprintingSpeedLimit;
    public final double     swimmingSpeedLimit;
    public final boolean    sneakingCheck;
    public final double     sneakingSpeedLimit;
    public final boolean    blockingCheck;
    public final double     blockingSpeedLimit;
    public final double     cobWebHoriSpeedLimit;
    public final double     cobWebVertSpeedLimit;

    public final ActionList actions;

    public final boolean    allowFlying;
    public final double     flyingSpeedLimitVertical;
    public final double     flyingSpeedLimitHorizontal;
    public final ActionList flyingActions;

    public final boolean    nofallCheck;
    public final boolean    nofallaggressive;
    public final float      nofallMultiplier;
    public final ActionList nofallActions;

    public final boolean    morePacketsCheck;
    public final ActionList morePacketsActions;

    public final boolean    morePacketsVehicleCheck;
    public final ActionList morePacketsVehicleActions;

    public final boolean    waterWalkCheck;
    public final ActionList waterWalkActions;

    public final int        flyingHeightLimit;

    public MovingConfig(final ConfigFile data) {

        identifyCreativeMode = data.getBoolean(ConfPaths.MOVING_RUNFLY_FLYING_ALLOWINCREATIVE);

        runflyCheck = data.getBoolean(ConfPaths.MOVING_RUNFLY_CHECK);

        final int walkspeed = data.getInt(ConfPaths.MOVING_RUNFLY_WALKSPEED, 100);
        final int sprintspeed = data.getInt(ConfPaths.MOVING_RUNFLY_SPRINTSPEED, 100);
        final int swimspeed = data.getInt(ConfPaths.MOVING_RUNFLY_SWIMSPEED, 100);
        final int sneakspeed = data.getInt(ConfPaths.MOVING_RUNFLY_SNEAKSPEED, 100);
        final int blockspeed = data.getInt(ConfPaths.MOVING_RUNFLY_BLOCKSPEED, 100);
        final int cobWebSpeed = data.getInt(ConfPaths.MOVING_RUNFLY_COBWEBSPEED, 100);
        walkingSpeedLimit = 0.22 * walkspeed / 100D;
        sprintingSpeedLimit = 0.35 * sprintspeed / 100D;
        swimmingSpeedLimit = 0.18 * swimspeed / 100D;
        sneakingSpeedLimit = 0.14 * sneakspeed / 100D;
        blockingSpeedLimit = 0.16 * blockspeed / 100D;
        cobWebHoriSpeedLimit = 0.08 * cobWebSpeed / 100D;
        cobWebVertSpeedLimit = 0.07 * cobWebSpeed / 100D;
        jumpheight = 135 / 100D;

        sneakingCheck = !data.getBoolean(ConfPaths.MOVING_RUNFLY_ALLOWFASTSNEAKING);
        blockingCheck = !data.getBoolean(ConfPaths.MOVING_RUNFLY_ALLOWFASTBLOCKING);
        maxCooldown = data.getInt(ConfPaths.MOVING_RUNFLY_MAXCOOLDOWN);
        actions = data.getActionList(ConfPaths.MOVING_RUNFLY_ACTIONS, Permissions.MOVING_RUNFLY);

        allowFlying = data.getBoolean(ConfPaths.MOVING_RUNFLY_FLYING_ALLOWALWAYS);
        flyingSpeedLimitVertical = data.getInt(ConfPaths.MOVING_RUNFLY_FLYING_SPEEDLIMITVERTICAL) / 100D;
        flyingSpeedLimitHorizontal = data.getInt(ConfPaths.MOVING_RUNFLY_FLYING_SPEEDLIMITHORIZONTAL) / 100D;
        flyingHeightLimit = data.getInt(ConfPaths.MOVING_RUNFLY_FLYING_HEIGHTLIMIT);
        flyingActions = data.getActionList(ConfPaths.MOVING_RUNFLY_FLYING_ACTIONS, Permissions.MOVING_FLYING);

        nofallCheck = data.getBoolean(ConfPaths.MOVING_RUNFLY_CHECKNOFALL);
        nofallMultiplier = 200 / 100F;
        nofallaggressive = data.getBoolean(ConfPaths.MOVING_RUNFLY_NOFALLAGGRESSIVE);
        nofallActions = data.getActionList(ConfPaths.MOVING_RUNFLY_NOFALLACTIONS, Permissions.MOVING_NOFALL);

        morePacketsCheck = data.getBoolean(ConfPaths.MOVING_MOREPACKETS_CHECK);
        morePacketsActions = data.getActionList(ConfPaths.MOVING_MOREPACKETS_ACTIONS, Permissions.MOVING_MOREPACKETS);

        morePacketsVehicleCheck = data.getBoolean(ConfPaths.MOVING_MOREPACKETSVEHICLE_CHECK);
        morePacketsVehicleActions = data.getActionList(ConfPaths.MOVING_MOREPACKETSVEHICLE_ACTIONS,
                Permissions.MOVING_MOREPACKETS);

        waterWalkCheck = data.getBoolean(ConfPaths.MOVING_WATERWALK_CHECK);
        waterWalkActions = data.getActionList(ConfPaths.MOVING_WATERWALK_ACTIONS, Permissions.MOVING_WATERWALK);
    }
}
