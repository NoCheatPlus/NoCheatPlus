package cc.co.evenprime.bukkit.dnsbl;

import java.util.List;
import org.bukkit.entity.Player;

/**
 * The class that handles the results of the proxy checks
 * has to implement this.
 */
public interface ProxyServerCheckResultHandler {

    /**
     * If the list of failures is empty, the player and his ip
     * passed all checks. If it is nonempty, it will contain
     * the servers that have blacklisted the ip
     */
    public void finishedTestForProxies(Player player, String ip, List<String> failures);
}
