package me.neatmonster.nocheatplus.checks.blockbreak;

import me.neatmonster.nocheatplus.ConfigItem;
import me.neatmonster.nocheatplus.actions.types.ActionList;
import me.neatmonster.nocheatplus.config.ConfPaths;
import me.neatmonster.nocheatplus.config.NoCheatPlusConfiguration;
import me.neatmonster.nocheatplus.config.Permissions;

/**
 * Configurations specific for the "BlockBreak" checks
 * Every world gets one of these assigned to it, or if a world doesn't get
 * it's own, it will use the "global" version
 * 
 */
public class BlockBreakConfig implements ConfigItem {

    public final boolean    fastBreakCheck;
    public final int        fastBreakIntervalSurvival;
    public final int        fastBreakIntervalCreative;
    public final ActionList fastBreakActions;

    public final boolean    reachCheck;
    public final double     reachDistance;
    public final ActionList reachActions;

    public final boolean    directionCheck;
    public final ActionList directionActions;
    public final double     directionPrecision;
    public final long       directionPenaltyTime;

    public final boolean    noswingCheck;
    public final ActionList noswingActions;

    public BlockBreakConfig(final NoCheatPlusConfiguration data) {

        fastBreakCheck = data.getBoolean(ConfPaths.BLOCKBREAK_FASTBREAK_CHECK);
        fastBreakIntervalSurvival = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_INTERVALSURVIVAL);
        fastBreakIntervalCreative = data.getInt(ConfPaths.BLOCKBREAK_FASTBREAK_INTERVALCREATIVE);
        fastBreakActions = data.getActionList(ConfPaths.BLOCKBREAK_FASTBREAK_ACTIONS, Permissions.BLOCKBREAK_FASTBREAK);
        reachCheck = data.getBoolean(ConfPaths.BLOCKBREAK_REACH_CHECK);
        reachDistance = 535D / 100D;
        reachActions = data.getActionList(ConfPaths.BLOCKBREAK_REACH_ACTIONS, Permissions.BLOCKBREAK_REACH);
        directionCheck = data.getBoolean(ConfPaths.BLOCKBREAK_DIRECTION_CHECK);
        directionPrecision = data.getInt(ConfPaths.BLOCKBREAK_DIRECTION_PRECISION) / 100D;
        directionPenaltyTime = data.getInt(ConfPaths.BLOCKBREAK_DIRECTION_PENALTYTIME);
        directionActions = data.getActionList(ConfPaths.BLOCKBREAK_DIRECTION_ACTIONS, Permissions.BLOCKBREAK_DIRECTION);
        noswingCheck = data.getBoolean(ConfPaths.BLOCKBREAK_NOSWING_CHECK);
        noswingActions = data.getActionList(ConfPaths.BLOCKBREAK_NOSWING_ACTIONS, Permissions.BLOCKBREAK_NOSWING);
    }
}
