package me.neatmonster.nocheatplus.checks.moving;

import me.neatmonster.nocheatplus.ConfigItem;
import me.neatmonster.nocheatplus.actions.types.ActionList;
import me.neatmonster.nocheatplus.config.ConfPaths;
import me.neatmonster.nocheatplus.config.NoCheatPlusConfiguration;
import me.neatmonster.nocheatplus.config.Permissions;

/**
 * Configurations specific for the Move Checks. Every world gets one of these
 * assigned to it.
 * 
 */
public class MovingConfig implements ConfigItem {

    public final boolean    runflyCheck;
    public final boolean    identifyCreativeMode;
    public final double     walkingSpeedLimit;
    public final double     sprintingSpeedLimit;
    public final double     jumpheight;
    public final double     swimmingSpeedLimit;
    public final boolean    sneakingCheck;
    public final double     sneakingSpeedLimit;
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

    public MovingConfig(final NoCheatPlusConfiguration data) {

        identifyCreativeMode = data.getBoolean(ConfPaths.MOVING_RUNFLY_FLYING_ALLOWINCREATIVE);

        runflyCheck = data.getBoolean(ConfPaths.MOVING_RUNFLY_CHECK);

        final int walkspeed = data.getInt(ConfPaths.MOVING_RUNFLY_WALKSPEED, 100);
        final int sprintspeed = data.getInt(ConfPaths.MOVING_RUNFLY_SPRINTSPEED, 100);
        final int swimspeed = data.getInt(ConfPaths.MOVING_RUNFLY_SWIMSPEED, 100);
        final int sneakspeed = data.getInt(ConfPaths.MOVING_RUNFLY_SNEAKSPEED, 100);
        walkingSpeedLimit = 0.22 * walkspeed / 100D;
        sprintingSpeedLimit = 0.35 * sprintspeed / 100D;
        swimmingSpeedLimit = 0.18 * swimspeed / 100D;
        sneakingSpeedLimit = 0.14 * sneakspeed / 100D;
        jumpheight = 135 / 100D;

        sneakingCheck = !data.getBoolean(ConfPaths.MOVING_RUNFLY_ALLOWFASTSNEAKING);
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
