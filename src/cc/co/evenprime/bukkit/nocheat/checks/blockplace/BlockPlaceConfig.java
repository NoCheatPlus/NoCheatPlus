package cc.co.evenprime.bukkit.nocheat.checks.blockplace;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

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

    public BlockPlaceConfig(NoCheatConfiguration data) {

        reachCheck = data.getBoolean(ConfPaths.BLOCKPLACE_REACH_CHECK);
        reachDistance = 535D / 100D;
        reachActions = data.getActionList(ConfPaths.BLOCKPLACE_REACH_ACTIONS);

        directionCheck = data.getBoolean(ConfPaths.BLOCKPLACE_DIRECTION_CHECK);
        directionPenaltyTime = data.getInt(ConfPaths.BLOCKPLACE_DIRECTION_PENALTYTIME);
        directionPrecision = ((double) data.getInt(ConfPaths.BLOCKPLACE_DIRECTION_PRECISION)) / 100D;
        directionActions = data.getActionList(ConfPaths.BLOCKPLACE_DIRECTION_ACTIONS);

        check = reachCheck || directionCheck;

    }
}
