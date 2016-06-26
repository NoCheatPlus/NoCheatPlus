package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.LinkedHashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.PacketFrequency;

/**
 * Pre-1.9.
 * 
 * @author asofold
 *
 */
public class CatchAllAdapter extends BaseAdapter {

    /**
     * Somehow determine types to monitor.
     * 
     * @return
     */
    private static Iterable<? extends PacketType> getPacketTypes() {
        // TODO: Config ?
        Set<PacketType> types = new LinkedHashSet<PacketType>();
        for (PacketType type : PacketType.Play.Client.getInstance().values()) {
            if (type.isSupported()) {
                types.add(type);
            }
        }
        //        // relax.
        //        types.add(PacketType.Play.Server.POSITION);
        return types;
    }

    private final PacketFrequency packetFrequency;

    public CatchAllAdapter(Plugin plugin) {
        super(plugin, ListenerPriority.LOWEST, getPacketTypes());
        packetFrequency = new PacketFrequency();
        NCPAPIProvider.getNoCheatPlusAPI().addComponent(packetFrequency);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        final Player player = event.getPlayer();
        final NetConfig cc = configFactory.getConfig(player);
        if (cc.packetFrequencyActive) {
            final NetData data = dataFactory.getData(player);
            if (packetFrequency.isEnabled(player, data, cc) 
                    && packetFrequency.check(player, data, cc)) {
                event.setCancelled(true);
            }
        }
    }

    //    @Override
    //    public void onPacketSending(PacketEvent event) {
    //        final Player player = event.getPlayer();
    //        final NetConfig cc = configFactory.getConfig(player);
    //        if (cc.packetFrequencyActive) {
    //            packetFrequency.relax(event.getPlayer(), dataFactory.getData(player), cc);
    //        }
    //    }

}
