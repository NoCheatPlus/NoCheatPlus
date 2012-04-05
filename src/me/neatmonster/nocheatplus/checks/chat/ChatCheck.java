package me.neatmonster.nocheatplus.checks.chat;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.checks.Check;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;

/**
 * Abstract base class for Chat checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class ChatCheck extends Check {

    private static final String id = "chat";

    public static ChatConfig getConfig(final ConfigurationCacheStore cache) {
        ChatConfig config = cache.get(id);
        if (config == null) {
            config = new ChatConfig(cache.getConfiguration());
            cache.set(id, config);
        }
        return config;
    }

    /**
     * Get the ChatConfig object that belongs to the world that the player
     * currently resides in.
     * 
     * @param player
     * @return
     */
    public static ChatConfig getConfig(final NoCheatPlusPlayer player) {
        return getConfig(player.getConfigurationStore());
    }

    /**
     * Get the "ChatData" object that belongs to the player. Will ensure
     * that such a object exists and if not, create one
     * 
     * @param player
     * @return
     */
    public static ChatData getData(final NoCheatPlusPlayer player) {
        final DataStore base = player.getDataStore();
        ChatData data = base.get(id);
        if (data == null) {
            data = new ChatData();
            base.set(id, data);
        }
        return data;
    }

    public ChatCheck(final NoCheatPlus plugin, final String name) {
        super(plugin, id, name);
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.TEXT)
            // Filter colors from the players message when logging
            return getData(player).message.replaceAll("\302\247.", "").replaceAll("\247.", "");
        else
            return super.getParameter(wildcard, player);
    }
}
