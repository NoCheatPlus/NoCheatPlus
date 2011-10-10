package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.DefaultConfiguration;

/**
 * Configurations specific for the "BlockBreak" checks
 * Every world gets one of these assigned to it.
 * 
 * @author Evenprime
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

    public CCBlockBreak(Configuration data) {

        check = data.getBoolean(DefaultConfiguration.BLOCKBREAK_CHECK);
        reachCheck = data.getBoolean(DefaultConfiguration.BLOCKBREAK_REACH_CHECK);
        reachDistance = ((double) data.getInteger(DefaultConfiguration.BLOCKBREAK_REACH_LIMIT)) / 100D;
        reachActions = data.getActionList(DefaultConfiguration.BLOCKBREAK_REACH_ACTIONS);
        checkinstabreakblocks = data.getBoolean(DefaultConfiguration.BLOCKBREAK_DIRECTION_CHECKINSTABREAKBLOCKS);
        directionCheck = data.getBoolean(DefaultConfiguration.BLOCKBREAK_DIRECTION_CHECK);
        directionActions = data.getActionList(DefaultConfiguration.BLOCKBREAK_DIRECTION_ACTIONS);
    }
}
