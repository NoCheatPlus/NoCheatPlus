package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;

public class CCTimed {

    public final boolean    check;
    public final boolean    godmodeCheck;
    public final double     godmodeTicksLimit;
    public final ActionList godmodeActions;

    public CCTimed(Configuration data) {

        check = data.getBoolean(Configuration.TIMED_CHECK);
        godmodeCheck = data.getBoolean(Configuration.TIMED_GODMODE_CHECK);
        godmodeTicksLimit = data.getInteger(Configuration.TIMED_GODMODE_TICKSLIMIT);
        godmodeActions = data.getActionList(Configuration.TIMED_GODMODE_ACTIONS);
    }
}
