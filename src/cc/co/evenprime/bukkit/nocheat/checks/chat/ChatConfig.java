package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.LinkedList;
import java.util.List;
import cc.co.evenprime.bukkit.nocheat.ConfigItem;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.ConfPaths;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class ChatConfig implements ConfigItem {

    public final boolean    spamCheck;
    public final String[]   spamWhitelist;
    public final int        spamTimeframe;
    public final int        spamMessageLimit;
    public final ActionList spamActions;
    public final boolean    colorCheck;
    public final ActionList colorActions;
    public final int        spamCommandLimit;

    public ChatConfig(NoCheatConfiguration data) {

        spamCheck = data.getBoolean(ConfPaths.CHAT_SPAM_CHECK);
        spamWhitelist = splitWhitelist(data.getString(ConfPaths.CHAT_SPAM_WHITELIST));
        spamTimeframe = data.getInt(ConfPaths.CHAT_SPAM_TIMEFRAME);
        spamMessageLimit = data.getInt(ConfPaths.CHAT_SPAM_MESSAGELIMIT);
        spamCommandLimit = data.getInt(ConfPaths.CHAT_SPAM_COMMANDLIMIT);
        spamActions = data.getActionList(ConfPaths.CHAT_SPAM_ACTIONS, Permissions.CHAT_SPAM);
        colorCheck = data.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = data.getActionList(ConfPaths.CHAT_COLOR_ACTIONS, Permissions.CHAT_COLOR);
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
