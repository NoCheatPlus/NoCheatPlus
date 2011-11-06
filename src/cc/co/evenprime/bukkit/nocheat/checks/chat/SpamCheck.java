package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.checks.ChatCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCChat;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;

public class SpamCheck extends ChatCheck {

    public SpamCheck(NoCheat plugin) {
        super(plugin, "chat.spam", Permissions.CHAT_SPAM);
    }

    public boolean check(NoCheatPlayer player, ChatData data, CCChat cc) {

        boolean cancel = false;
        // Maybe it's a command and on the whitelist
        for(String s : cc.spamWhitelist) {
            if(data.message.startsWith(s)) {
                // It is
                return false;
            }
        }

        final int time = plugin.getIngameSeconds();

        if(data.spamLasttime + cc.spamTimeframe <= time) {
            data.spamLasttime = time;
            data.messageCount = 0;
        }

        data.messageCount++;

        if(data.messageCount > cc.spamLimit) {

            data.spamVL = data.messageCount - cc.spamLimit;
            cancel = executeActions(player, cc.spamActions.getActions(data.spamVL));
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCChat cc) {
        return cc.spamCheck;
    }

    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case VIOLATIONS:
            return String.format(Locale.US, "%d", player.getData().chat.spamVL);
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
