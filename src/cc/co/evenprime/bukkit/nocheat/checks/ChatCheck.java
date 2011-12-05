package cc.co.evenprime.bukkit.nocheat.checks;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.WildCard;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCChat;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;

public abstract class ChatCheck extends Check {

    public ChatCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, ChatData data, CCChat cc);

    public abstract boolean isEnabled(CCChat cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return player.getData().chat.history;
    }

    @Override
    public String getParameter(WildCard wildcard, NoCheatPlayer player) {
        switch (wildcard) {

        case TEXT: {
            return player.getData().chat.message;
        }

        default:
            return super.getParameter(wildcard, player);
        }
    }
}
