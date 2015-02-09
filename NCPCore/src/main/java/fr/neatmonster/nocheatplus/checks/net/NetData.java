package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

/**
 * Data for net checks. Some data structures may not be thread-safe, but
 * accessing each checks data individually respecting the sequence of events
 * should work.
 * 
 * @author asofold
 *
 */
public class NetData extends ACheckData {

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

    /**
     * Last 20 seconds keep alive packets counting. Use lastUpdate() for the
     * time of the last event. System.currentTimeMillis() is used.
     */
    public ActionFrequency keepAliveFreq = new ActionFrequency(20, 1000);

    public NetData(final NetConfig config) {
        super(config);
        flyingFrequencyAll = new ActionFrequency(config.flyingFrequencySeconds, 1000L);
        flyingFrequencyRedundantFreq = new ActionFrequency(config.flyingFrequencyRedundantSeconds, 1000L);
    }

}
