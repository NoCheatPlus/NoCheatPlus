package cc.co.evenprime.bukkit.nocheat.config.cache;

import java.util.LinkedList;
import java.util.List;

import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.util.ActionList;

public class CCChat {

    public final boolean    check;
    public final boolean    spamCheck;
    public final String[]   spamWhitelist;
    public final int        spamTimeframe;
    public final int        spamLimit;
    public final ActionList spamActions;

    public CCChat(Configuration data) {

        check = data.getBoolean(Configuration.CHAT_CHECK);
        spamCheck = data.getBoolean(Configuration.CHAT_SPAM_CHECK);
        spamWhitelist = splitWhitelist(data.getString(Configuration.CHAT_SPAM_WHITELIST));
        spamTimeframe = data.getInteger(Configuration.CHAT_SPAM_TIMEFRAME);
        spamLimit = data.getInteger(Configuration.CHAT_SPAM_LIMIT);
        spamActions = data.getActionList(Configuration.CHAT_SPAM_ACTIONS);

    }

    private String[] splitWhitelist(String string) {

        List<String> strings = new LinkedList<String>();
        string = string.trim();

        for(String s : string.split(",")) {
            if(s != null && s.trim().length() > 0) {
                strings.add(s.trim());
            }
        }

        return strings.toArray(new String[strings.size()]);
    }
}
