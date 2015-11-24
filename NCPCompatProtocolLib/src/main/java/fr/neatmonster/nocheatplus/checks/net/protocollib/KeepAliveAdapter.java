package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.KeepAliveFrequency;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;

/**
 * Limit keep alive packet frequency, set lastKeepAliveTime (even if disabled,
 * in case fight.godmode is enabled).
 * 
 * @author asofold
 *
 */
public class KeepAliveAdapter extends BaseAdapter {

    /** Dummy check for bypass checking and actions execution. */
    private final KeepAliveFrequency frequencyCheck = new KeepAliveFrequency();

    public KeepAliveAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Client.KEEP_ALIVE);

        // Add feature tags for checks.
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET_KEEPALIVEFREQUENCY_ACTIVE)) {
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("checks", Arrays.asList(KeepAliveFrequency.class.getSimpleName()));
        }
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(frequencyCheck);
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        final long time =  System.currentTimeMillis();
        final Player player = event.getPlayer();
        if (player == null) {
            counters.add(ProtocolLibComponent.idNullPlayer, 1);
            event.setCancelled(true);
            return;
        }
        // Always update last received time.
        final NetData data = dataFactory.getData(player);
        data.lastKeepAliveTime = time;
        final NetConfig cc = configFactory.getConfig(player);

        // Run check(s).
        // TODO: Match vs. outgoing keep alive requests.
        // TODO: Better modeling of actual packet sequences (flying vs. keep alive vs. request/ping).
        // TODO: Better integration with god-mode check / trigger reset ndt.
        if (cc.keepAliveFrequencyActive && frequencyCheck.check(player, time, data, cc)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        // TODO: Maybe detect if keep alive wasn't asked for + allow cancel.
    }

}
