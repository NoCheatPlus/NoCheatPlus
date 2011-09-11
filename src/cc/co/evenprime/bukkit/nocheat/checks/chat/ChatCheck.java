package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.HashMap;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.Permissions;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutor;
import cc.co.evenprime.bukkit.nocheat.actions.ActionExecutorWithHistory;
import cc.co.evenprime.bukkit.nocheat.actions.types.LogAction;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;

/**
 * 
 * @author Evenprime
 *
 */
public class ChatCheck {

    private final ActionExecutor action;
    private final NoCheat        plugin;

    public ChatCheck(NoCheat plugin) {

        action = new ActionExecutorWithHistory(plugin);
        this.plugin = plugin;
    }

    public boolean check(Player player, String message, ChatData data, ConfigurationCache cc) {

        boolean cancel = false;

        boolean spamCheck = cc.chat.spamCheck && !player.hasPermission(Permissions.CHAT_SPAM);

        if(spamCheck) {

            int time = plugin.getIngameSeconds();

            if(data.spamLasttime + cc.chat.spamTimeframe <= plugin.getIngameSeconds()) {
                data.spamLasttime = time;
                data.messageCount = 0;
            }

            data.messageCount++;

            if(data.messageCount > cc.chat.spamLimit) {

                // Prepare some event-specific values for logging and custom
                // actions
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(LogAction.CHECK, "chat.spam");
                params.put(LogAction.TEXT, message);
                cancel = action.executeActions(player, cc.chat.spamActions, data.messageCount - cc.chat.spamLimit, params, cc);
            }
        }
        
        return cancel;
    }

}
