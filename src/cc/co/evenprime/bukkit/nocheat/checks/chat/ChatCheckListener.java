package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
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
import cc.co.evenprime.bukkit.nocheat.data.ChatData;

public class ChatCheckListener implements Listener, EventManager {

    private final List<ChatCheck> checks;
    private NoCheat               plugin;

    public ChatCheckListener(NoCheat plugin) {

        this.checks = new ArrayList<ChatCheck>(3);
        this.checks.add(new EmptyCheck(plugin));
        this.checks.add(new SpamCheck(plugin));
        this.checks.add(new ColorCheck(plugin));

        this.plugin = plugin;
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        final CCChat cc = ChatCheck.getConfig(player.getConfigurationStore());

        if(!cc.check || player.hasPermission(Permissions.CHAT)) {
            return;
        }

        final ChatData data = ChatCheck.getData(player.getDataStore());

        data.message = event.getMessage();

        for(ChatCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                cancelled = check.check(player, data, cc);
            }
        }

        if(cancelled) {
            event.setCancelled(cancelled);
        } else {
            event.setMessage(data.message);
        }

    }

    public List<String> getActiveChecks(ConfigurationCacheStore cc) {
        LinkedList<String> s = new LinkedList<String>();

        CCChat c = ChatCheck.getConfig(cc);
        if(c.check && c.spamCheck)
            s.add("chat.spam");
        if(c.check && c.emptyCheck)
            s.add("chat.empty");
        return s;
    }
}
