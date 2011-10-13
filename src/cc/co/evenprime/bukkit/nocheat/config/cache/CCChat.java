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

        check = data.getBoolean(Configuration.CHAT_CHECK);
        spamCheck = data.getBoolean(Configuration.CHAT_SPAM_CHECK);
        spamTimeframe = data.getInteger(Configuration.CHAT_SPAM_TIMEFRAME);
        spamLimit = data.getInteger(Configuration.CHAT_SPAM_LIMIT);
        spamActions = data.getActionList(Configuration.CHAT_SPAM_ACTIONS);

    }
}
