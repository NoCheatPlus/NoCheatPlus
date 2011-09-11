package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;

public class CCChat {

    public final boolean    check;
    public final boolean    spamCheck;
    public final int        spamTimeframe;
    public final int        spamLimit;
    public final ActionList spamActions;

    public CCChat(Configuration data) {

        check = data.getBoolean("chat.check");
        spamCheck = data.getBoolean("chat.spam.check");
        spamTimeframe = data.getInteger("chat.spam.timeframe");
        spamLimit = data.getInteger("chat.spam.limit");
        spamActions = data.getActionList("chat.spam.actions");

    }
}
