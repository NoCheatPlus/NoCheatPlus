package fr.neatmonster.nocheatplus.checks.net;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.net.model.DataPacketFlying;
import fr.neatmonster.nocheatplus.checks.net.model.TeleportQueue;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

/**
 * Data for net checks. Some data structures may not be thread-safe, intended
 * for thread-local use. Order of events should make use within packet handlers
 * safe.
 * 
 * @author asofold
 *
 */
public class NetData extends ACheckData {

    // Reentrant lock.
    private final Lock lock = new ReentrantLock();

    // AttackFrequency
    public ActionFrequency attackFrequencySeconds = new ActionFrequency(16, 500);

    // FlyingFrequency
    /** All flying packets, use System.currentTimeMillis() for time. */
    public final ActionFrequency flyingFrequencyAll;
    public boolean flyingFrequencyOnGround = false;
    public long flyingFrequencyTimeOnGround = 0L;
    public long flyingFrequencyTimeNotOnGround = 0L;
    /**
     * Monitors redundant packets, when more than 20 packets per second are
     * sent. Use System.currentTimeMillis() for time.
     */
    public final ActionFrequency flyingFrequencyRedundantFreq;

    // KeepAliveFrequency
    /**
     * Last 20 seconds keep alive packets counting. Use lastUpdate() for the
     * time of the last event. System.currentTimeMillis() is used.
     */
    public ActionFrequency keepAliveFreq = new ActionFrequency(20, 1000);

    // Shared.
    /**
     * Last time some action was received (keep alive/flying/interaction). Also
     * maintained for fight.godmode.
     */
    public long lastKeepAliveTime = 0L;

    /**
     * Detect teleport-ACK packets, consistency check to only use outgoing
     * position if there has been a PlayerTeleportEvent for it.
     */
    public final TeleportQueue teleportQueue = new TeleportQueue(); // TODO: Consider using one lock per data instance and pass here.

    /**
     * Store past flying packet locations for reference (lock for
     * synchronization). Mainly meant for access to flying packets from the
     * primary thread. Latest packet is first.
     */
    // TODO: Might extend to synchronize with moving events.
    private final LinkedList<DataPacketFlying> flyingQueue = new LinkedList<DataPacketFlying>();
    /** Maximum amount of packets to store. */
    private final int flyingQueueMaxSize = 10;

    public NetData(final NetConfig config) {
        super(config);
        flyingFrequencyAll = new ActionFrequency(config.flyingFrequencySeconds, 1000L);
        flyingFrequencyRedundantFreq = new ActionFrequency(config.flyingFrequencyRedundantSeconds, 1000L);
    }

    public void onJoin(final Player player) {
        teleportQueue.clear();
        clearFlyingQueue();
    }

    public void onLeave(Player player) {
        teleportQueue.clear();
        clearFlyingQueue();
    }

    /**
     * Add a packet to the queue.
     * 
     * @param packetData
     * @return If a packet has been removed due to exceeding maximum size.
     */
    public boolean addFlyingQueue(final DataPacketFlying packetData) {
        boolean res = false;
        lock.lock();
        flyingQueue.addFirst(packetData);
        if (flyingQueue.size() > flyingQueueMaxSize) {
            flyingQueue.removeLast();
            res = true;
        }
        lock.unlock();
        return res;
    }

    /**
     * Clear the flying packet queue.
     */
    public void clearFlyingQueue() {
        lock.lock();
        flyingQueue.clear();
        lock.unlock();
    }

    public DataPacketFlying[] copyFlyingQueue() {
        lock.lock();
        final DataPacketFlying[] out = flyingQueue.toArray(new DataPacketFlying[flyingQueue.size()]);
        lock.unlock();
        return out;
    }

}
