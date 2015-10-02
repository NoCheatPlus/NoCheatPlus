package fr.neatmonster.nocheatplus.checks.net.protocollib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;

public class DebugAdapter extends BaseAdapter {

    public DebugAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, new PacketType[] {
                PacketType.Play.Client.BLOCK_PLACE,
                PacketType.Play.Client.BLOCK_DIG,
        });
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        final Player player = event.getPlayer();
        if (dataFactory.getData(player).debug) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " packet: " + event.getPacketType());
        }
    }

}
