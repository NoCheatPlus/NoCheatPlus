package cc.co.evenprime.bukkit.dnsbl;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Test if the connecting IP address of players belongs to a
 * known spam network. Most public proxy servers are used for
 * E-Mail spam, therefore almost all public proxy servers that
 * are used for Minecraft will be blacklisted for spam
 */
public class ProxyServerChecker {

    private static final String[] attributes = {"A"};

    private final Plugin          plugin;
    private final DirContext      context;
    private final String[]        blocklistProviders;

    /**
     * We need a plugin as the owner for the created Bukkit tasks
     * and a string array of adresses of dnsbl providers. See this
     * page: http://www.dnsbl.info/dnsbl-list.php for potential
     * lists.
     * 
     */
    public ProxyServerChecker(Plugin plugin, String[] dnsblProviders) throws NamingException {

        this.plugin = plugin;
        this.blocklistProviders = dnsblProviders.clone();

        Hashtable<Object, String> environment = new Hashtable<Object, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        context = new InitialDirContext(environment);
    }

    /**
     * Test a player and his IP in a seperate bukkit thread for proxy usage.
     * When finished, the handler will be informed in a Bukkit threadsafe way.
     */
    public void check(final Player player, final String ip, final ProxyServerCheckResultHandler handler) {

        Runnable r = new CheckRunnable(player, ip, handler);

        // We have time, therefore let the bukkit scheduler do this
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, r);
    }

    /**
     * Runnable that does the actual dns checking
     *
     */
    public class CheckRunnable implements Runnable {

        private final Player                        player;
        private final ProxyServerCheckResultHandler handler;
        private final String                        ip;
        private final List<String>                  failures = new LinkedList<String>();

        public CheckRunnable(Player player, String ip, ProxyServerCheckResultHandler handler) {
            this.ip = ip;
            this.player = player;
            this.handler = handler;
        }

        @Override
        public void run() {

            // Invert the IP
            String[] parts = ip.split("\\.");
            StringBuilder buffer = new StringBuilder();

            for(int i = parts.length - 1; i >= 0; i--) {
                buffer.append(parts[i]).append('.');
            }

            final String invertedIp = buffer.toString();

            for(String provider : blocklistProviders) {
                String lookupHost = invertedIp + provider;
                try {
                    context.getAttributes(lookupHost, attributes);

                    // If we got a response, the address is listed
                    // so the player failed that one.
                    failures.add(provider);

                } catch(NameNotFoundException e) {
                    // great, the player is not on the blacklist
                    // e.printStackTrace();
                } catch(NamingException e) {
                    // not so great, but I don't care here
                    // e.printStackTrace();
                }
            }

            // We are done, let's schedule a sync task to handle
            // the results
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    handler.finishedTestForProxies(player, ip, failures);
                }
            });
        }
    }
}
