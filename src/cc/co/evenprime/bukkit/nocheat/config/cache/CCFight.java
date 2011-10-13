package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;


public class CCFight {
    public final boolean  check;
    public final boolean  directionCheck;
    public final ActionList directionActions;

    public CCFight(Configuration data) {

        check = data.getBoolean(Configuration.FIGHT_CHECK);
        directionCheck = data.getBoolean(Configuration.FIGHT_DIRECTION_CHECK);
        directionActions = data.getActionList(Configuration.FIGHT_DIRECTION_ACTIONS);
    }
}
