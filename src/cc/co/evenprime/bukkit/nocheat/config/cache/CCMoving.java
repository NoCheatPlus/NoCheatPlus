package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;

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

    public final boolean    noclipCheck;
    public final ActionList noclipActions;

    public final boolean    morePacketsCheck;
    public final ActionList morePacketsActions;

    public CCMoving(Configuration data) {

        check = data.getBoolean("moving.check");

        runflyCheck = data.getBoolean("moving.runfly.check");
        walkingSpeedLimit = ((double) data.getInteger("moving.runfly.walkingspeedlimit")) / 100D;
        sprintingSpeedLimit = ((double) data.getInteger("moving.runfly.sprintingspeedlimit")) / 100D;
        jumpheight = ((double) data.getInteger("moving.runfly.jumpheight")) / 100D;
        actions = data.getActionList("moving.runfly.actions");
        
        swimmingCheck = data.getBoolean("moving.runfly.checkswimming");
        swimmingSpeedLimit = ((double) data.getInteger("moving.runfly.swimmingspeedlimit")) / 100D;
        sneakingCheck = data.getBoolean("moving.runfly.checksneaking");
        sneakingSpeedLimit = ((double) data.getInteger("moving.runfly.sneakingspeedlimit")) / 100D;
        
        allowFlying = data.getBoolean("moving.runfly.allowlimitedflying");
        flyingSpeedLimitVertical = ((double) data.getInteger("moving.runfly.flyingspeedlimitvertical")) / 100D;
        flyingSpeedLimitHorizontal = ((double) data.getInteger("moving.runfly.flyingspeedlimithorizontal")) / 100D;
        flyingActions = data.getActionList("moving.runfly.flyingactions");

        noclipCheck = data.getBoolean("moving.noclip.check");
        noclipActions = data.getActionList("moving.noclip.actions");

        morePacketsCheck = data.getBoolean("moving.morepackets.check");
        morePacketsActions = data.getActionList("moving.morepackets.actions");

    }
}
