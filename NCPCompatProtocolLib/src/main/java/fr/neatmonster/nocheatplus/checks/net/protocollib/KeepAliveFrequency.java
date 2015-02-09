package fr.neatmonster.nocheatplus.checks.net.protocollib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;

public class KeepAliveFrequency extends BaseAdapter {

    /** Dummy check for bypass checking and actions execution. */
    private final Check check = new Check(CheckType.NET_KEEPALIVEFREQUENCY) {};

    public KeepAliveFrequency(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Client.KEEP_ALIVE);
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
        final NetConfig cc = configFactory.getConfig(player);
        if (!cc.keepAliveFrequencyActive) {
            return;
        }
        final NetData data = dataFactory.getData(player);
        // TODO: Better modeling of actual packet sequences (flying vs. keep alive vs. request/ping).
        // TODO: Better integration wih god-mode check / trigger reset ndt.
        data.keepAliveFreq.add(time, 1f);
        // Use last time accepted as a hard reference.
        final float first = data.keepAliveFreq.bucketScore(0);
        if (first > 1f && !check.hasBypass(player)) {
            // Trigger a violation.
            final double vl = Math.max(first - 1f, data.keepAliveFreq.score(1f) - data.keepAliveFreq.numberOfBuckets());
            if (check.executeActions(player, vl, 1.0, cc.keepAliveFrequencyActions)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        // TODO: Maybe detect if keep alive wasn't asked for + allow cancel.
    }

}
