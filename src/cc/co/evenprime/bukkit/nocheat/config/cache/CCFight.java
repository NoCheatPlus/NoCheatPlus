package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;

public class CCFight {

    public final boolean    check;
    public final boolean    directionCheck;
    public final double     directionPrecision;
    public final ActionList directionActions;
    public final long       directionPenaltyTime;

    public CCFight(Configuration data) {

        check = data.getBoolean(Configuration.FIGHT_CHECK);
        directionCheck = data.getBoolean(Configuration.FIGHT_DIRECTION_CHECK);
        directionPrecision = ((double) (data.getInteger(Configuration.FIGHT_DIRECTION_PRECISION))) / 100D;
        directionPenaltyTime = data.getInteger(Configuration.FIGHT_DIRECTION_PENALTYTIME);
        directionActions = data.getActionList(Configuration.FIGHT_DIRECTION_ACTIONS);
    }
}
