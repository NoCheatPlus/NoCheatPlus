package cc.co.evenprime.bukkit.nocheat.events;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.checks.chat.ChatCheck;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationManager;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;
import cc.co.evenprime.bukkit.nocheat.data.DataManager;

/**
 * 
 * @author Evenprime
 *
 */
public class PlayerChatEventManager extends PlayerListener implements EventManager {

    private final ChatCheck            chatCheck;
    private final DataManager          data;
    private final ConfigurationManager config;

    public PlayerChatEventManager(NoCheat plugin) {

        this.data = plugin.getDataManager();
        this.config = plugin.getConfigurationManager();
        this.chatCheck = new ChatCheck(plugin);

        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_CHAT, this, Priority.Lowest, plugin);
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {

        if(event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ConfigurationCache cc = config.getConfigurationCacheForWorld(player.getWorld().getName());

        // Find out if checks need to be done for that player
        if(cc.chat.check && !player.hasPermission(Permissions.CHAT)) {

            boolean cancel = false;

            // Get the player-specific stored data that applies here
            final ChatData data = this.data.getChatData(player);

            cancel = chatCheck.check(player, event.getMessage(), data, cc);

            if(cancel) {
                event.setCancelled(true);
            }
        }

    }

    @Override
    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.chat.check && cc.chat.spamCheck)
            s.add("chat.spam");
        return s;
    }

    @Override
    public List<String> getInactiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(!(cc.chat.check && cc.chat.spamCheck))
            s.add("chat.spam");

        return s;
    }
}
