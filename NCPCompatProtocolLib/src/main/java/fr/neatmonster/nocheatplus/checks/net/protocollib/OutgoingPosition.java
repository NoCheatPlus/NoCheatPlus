package fr.neatmonster.nocheatplus.checks.net.protocollib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.logging.Streams;

public class OutgoingPosition extends BaseAdapter {

    public static final int indexX = 0;
    public static final int indexY = 1;
    public static final int indexZ = 2;
    public static final int indexYaw = 0;
    public static final int indexPitch = 1;

    public OutgoingPosition(Plugin plugin) {
        // PacketPlayInFlying[3, legacy: 10]
        super(plugin, ListenerPriority.HIGHEST, new PacketType[] {
                PacketType.Play.Server.POSITION
                // TODO: POSITION_LOOK ??
        });
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final long time = System.currentTimeMillis();

        final Player player = event.getPlayer();
        final NetConfig cc = configFactory.getConfig(player);
        if (cc.flyingFrequencyActive) {
            final NetData data = dataFactory.getData(player);
            final DataPacketFlying packetData = interpretPacket(event.getPacket(), time, data);
            if (packetData != null && data.debug) {
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " Expect ACK on outgoing position: " + packetData);
            }
        }
    }

    private DataPacketFlying interpretPacket(final PacketContainer packet, final long time, final NetData data) {
        final StructureModifier<Double> doubles = packet.getDoubles();
        final StructureModifier<Float> floats = packet.getFloat();

        if (doubles.size() != 3 || floats.size() != 2) {
            packetMismatch(packet);
            return null;
        }

        // TODO: Detect/skip data with relative coordinates.
        // TODO: Concept: force KeepAlive vs. set expected coordinates in Bukkit events.

        final double x = doubles.read(indexX);
        final double y = doubles.read(indexY);
        final double z = doubles.read(indexZ);
        final float yaw = floats.read(indexYaw);
        final float pitch = floats.read(indexPitch);

        return data.teleportQueue.onOutgoingTeleport(x, y, z, yaw, pitch);
    }

    private void packetMismatch(PacketContainer packet) {
        // TODO: Consider.
    }

}
