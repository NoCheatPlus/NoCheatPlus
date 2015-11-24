package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.net.model.TeleportQueue;
import fr.neatmonster.nocheatplus.utilities.ds.count.ActionFrequency;

/**
 * Data for net checks. Some data structures may not be thread-safe, but
 * accessing each checks data individually respecting the sequence of events
 * should work.
 * 
 * @author asofold
 *
 */
public class NetData extends ACheckData {

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

    public final TeleportQueue teleportQueue = new TeleportQueue(); // TODO: Consider using one lock per data instance and pass here.

    public NetData(final NetConfig config) {
        super(config);
        flyingFrequencyAll = new ActionFrequency(config.flyingFrequencySeconds, 1000L);
        flyingFrequencyRedundantFreq = new ActionFrequency(config.flyingFrequencyRedundantSeconds, 1000L);
    }

    public void onJoin(final Player player) {
        teleportQueue.clear();
    }

    public void onLeave(Player player) {
        teleportQueue.clear();
    }

}
