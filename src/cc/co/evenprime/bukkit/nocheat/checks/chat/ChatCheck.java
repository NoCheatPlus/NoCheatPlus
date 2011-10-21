package cc.co.evenprime.bukkit.nocheat.checks.chat;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCChat;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;
import cc.co.evenprime.bukkit.nocheat.data.ChatData;

/**
 * 
 */
public class ChatCheck {

    private final NoCheat plugin;

    public ChatCheck(NoCheat plugin) {

        this.plugin = plugin;
    }

    public boolean check(final Player player, final String message, final ConfigurationCache cc) {

        boolean cancel = false;
        
        final CCChat ccchat = cc.chat;

        final boolean spamCheck = ccchat.spamCheck && !player.hasPermission(Permissions.CHAT_SPAM);

        if(spamCheck) {

            // Maybe it's a command and on the whitelist
            for(String s : ccchat.spamWhitelist) {
                if(message.startsWith(s)) {
                    // It is
                    return false;
                }
            }

            final int time = plugin.getIngameSeconds();

            final BaseData data = plugin.getData(player.getName());
            final ChatData chat = data.chat;

            if(chat.spamLasttime + ccchat.spamTimeframe <= time) {
                chat.spamLasttime = time;
                chat.messageCount = 0;
            }

            chat.messageCount++;

            if(chat.messageCount > ccchat.spamLimit) {

                // Prepare some event-specific values for logging and custom
                // actions

                data.log.check = "chat.spam";
                data.log.text = message;

                cancel = plugin.execute(player, ccchat.spamActions, chat.messageCount - ccchat.spamLimit, chat.history, cc);
            }
        }

        return cancel;
    }

}
