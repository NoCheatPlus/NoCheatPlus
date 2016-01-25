package fr.neatmonster.nocheatplus.checks.net.protocollib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.checks.net.model.CountableLocation;

public class OutgoingPosition extends BaseAdapter {

    public static final int indexX = 0;
    public static final int indexY = 1;
    public static final int indexZ = 2;
    public static final int indexYaw = 0;
    public static final int indexPitch = 1;

    private final Integer ID_OUTGOING_POSITION_UNTRACKED = counters.registerKey("packet.outgoing_position.untracked");

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
        if (configFactory.getConfig(player).flyingFrequencyActive) {
            interpretPacket(player, event.getPacket(), time, dataFactory.getData(player));
        }
    }

    private void interpretPacket(final Player player, final PacketContainer packet, final long time, final NetData data) {
        final StructureModifier<Double> doubles = packet.getDoubles();
        final StructureModifier<Float> floats = packet.getFloat();

        if (doubles.size() != 3 || floats.size() != 2) {
            packetMismatch(packet);
            return;
        }

        // TODO: Detect/skip data with relative coordinates.
        // TODO: Concept: force KeepAlive vs. set expected coordinates in Bukkit events.

        final double x = doubles.read(indexX);
        final double y = doubles.read(indexY);
        final double z = doubles.read(indexZ);
        final float yaw = floats.read(indexYaw);
        final float pitch = floats.read(indexPitch);

        final CountableLocation packetData = data.teleportQueue.onOutgoingTeleport(x, y, z, yaw, pitch);
        if (packetData == null) {
            // Add counter for untracked (by Bukkit API) outgoing teleport.
            // TODO: There may be other cases which are indicated by Bukkit API events.
            counters.add(ID_OUTGOING_POSITION_UNTRACKED, 1);
            if (data.debug) {
                debug(player, "Untracked outgoing position: " + x + ", " + y + ", " + z + " (yaw=" + yaw + ", pitch=" + pitch + ").");
            }
        }
        else {
            if (data.debug) {
                debug(player, "Expect ACK on outgoing position: " + packetData);
            }
        }
    }

    private void packetMismatch(PacketContainer packet) {
        // TODO: What? Add to counters?
    }

}
