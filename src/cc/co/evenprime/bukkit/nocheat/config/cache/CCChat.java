package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.DefaultConfiguration;

public class CCChat {

    public final boolean    check;
    public final boolean    spamCheck;
    public final int        spamTimeframe;
    public final int        spamLimit;
    public final ActionList spamActions;

    public CCChat(Configuration data) {

        check = data.getBoolean(DefaultConfiguration.CHAT_CHECK);
        spamCheck = data.getBoolean(DefaultConfiguration.CHAT_SPAM_CHECK);
        spamTimeframe = data.getInteger(DefaultConfiguration.CHAT_SPAM_TIMEFRAME);
        spamLimit = data.getInteger(DefaultConfiguration.CHAT_SPAM_LIMIT);
        spamActions = data.getActionList(DefaultConfiguration.CHAT_SPAM_ACTIONS);

    }
}
