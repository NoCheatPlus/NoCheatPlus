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

    public final boolean    flyingCheck;
    public final double     flyingSpeedLimitVertical;
    public final double     flyingSpeedLimitHorizontal;

    public final ActionList flyingActions;

    public final boolean    runningCheck;
    public final double     runningSpeedLimit;

    public final ActionList runningActions;

    public final boolean    swimmingCheck;
    public final double     swimmingSpeedLimit;

    public final boolean    sneakingCheck;
    public final double     sneakingSpeedLimit;

    public final boolean    noclipCheck;
    public final ActionList noclipActions;

    public final boolean    morePacketsCheck;
    public final ActionList morePacketsActions;

    public CCMoving(Configuration data) {

        check = data.getBoolean("moving.check");
        flyingCheck = data.getBoolean("moving.flying.check");
        flyingSpeedLimitVertical = ((double) data.getInteger("moving.flying.speedlimitvertical")) / 100D;
        flyingSpeedLimitHorizontal = ((double) data.getInteger("moving.flying.speedlimithorizontal")) / 100D;
        flyingActions = data.getActionList("moving.flying.actions");

        runningCheck = data.getBoolean("moving.running.check");
        runningSpeedLimit = ((double) data.getInteger("moving.running.speedlimit")) / 100D;
        runningActions = data.getActionList("moving.running.actions");
        swimmingCheck = data.getBoolean("moving.running.swimming.check");
        swimmingSpeedLimit = ((double) data.getInteger("moving.running.swimming.speedlimit")) / 100D;
        sneakingCheck = data.getBoolean("moving.running.sneaking.check");
        sneakingSpeedLimit = ((double) data.getInteger("moving.running.sneaking.speedlimit")) / 100D;

        noclipCheck = data.getBoolean("moving.noclip.check");
        noclipActions = data.getActionList("moving.noclip.actions");

        morePacketsCheck = data.getBoolean("moving.morepackets.check");
        morePacketsActions = data.getActionList("moving.morepackets.actions");

    }
}
