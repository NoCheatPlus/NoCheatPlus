package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.Statistics.Id;

public class SpamCheck extends ChatCheck {

    public SpamCheck(NoCheat plugin) {
        super(plugin, "chat.spam");
    }

    public boolean check(NoCheatPlayer player, ChatData data, ChatConfig cc) {

        boolean cancel = false;
        // Maybe it's a command and on the whitelist
        for(String s : cc.spamWhitelist) {
            if(data.message.startsWith(s)) {
                // It is
                return false;
            }
        }

        int commandLimit = 0;
        int messageLimit = 0;
        int timeframe = 0;

        // Set limits depending on "proxy server check" results
        if(data.botcheckpassed || player.hasPermission(Permissions.CHAT_SPAM_BOT)) {
            commandLimit = cc.spamCommandLimit;
            messageLimit = cc.spamMessageLimit;
            timeframe = cc.spamTimeframe;
        } else {
            commandLimit = cc.spambotCommandLimit;
            messageLimit = cc.spambotMessageLimit;
            timeframe = cc.spambotTimeframe;
        }
        final long time = System.currentTimeMillis() / 1000;

        if(data.spamLastTime + timeframe <= time) {
            data.spamLastTime = time;
            data.messageCount = 0;
            data.commandCount = 0;
        }
        // Security check, if the system time changes
        else if(data.spamLastTime > time) {
            data.spamLastTime = Integer.MIN_VALUE;
        }

        if(data.message.startsWith("/"))
            data.commandCount++;
        else
            data.messageCount++;

        if(data.messageCount > messageLimit || data.commandCount > commandLimit) {

            data.spamVL = Math.max(0, data.messageCount - messageLimit);
            data.spamVL += Math.max(0, data.commandCount - commandLimit);
            incrementStatistics(player, Id.CHAT_SPAM, 1);

            cancel = executeActions(player, cc.spamActions.getActions(data.spamVL));
        }

        return cancel;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).spamVL);
        else
            return super.getParameter(wildcard, player);
    }
}
