package fr.neatmonster.nocheatplus.checks.net.model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Queue outgoing teleport locations (DataPacketFlying), in order to cancel
 * other moving until the client acknowledges previous teleport locations. In
 * fact this might only queue a single location.
 * 
 * @author asofold
 *
 */
public class TeleportQueue {

    public static enum AckResolution {
        /** Waiting for ACK. */
        CANCEL,
        /** Packet is ACK. */
        ACK,
        /** Not waiting for an ACK. */
        IDLE
    }

    private final Lock lock = new ReentrantLock();

    /** Validated outgoing teleport location. */
    private DataPacketFlying lastTeleport = null;
    /** Top be validated teleport location from a Bukkit event. */
    private DataPacketFlying expectTeleport = null;

    /**
     * Call for Bukkit events (expect this packet to be sent).
     * @param packetData
     */
    public void onTeleportEvent(final DataPacketFlying packetData) {
        lock.lock();
        expectTeleport = packetData;
        lock.unlock();
    }

    /**
     * Call for outgoing teleport packets.
     * @param packetData
     */
    public DataPacketFlying onOutgoingTeleport(final double x, final double y, final double z, final float yaw, final float pitch) {
        DataPacketFlying res = null;
        lock.lock();
        // Only register this location, if it matches the location from a Bukkit event.
        if (expectTeleport != null && expectTeleport.matches(x, y, z, yaw, pitch)) {
            res = expectTeleport;
            lastTeleport = expectTeleport;
            expectTeleport = null;
        }
        lock.unlock();
        return res;
    }

    /**
     * Test if the move is an ACK move (or no ACK is expected), adjust internals
     * if necessary.
     * 
     * @param packetData
     * @return ACK, if the packet matches an expected ACK position. IDLE, if no
     *         ACK is expected or on timeout. CANCEL, if still waiting for an
     *         ACK.
     */
    public AckResolution processAck(final DataPacketFlying packetData) {
        final AckResolution res;

        lock.lock();
        if (lastTeleport == null) {
            res = AckResolution.IDLE;
        } else if (packetData.containsSameLocation(lastTeleport)) {
            lastTeleport = null;
            res = AckResolution.ACK;
        } else if (packetData.time - lastTeleport.time > 15000 || packetData.time < lastTeleport.time) {
            lastTeleport = null;
            res = AckResolution.IDLE;
        } else {
            res = AckResolution.CANCEL;
        }
        lock.unlock();

        return res;
    }

    public void clear() {
        lock.lock();
        lastTeleport = null;
        expectTeleport = null;
        lock.unlock();
    }

}
