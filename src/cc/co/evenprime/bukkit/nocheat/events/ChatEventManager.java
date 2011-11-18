package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.ChatCheck;
import cc.co.evenprime.bukkit.nocheat.checks.chat.SpamCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCChat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

public class ChatEventManager extends EventManager {

    private final List<ChatCheck> checks;

    public ChatEventManager(NoCheat plugin) {

        super(plugin);

        this.checks = new ArrayList<ChatCheck>(1);
        this.checks.add(new SpamCheck(plugin));

        registerListener(Event.Type.PLAYER_CHAT, Priority.Lowest, true, plugin.getPerformance(Type.CHAT));
        registerListener(Event.Type.PLAYER_COMMAND_PREPROCESS, Priority.Lowest, true, plugin.getPerformance(Type.CHAT));
    }

    @Override
    protected void handlePlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event, final Priority priority) {
        handlePlayerChatEvent((PlayerChatEvent) event, priority);
    }

    @Override
    protected void handlePlayerChatEvent(final PlayerChatEvent event, final Priority priority) {

        boolean cancelled = false;

        final NoCheatPlayer player = plugin.getPlayer(event.getPlayer());
        final CCChat cc = player.getConfiguration().chat;

        if(!cc.check || player.hasPermission(Permissions.CHAT)) {
            return;
        }

        final ChatData data = player.getData().chat;

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

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.chat.check && cc.chat.spamCheck)
            s.add("chat.spam");
        return s;
    }
}
