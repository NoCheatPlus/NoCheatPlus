package fr.neatmonster.nocheatplus.checks.net;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

/**
 * Primary thread only.
 * @author asofold
 *
 */
public class NetData extends ACheckData {

    /** All flying packets, use Monotonic.millis() for time. */
    public final ActionFrequency flyingFrequencyAll;
    public boolean flyingFrequencyOnGround = false;
    public long flyingFrequencyTimeOnGround = 0L;
    public long flyingFrequencyTimeNotOnGround = 0L;
    /**
     * Monitors redundant packets, when more than 20 packets per second are
     * sent. Use Monotonic.millis() for time.
     */
    public final ActionFrequency flyingFrequencyRedundantFreq;

    public NetData(final NetConfig config) {
        super(config);
        flyingFrequencyAll = new ActionFrequency(config.flyingFrequencySeconds, 1000L);
        flyingFrequencyRedundantFreq = new ActionFrequency(config.flyingFrequencyRedundantSeconds, 1000L);
    }

}
