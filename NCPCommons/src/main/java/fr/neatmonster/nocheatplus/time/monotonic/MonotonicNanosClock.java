package fr.neatmonster.nocheatplus.time.monotonic;

/**
 * Monotonic clock based on System.nanoTime(). Not thread-safe.
 * 
 * @author mc_dev
 * 
 */
public class MonotonicNanosClock extends MonotonicAbstractClock {
    
    @Override
    protected long fetchClock() {
        return System.nanoTime();
    }
    
}
