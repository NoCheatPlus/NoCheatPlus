package fr.neatmonster.nocheatplus.checks.net.model;

import java.util.Iterator;
import java.util.LinkedList;
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
        WAITING,
        /** Packet is ACK. */
        ACK,
        /** Not waiting for an ACK. */
        IDLE
    }

    // TODO: Consider passing a reentrant lock in the constructor (e.g. one lock per NetData).
    private final Lock lock = new ReentrantLock();

    /** Validated outgoing teleport locations, expected to be confirmed by the client. */
    private final LinkedList<CountableLocation> expectIncoming = new LinkedList<CountableLocation>();
    /** Location from a Bukkit event, which we expect an outgoing teleport for. */
    private DataLocation expectOutgoing = null;

    private long maxAge = 4000; // TODO: configurable
    private int maxQueueSize = 60; // TODO: configurable


    /**
     * Maximum age in milliseconds, older entries expire.
     * @return
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * Call for Bukkit events (expect this packet to be sent).
     * @param packetData
     */
    public void onTeleportEvent(final double x, final double y, final double z, final float yaw, final float pitch) {
        lock.lock();
        expectOutgoing = new DataLocation(x, y, z, yaw, pitch);
        lock.unlock();
    }

    /**
     * Call for outgoing teleport packets. Lazily checks for expiration and max queue size. 
     * @param packetData
     */
    public CountableLocation onOutgoingTeleport(final double x, final double y, final double z, final float yaw, final float pitch) {
        CountableLocation res = null;
        final long time = System.currentTimeMillis();
        lock.lock();
        // Only register this location, if it matches the location from a Bukkit event.
        if (expectOutgoing != null) {
            if (expectOutgoing.isSameLocation(x, y, z, yaw, pitch)) {
                // Add to queue.
                if (!expectIncoming.isEmpty()) {
                    // Lazy expiration check.
                    final Iterator<CountableLocation> it = expectIncoming.iterator();
                    while (it.hasNext()) {
                        if (time - maxAge > it.next().time) {
                            it.remove();
                        } else {
                            break;
                        }
                    }
                    if (!expectIncoming.isEmpty()) {
                        final CountableLocation last = expectIncoming.getLast();
                        if (last.isSameLocation(x, y, z, yaw, pitch)) {
                            last.time = time;
                            last.count ++;
                        }
                    }
                }
                // Add a new entry, if not merged with last.
                if (res == null) {
                    res = new CountableLocation(x, y, z, yaw, pitch, 1, time);
                    expectIncoming.addLast(res);
                    // Don't exceed maxQueueSize.
                    if (expectIncoming.size() > maxQueueSize) {
                        expectIncoming.removeFirst();
                    }
                }
            }
            // Reset in any case.
            expectOutgoing = null;
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
        if (expectIncoming.isEmpty() || !packetData.hasPos || !packetData.hasLook) {
            res = AckResolution.IDLE;
        } else {
            res = getAckResolution(packetData);
        }
        lock.unlock();

        return res;
    }

    /**
     * Check queue (lock is handled outside of this method). Does check for
     * expiration of entries.
     * 
     * @param packetData
     * @return
     */
    private AckResolution getAckResolution(final DataPacketFlying packetData) {
        // Iterate from oldest to newest.
        final Iterator<CountableLocation> it = expectIncoming.iterator();
        while (it.hasNext()) {
            final CountableLocation ref = it.next();
            if (packetData.time - maxAge >= ref.time) {
                it.remove();
            }
            else if (packetData.isSameLocation(ref)) {
                // Match.
                // Remove all preceding older entries.
                while (ref != expectIncoming.getFirst()) {
                    expectIncoming.removeFirst();
                }
                // Decrease count and remove the matching entry if count is down to 0.
                if (--ref.count <= 0) { // (Lots of safety margin.)
                    expectIncoming.removeFirst(); // Do not use the iterator here.
                }
                return AckResolution.ACK;
            } else {
                // Skip until match or none found.
                // TODO: Consider settings like maxSkipCount or strictly return WAITING.
            }
        }
        // No match.
        return expectIncoming.isEmpty() ? AckResolution.IDLE : AckResolution.WAITING;
    }

    public void clear() {
        lock.lock();
        expectIncoming.clear();
        expectOutgoing = null;
        lock.unlock();
    }

}
