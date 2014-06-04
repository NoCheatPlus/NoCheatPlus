package fr.neatmonster.nocheatplus.time.monotonic;

import fr.neatmonster.nocheatplus.time.Clock;

/**
 * Monotonic clock. The clock() method will count since creation or call of
 * reset, unless stated otherwise.
 * 
 * @author mc_dev
 * 
 */
public interface MonotonicClock extends Clock {
    
    /**
     * Monotonic clock allow resetting for some reason.
     */
    public void reset(long clock);
    
}
