package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class SpamCheck extends ChatCheck {

    public SpamCheck(NoCheat plugin) {
        super(plugin, "chat.spam", Permissions.CHAT_SPAM);
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

        final long time = System.currentTimeMillis() / 1000;

        if(data.spamLastTime + cc.spamTimeframe <= time) {
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

        if(data.messageCount > cc.spamLimit || data.commandCount > cc.commandLimit) {

            data.spamVL = Math.max(0, data.messageCount - cc.spamLimit);
            data.spamVL += Math.max(0, data.commandCount - cc.commandLimit);
            data.spamTotalVL++;
            data.spamFailed++;

            cancel = executeActions(player, cc.spamActions.getActions(data.spamVL));
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(ChatConfig cc) {
        return cc.spamCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).spamVL);
        else
            return super.getParameter(wildcard, player);
    }
}
