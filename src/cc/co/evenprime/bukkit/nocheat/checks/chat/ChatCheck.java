package cc.co.evenprime.bukkit.nocheat.checks.chat;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;

public abstract class ChatCheck extends Check {

    private static final String id = "chat";

    public ChatCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, ChatData data, CCChat cc);

    public abstract boolean isEnabled(CCChat cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return getData(player.getDataStore()).history;
    }

    @Override
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.TEXT)
            return getData(player.getDataStore()).message;
        else
            return super.getParameter(wildcard, player);
    }

    public static ChatData getData(DataStore base) {
        ChatData data = base.get(id);
        if(data == null) {
            data = new ChatData();
            base.set(id, data);
        }
        return data;
    }

    public static CCChat getConfig(ConfigurationCacheStore cache) {
        CCChat config = cache.get(id);
        if(config == null) {
            config = new CCChat(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }
}
