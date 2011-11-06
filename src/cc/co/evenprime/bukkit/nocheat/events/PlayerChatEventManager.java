package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.ChatCheck;
import cc.co.evenprime.bukkit.nocheat.checks.chat.SpamCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCChat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;
import cc.co.evenprime.bukkit.nocheat.debug.Performance;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * 
 */
public class PlayerChatEventManager extends PlayerListener implements EventManager {

    private final NoCheat         plugin;
    private final List<ChatCheck> checks;
    private final Performance     chatPerformance;

    public PlayerChatEventManager(NoCheat plugin) {

        this.plugin = plugin;

        this.checks = new ArrayList<ChatCheck>(1);
        this.checks.add(new SpamCheck(plugin));

        this.chatPerformance = plugin.getPerformance(Type.CHAT);

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_CHAT, this, Priority.Lowest, plugin);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Priority.Lowest, plugin);
    }

    @Override
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        // We redirect to the other method anyway, so no need to set up a
        // performance counter here
        onPlayerChat(event);
    }

    @Override
    public void onPlayerChat(final PlayerChatEvent event) {

        if(event.isCancelled()) {
            return;
        }

        // Performance counter setup
        long nanoTimeStart = 0;
        final boolean performanceCheck = chatPerformance.isEnabled();

        if(performanceCheck)
            nanoTimeStart = System.nanoTime();

        handleEvent(event);

        // store performance time
        if(performanceCheck)
            chatPerformance.addTime(System.nanoTime() - nanoTimeStart);
    }

    private void handleEvent(PlayerChatEvent event) {
        boolean cancelled = false;

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer().getName());

        if(!player.getConfiguration().chat.check || player.hasPermission(Permissions.CHAT)) {
            return;
        }

        CCChat cc = player.getConfiguration().chat;
        ChatData data = player.getData().chat;

        data.message = event.getMessage();

        for(ChatCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                check.check(player, data, cc);
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
