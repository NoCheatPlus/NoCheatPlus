package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.ChatCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCChat;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;

public class EmptyCheck extends ChatCheck {

    public EmptyCheck(NoCheat plugin) {
        super(plugin, "chat.empty", Permissions.CHAT_EMPTY);
    }

    public boolean check(NoCheatPlayer player, ChatData data, CCChat cc) {

        boolean cancel = false;

        if(data.message.trim().length() == 0) {

            data.emptyVL += 1;
            data.emptyTotalVL += 1;
            data.emptyFailed++;

            cancel = executeActions(player, cc.emptyActions.getActions(data.emptyVL));
        }

        return cancel;
    }

    @Override
    public boolean isEnabled(CCChat cc) {
        return cc.emptyCheck;
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", player.getData().chat.emptyVL);
        else
            return super.getParameter(wildcard, player);
    }
}
