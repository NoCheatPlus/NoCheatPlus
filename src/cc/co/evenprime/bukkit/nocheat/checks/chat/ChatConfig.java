package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.LinkedList;
import java.util.List;
import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;

public class ChatConfig implements ConfigItem {

    public final boolean    spamCheck;
    public final String[]   spamWhitelist;
    public final int        spamTimeframe;
    public final int        spamMessageLimit;
    public final ActionList spamActions;
    public final boolean    colorCheck;
    public final ActionList colorActions;
    public final int        spamCommandLimit;
    public final boolean    spambotCheck;
    public final ActionList spambotActions;
    public final int        spambotCommandLimit;
    public final int        spambotMessageLimit;
    public final int        spambotTimeframe;

    public ChatConfig(NoCheatConfiguration data) {

        spamCheck = data.getBoolean(ConfPaths.CHAT_SPAM_CHECK);
        spamWhitelist = splitWhitelist(data.getString(ConfPaths.CHAT_SPAM_WHITELIST));
        spamTimeframe = data.getInt(ConfPaths.CHAT_SPAM_TIMEFRAME);
        spamMessageLimit = data.getInt(ConfPaths.CHAT_SPAM_MESSAGELIMIT);
        spamCommandLimit = data.getInt(ConfPaths.CHAT_SPAM_COMMANDLIMIT);
        spamActions = data.getActionList(ConfPaths.CHAT_SPAM_ACTIONS);
        colorCheck = data.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = data.getActionList(ConfPaths.CHAT_COLOR_ACTIONS);
        spambotCheck = data.getBoolean(ConfPaths.CHAT_SPAMBOT_CHECK);
        spambotActions = data.getActionList(ConfPaths.CHAT_SPAMBOT_ACTIONS);
        spambotCommandLimit = data.getInt(ConfPaths.CHAT_SPAMBOT_COMMANDLIMIT);
        spambotMessageLimit = data.getInt(ConfPaths.CHAT_SPAMBOT_MESSAGELIMIT);
        spambotTimeframe = data.getInt(ConfPaths.CHAT_SPAMBOT_TIMEFRAME);
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
