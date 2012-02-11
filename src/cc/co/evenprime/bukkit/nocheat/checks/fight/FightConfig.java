package cc.co.evenprime.bukkit.nocheat.checks.fight;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

public class FightConfig implements ConfigItem {

    public final boolean    directionCheck;
    public final double     directionPrecision;
    public final ActionList directionActions;
    public final long       directionPenaltyTime;
    public final boolean    noswingCheck;
    public final ActionList noswingActions;
    public final boolean    reachCheck;
    public final double     reachLimit;
    public final long       reachPenaltyTime;
    public final ActionList reachActions;
    public final int        speedAttackLimit;
    public final ActionList speedActions;
    public final boolean    speedCheck;
    public final boolean    godmodeCheck;
    public final ActionList godmodeActions;

    public final boolean    damageChecks;

    public FightConfig(NoCheatConfiguration data) {

        directionCheck = data.getBoolean(ConfPaths.FIGHT_DIRECTION_CHECK);
        directionPrecision = ((double) (data.getInt(ConfPaths.FIGHT_DIRECTION_PRECISION))) / 100D;
        directionPenaltyTime = data.getInt(ConfPaths.FIGHT_DIRECTION_PENALTYTIME);
        directionActions = data.getActionList(ConfPaths.FIGHT_DIRECTION_ACTIONS);
        noswingCheck = data.getBoolean(ConfPaths.FIGHT_NOSWING_CHECK);
        noswingActions = data.getActionList(ConfPaths.FIGHT_NOSWING_ACTIONS);
        reachCheck = data.getBoolean(ConfPaths.FIGHT_REACH_CHECK);
        reachLimit = ((double) (data.getInt(ConfPaths.FIGHT_REACH_LIMIT))) / 100D;
        reachPenaltyTime = data.getInt(ConfPaths.FIGHT_REACH_PENALTYTIME);
        reachActions = data.getActionList(ConfPaths.FIGHT_REACH_ACTIONS);
        speedCheck = data.getBoolean(ConfPaths.FIGHT_SPEED_CHECK);
        speedActions = data.getActionList(ConfPaths.FIGHT_SPEED_ACTIONS);
        speedAttackLimit = data.getInt(ConfPaths.FIGHT_SPEED_ATTACKLIMIT);

        godmodeCheck = data.getBoolean(ConfPaths.FIGHT_GODMODE_CHECK);
        godmodeActions = data.getActionList(ConfPaths.FIGHT_GODMODE_ACTIONS);

        damageChecks = directionCheck || noswingCheck || reachCheck || speedCheck;
    }
}
