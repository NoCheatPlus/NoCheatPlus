package me.neatmonster.nocheatplus.checks.chat;

import java.util.LinkedList;
import java.util.List;

import me.neatmonster.nocheatplus.ConfigItem;
import me.neatmonster.nocheatplus.actions.types.ActionList;
import me.neatmonster.nocheatplus.config.ConfPaths;
import me.neatmonster.nocheatplus.config.NoCheatPlusConfiguration;
import me.neatmonster.nocheatplus.config.Permissions;

/**
 * Configurations specific for the "Chat" checks
 * Every world gets one of these assigned to it, or if a world doesn't get
 * it's own, it will use the "global" version
 * 
 */
public class ChatConfig implements ConfigItem {

    public final boolean    spamCheck;
    public final String[]   spamWhitelist;
    public final long       spamTimeframe;
    public final int        spamMessageLimit;
    public final int        spamCommandLimit;
    public final ActionList spamActions;

    public final boolean    colorCheck;
    public final ActionList colorActions;

    public ChatConfig(final NoCheatPlusConfiguration data) {

        spamCheck = data.getBoolean(ConfPaths.CHAT_SPAM_CHECK);
        spamWhitelist = splitWhitelist(data.getString(ConfPaths.CHAT_SPAM_WHITELIST));
        spamTimeframe = data.getInt(ConfPaths.CHAT_SPAM_TIMEFRAME) * 1000L;
        spamMessageLimit = data.getInt(ConfPaths.CHAT_SPAM_MESSAGELIMIT);
        spamCommandLimit = data.getInt(ConfPaths.CHAT_SPAM_COMMANDLIMIT);
        spamActions = data.getActionList(ConfPaths.CHAT_SPAM_ACTIONS, Permissions.CHAT_SPAM);
        colorCheck = data.getBoolean(ConfPaths.CHAT_COLOR_CHECK);
        colorActions = data.getActionList(ConfPaths.CHAT_COLOR_ACTIONS, Permissions.CHAT_COLOR);
    }

    /**
     * Convenience method to split a string into an array on every occurance of
     * the "," character, removing all whitespaces before and after it too.
     * 
     * @param string
     *            The string containing text seperated by ","
     * @return An array of the seperate texts
     */
    private String[] splitWhitelist(String string) {

        final List<String> strings = new LinkedList<String>();
        string = string.trim();

        for (final String s : string.split(","))
            if (s != null && s.trim().length() > 0)
                strings.add(s.trim());

        return strings.toArray(new String[strings.size()]);
    }
}
