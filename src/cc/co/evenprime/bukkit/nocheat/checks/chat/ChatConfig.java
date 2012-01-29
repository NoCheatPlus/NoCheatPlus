package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.LinkedList;
import java.util.List;
import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

public class ChatConfig implements ConfigItem {

    public final boolean    check;
    public final boolean    spamCheck;
    public final String[]   spamWhitelist;
    public final int        spamTimeframe;
    public final int        spamLimit;
    public final ActionList spamActions;
    public final boolean    colorCheck;
    public final ActionList colorActions;

    public ChatConfig(NoCheatConfiguration data) {

        spamCheck = data.getBoolean(ConfPaths.CHAT_SPAM_CHECK);
        spamWhitelist = splitWhitelist(data.getString(ConfPaths.CHAT_SPAM_WHITELIST));
        spamTimeframe = data.getInt(ConfPaths.CHAT_SPAM_TIMEFRAME);
        spamLimit = data.getInt(ConfPaths.CHAT_SPAM_LIMIT);
        spamActions = data.getActionList(ConfPaths.CHAT_SPAM_ACTIONS);
        colorCheck = data.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = data.getActionList(ConfPaths.CHAT_COLOR_ACTIONS);

        check = spamCheck || colorCheck;

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
