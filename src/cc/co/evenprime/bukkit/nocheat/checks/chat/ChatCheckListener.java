package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import cc.co.evenprime.bukkit.nocheat.EventManager;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

public class ChatCheckListener implements Listener, EventManager {

    private final SpamCheck   spamCheck;
    private final ColorCheck  colorCheck;

    private final NoCheat     plugin;

    public ChatCheckListener(NoCheat plugin) {

        this.plugin = plugin;

        spamCheck = new SpamCheck(plugin);
        colorCheck = new ColorCheck(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void commandPreprocess(final PlayerCommandPreprocessEvent event) {
        chat((PlayerChatEvent) event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chat(final PlayerChatEvent event) {

        if(event.isCancelled())
            return;

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final ChatConfig cc = ChatCheck.getConfig(player.getConfigurationStore());
        final ChatData data = ChatCheck.getData(player.getDataStore());

        data.message = event.getMessage();

        // Now do the actual checks
        if(cc.spamCheck && !player.hasPermission(Permissions.CHAT_SPAM)) {
            cancelled = spamCheck.check(player, data, cc);
        }
        if(!cancelled && cc.colorCheck && !player.hasPermission(Permissions.CHAT_COLOR)) {
            cancelled = colorCheck.check(player, data, cc);
        }

        if(cancelled) {
            event.setCancelled(cancelled);
        } else {
            event.setMessage(data.message);
        }
    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        ChatConfig c = ChatCheck.getConfig(cc);
        if(c.spamCheck)
            s.add("chat.spam");
        if(c.colorCheck)
            s.add("chat.color");
        return s;
    }
}
