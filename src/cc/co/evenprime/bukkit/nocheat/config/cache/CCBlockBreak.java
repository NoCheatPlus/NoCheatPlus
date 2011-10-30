package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;

/**
 * Configurations specific for the "BlockBreak" checks
 * Every world gets one of these assigned to it.
 * 
 */
public class CCBlockBreak {

    public final boolean    check;
    public final boolean    checkinstabreakblocks;
    public final boolean    reachCheck;
    public final double     reachDistance;
    public final ActionList reachActions;
    public final boolean    directionCheck;
    public final ActionList directionActions;
    public final double     directionPrecision;
    public final long       directionPenaltyTime;
    public final boolean    noswingCheck;
    public final ActionList noswingActions;

    public CCBlockBreak(Configuration data) {

        check = data.getBoolean(Configuration.BLOCKBREAK_CHECK);
        reachCheck = data.getBoolean(Configuration.BLOCKBREAK_REACH_CHECK);
        reachDistance = ((double) data.getInteger(Configuration.BLOCKBREAK_REACH_LIMIT)) / 100D;
        reachActions = data.getActionList(Configuration.BLOCKBREAK_REACH_ACTIONS);
        checkinstabreakblocks = data.getBoolean(Configuration.BLOCKBREAK_DIRECTION_CHECKINSTABREAKBLOCKS);
        directionCheck = data.getBoolean(Configuration.BLOCKBREAK_DIRECTION_CHECK);
        directionPrecision = ((double) data.getInteger(Configuration.BLOCKBREAK_DIRECTION_PRECISION)) / 100D;
        directionPenaltyTime = data.getInteger(Configuration.BLOCKBREAK_DIRECTION_PENALTYTIME);
        directionActions = data.getActionList(Configuration.BLOCKBREAK_DIRECTION_ACTIONS);
        noswingCheck = data.getBoolean(Configuration.BLOCKBREAK_NOSWING_CHECK);
        noswingActions = data.getActionList(Configuration.BLOCKBREAK_NOSWING_ACTIONS);

    }
}
