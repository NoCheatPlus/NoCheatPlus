package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.EventType;
import cc.co.evenprime.bukkit.nocheat.events.EventManagerImpl;

public class ChatEventManager extends EventManagerImpl {

    private final List<ChatCheck> checks;

    public ChatEventManager(NoCheat plugin) {

        super(plugin);

        this.checks = new ArrayList<ChatCheck>(2);
        this.checks.add(new EmptyCheck(plugin));
        this.checks.add(new SpamCheck(plugin));

        registerListener(Event.Type.PLAYER_CHAT, Priority.Lowest, true, plugin.getPerformance(EventType.CHAT));
        registerListener(Event.Type.PLAYER_COMMAND_PREPROCESS, Priority.Lowest, true, plugin.getPerformance(EventType.CHAT));
    }

    @Override
    protected void handlePlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event, final Priority priority) {
        handlePlayerChatEvent((PlayerChatEvent) event, priority);
    }

    @Override
    protected void handlePlayerChatEvent(final PlayerChatEvent event, final Priority priority) {

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
