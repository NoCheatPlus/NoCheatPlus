package cc.co.evenprime.bukkit.nocheat.checks.chat;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.bukkit.entity.Player;
import cc.co.evenprime.bukkit.dnsbl.ProxyServerCheckResultHandler;
import cc.co.evenprime.bukkit.dnsbl.ProxyServerChecker;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;

/**
 * The actual spam check is done at "SpamCheck". This will only
 * do the initial test for proxy usage.
 * 
 */
public class SpambotTest extends ChatCheck implements ProxyServerCheckResultHandler {

    public SpambotTest(NoCheat plugin) {
        super(plugin, "chat.spambot");
    }

    public void startTestForProxies(Player player, String ip) {

        ProxyServerChecker checker = plugin.getProxyServerChecker();
        checker.check(player, ip, this);
    }

    /**
     * This gets called after the checker finished his work. The checker
     * makes sure that this is called in a save, synchronized way
     */
    public void finishedTestForProxies(Player player, String ip, List<String> failures) {
        NoCheatPlayer ncplayer = plugin.getPlayer(player);
        ChatData data = ChatCheck.getData(ncplayer.getDataStore());
        ChatConfig config = ChatCheck.getConfig(ncplayer.getConfigurationStore());

        boolean cancelled = false;

        if(failures.size() > 0 && config.spambotCheck && !player.hasPermission(Permissions.CHAT_SPAM_BOT)) {
            data.spamBotFailed = new LinkedList<String>(failures);
            // cancelled means the player stays in "spambot" status
            cancelled = executeActions(ncplayer, config.spambotActions.getActions(failures.size()));
        }

        if(!cancelled) {
            data.botcheckpassed = true;
        }
    }

    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).spamBotFailed.size());
        else if(wildcard == ParameterName.SERVERS) {
            StringBuilder sb = new StringBuilder();
            List<String> strings = getData(player.getDataStore()).spamBotFailed;
            for(String s : strings) {
                sb.append(s).append(" ");
            }
            return sb.toString().trim();
        } else
            return super.getParameter(wildcard, player);
    }
}
