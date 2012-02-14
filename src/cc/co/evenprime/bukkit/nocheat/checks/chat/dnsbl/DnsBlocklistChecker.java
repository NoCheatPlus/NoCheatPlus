package cc.co.evenprime.bukkit.nocheat.checks.chat.dnsbl;

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
import cc.co.evenprime.bukkit.nocheat.NoCheat;

public class DnsBlocklistChecker {

    private static final String[] attributes = {"A"};

    private final NoCheat         plugin;
    private final DirContext      context;
    private final String[]        blocklistProviders;

    public DnsBlocklistChecker(NoCheat plugin, int limit, String... providers) throws NamingException {

        Hashtable<Object, String> environment = new Hashtable<Object, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        context = new InitialDirContext(environment);
        this.plugin = plugin;
        this.blocklistProviders = providers;
    }

    public void check(final Player player, final String ip) {

        // Invert IP
        String[] parts = ip.split("\\.");
        StringBuilder buffer = new StringBuilder();

        for(int i = parts.length - 1; i >= 0; i--) {
            buffer.append(parts[i]).append('.');
        }

        final String invertedIp = buffer.toString();

        Runnable r = new CheckRunnable(invertedIp, player);

        // We have time, therefore let the bukkit scheduler do this
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, r);
    }

    public void finished(final Player player, final List<String> failures) {

    }

    /**
     * Runnable that does the actual dns checking
     *
     */
    public class CheckRunnable implements Runnable {

        private final String       invertedIp;
        private final Player       player;
        private final List<String> failures = new LinkedList<String>();

        public CheckRunnable(String invertedIp, Player player) {
            this.invertedIp = invertedIp;
            this.player = player;
        }

        @Override
        public void run() {
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
                    // not so great, but I don't care atm
                    // e.printStackTrace();
                }
            }

            // We are done, let's work with the results
            finished(player, failures);
        }
    }
}
