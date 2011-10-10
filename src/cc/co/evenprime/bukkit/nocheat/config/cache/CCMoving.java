package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.DefaultConfiguration;

/**
 * Configurations specific for the Move Checks. Every world gets one of these
 * assigned to it.
 * 
 * @author Evenprime
 * 
 */
public class CCMoving {

    public final boolean    check;

    public final boolean    runflyCheck;
    public final boolean    identifyCreativeMode;
    public final double     walkingSpeedLimit;
    public final double     sprintingSpeedLimit;
    public final double     jumpheight;
    public final boolean    swimmingCheck;
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

    public CCMoving(Configuration data) {

        check = data.getBoolean(DefaultConfiguration.MOVING_CHECK);
        identifyCreativeMode = data.getBoolean(DefaultConfiguration.MOVING_IDENTIFYCREATIVEMODE);
        
        runflyCheck = data.getBoolean(DefaultConfiguration.MOVING_RUNFLY_CHECK);
        walkingSpeedLimit = ((double) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_WALKINGSPEEDLIMIT)) / 100D;
        sprintingSpeedLimit = ((double) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_SPRINTINGSPEEDLIMIT)) / 100D;
        jumpheight = ((double) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_JUMPHEIGHT)) / 100D;
        actions = data.getActionList(DefaultConfiguration.MOVING_RUNFLY_ACTIONS);

        swimmingCheck = data.getBoolean(DefaultConfiguration.MOVING_RUNFLY_CHECKSWIMMING);
        swimmingSpeedLimit = ((double) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_SWIMMINGSPEEDLIMIT)) / 100D;
        sneakingCheck = data.getBoolean(DefaultConfiguration.MOVING_RUNFLY_CHECKSNEAKING);
        sneakingSpeedLimit = ((double) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_SNEAKINGSPEEDLIMIT)) / 100D;

        allowFlying = data.getBoolean(DefaultConfiguration.MOVING_RUNFLY_ALLOWLIMITEDFLYING);
        flyingSpeedLimitVertical = ((double) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_FLYINGSPEEDLIMITVERTICAL)) / 100D;
        flyingSpeedLimitHorizontal = ((double) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_FLYINGSPEEDLIMITHORIZONTAL)) / 100D;
        flyingActions = data.getActionList(DefaultConfiguration.MOVING_RUNFLY_FLYINGACTIONS);

        nofallCheck = data.getBoolean(DefaultConfiguration.MOVING_RUNFLY_CHECKNOFALL);
        nofallMultiplier = ((float) data.getInteger(DefaultConfiguration.MOVING_RUNFLY_NOFALLMULTIPLIER)) / 100F;
        nofallActions = data.getActionList(DefaultConfiguration.MOVING_RUNFLY_NOFALLACTIONS);

        morePacketsCheck = data.getBoolean(DefaultConfiguration.MOVING_MOREPACKETS_CHECK);
        morePacketsActions = data.getActionList(DefaultConfiguration.MOVING_MOREPACKETS_ACTIONS);

    }
}
