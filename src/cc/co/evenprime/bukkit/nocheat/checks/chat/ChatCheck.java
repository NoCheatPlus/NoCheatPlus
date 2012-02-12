package cc.co.evenprime.bukkit.nocheat.checks.chat;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.checks.Check;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;

public abstract class ChatCheck extends Check {

    private static final String id = "chat";

    public ChatCheck(NoCheat plugin, String name) {
        super(plugin, id, name);
    }

    @Override
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.TEXT)
            // Filter colors from the players message when logging
            return getData(player.getDataStore()).message.replaceAll("\302\247.", "").replaceAll("\247.", "");
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

    public static ChatConfig getConfig(ConfigurationCacheStore cache) {
        ChatConfig config = cache.get(id);
        if(config == null) {
            config = new ChatConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }
}
