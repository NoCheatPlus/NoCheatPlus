package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;

/**
 * 
 */
public class CCBlockPlace {

    public final boolean    check;
    public final boolean    onliquidCheck;
    public final ActionList onliquidActions;

    public final boolean    reachCheck;
    public final double     reachDistance;
    public final ActionList reachActions;

    public final boolean    noswingCheck;
    public final ActionList noswingActions;

    public CCBlockPlace(Configuration data) {

        check = data.getBoolean(Configuration.BLOCKPLACE_CHECK);

        onliquidCheck = data.getBoolean(Configuration.BLOCKPLACE_ONLIQUID_CHECK);
        onliquidActions = data.getActionList(Configuration.BLOCKPLACE_ONLIQUID_ACTIONS);

        reachCheck = data.getBoolean(Configuration.BLOCKPLACE_REACH_CHECK);
        reachDistance = data.getInteger(Configuration.BLOCKPLACE_REACH_LIMIT);
        reachActions = data.getActionList(Configuration.BLOCKPLACE_REACH_ACTIONS);

        noswingCheck = data.getBoolean(Configuration.BLOCKPLACE_NOSWING_CHECK);
        noswingActions = data.getActionList(Configuration.BLOCKPLACE_NOSWING_ACTIONS);
    }
}
