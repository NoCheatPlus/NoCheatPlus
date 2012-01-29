package cc.co.evenprime.bukkit.nocheat.checks.fight;

import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

public class FightConfig implements ConfigItem {

    public final boolean    check;
    public final boolean    directionCheck;
    public final double     directionPrecision;
    public final ActionList directionActions;
    public final long       directionPenaltyTime;
    public final boolean    noswingCheck;
    public final ActionList noswingActions;

    public FightConfig(NoCheatConfiguration data) {

        directionCheck = data.getBoolean(ConfPaths.FIGHT_DIRECTION_CHECK);
        directionPrecision = ((double) (data.getInt(ConfPaths.FIGHT_DIRECTION_PRECISION))) / 100D;
        directionPenaltyTime = data.getInt(ConfPaths.FIGHT_DIRECTION_PENALTYTIME);
        directionActions = data.getActionList(ConfPaths.FIGHT_DIRECTION_ACTIONS);
        noswingCheck = data.getBoolean(ConfPaths.FIGHT_NOSWING_CHECK);
        noswingActions = data.getActionList(ConfPaths.FIGHT_NOSWING_ACTIONS);

        check = directionCheck || noswingCheck;
    }
}
