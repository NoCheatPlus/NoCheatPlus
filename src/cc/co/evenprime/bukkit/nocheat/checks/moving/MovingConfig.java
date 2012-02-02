package cc.co.evenprime.bukkit.nocheat.checks.moving;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

/**
 * Configurations specific for the Move Checks. Every world gets one of these
 * assigned to it.
 * 
 */
public class MovingConfig implements ConfigItem {

    public final boolean    check;

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
    public final float      nofallMultiplier;
    public final ActionList nofallActions;

    public final boolean    morePacketsCheck;
    public final ActionList morePacketsActions;

    public final double     flyingHeightLimit;

    public MovingConfig(NoCheatConfiguration data) {

        identifyCreativeMode = data.getBoolean(ConfPaths.MOVING_RUNFLY_FLYING_ALLOWINCREATIVE);

        runflyCheck = data.getBoolean(ConfPaths.MOVING_RUNFLY_CHECK);
        walkingSpeedLimit = ((double) 22) / 100D;
        sprintingSpeedLimit = ((double) 35) / 100D;
        jumpheight = ((double) 135) / 100D;
        actions = data.getActionList(ConfPaths.MOVING_RUNFLY_ACTIONS);

        swimmingSpeedLimit = ((double) 18) / 100D;
        sneakingCheck = !data.getBoolean(ConfPaths.MOVING_RUNFLY_ALLOWFASTSNEAKING);
        sneakingSpeedLimit = ((double) 14) / 100D;

        allowFlying = data.getBoolean(ConfPaths.MOVING_RUNFLY_FLYING_ALLOWALWAYS);
        flyingSpeedLimitVertical = ((double) data.getInt(ConfPaths.MOVING_RUNFLY_FLYING_SPEEDLIMITVERTICAL)) / 100D;
        flyingSpeedLimitHorizontal = ((double) data.getInt(ConfPaths.MOVING_RUNFLY_FLYING_SPEEDLIMITHORIZONTAL)) / 100D;
        flyingHeightLimit = data.getInt(ConfPaths.MOVING_RUNFLY_FLYING_HEIGHTLIMIT);
        flyingActions = data.getActionList(ConfPaths.MOVING_RUNFLY_FLYING_ACTIONS);

        nofallCheck = data.getBoolean(ConfPaths.MOVING_RUNFLY_CHECKNOFALL);
        nofallMultiplier = ((float) 200) / 100F;
        nofallActions = data.getActionList(ConfPaths.MOVING_RUNFLY_NOFALLACTIONS);

        morePacketsCheck = data.getBoolean(ConfPaths.MOVING_MOREPACKETS_CHECK);
        morePacketsActions = data.getActionList(ConfPaths.MOVING_MOREPACKETS_ACTIONS);

        check = runflyCheck || morePacketsCheck;

    }
}
