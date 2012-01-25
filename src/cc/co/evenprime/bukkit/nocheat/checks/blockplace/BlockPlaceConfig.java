package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;

/**
 * 
 */
public class BlockPlaceConfig implements ConfigItem {

    public final boolean    check;

    public final boolean    reachCheck;
    public final double     reachDistance;
    public final ActionList reachActions;

    public final boolean    directionCheck;
    public final ActionList directionActions;
    public final long       directionPenaltyTime;
    public final double     directionPrecision;

    public BlockPlaceConfig(Configuration data) {

        check = data.getBoolean(Configuration.BLOCKPLACE_CHECK);

        reachCheck = data.getBoolean(Configuration.BLOCKPLACE_REACH_CHECK);
        reachDistance = 535D / 100D;
        reachActions = data.getActionList(Configuration.BLOCKPLACE_REACH_ACTIONS);

        directionCheck = data.getBoolean(Configuration.BLOCKPLACE_DIRECTION_CHECK);
        directionPenaltyTime = data.getInteger(Configuration.BLOCKPLACE_DIRECTION_PENALTYTIME);
        directionPrecision = ((double) data.getInteger(Configuration.BLOCKPLACE_DIRECTION_PRECISION)) / 100D;
        directionActions = data.getActionList(Configuration.BLOCKPLACE_DIRECTION_ACTIONS);

    }
}
