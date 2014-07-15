package fr.neatmonster.nocheatplus.time.monotonic;

/**
 * Thread safe version of a monotonic clock, wrapping around a given clock.
 * Since synchronized method bodies wrap around the underlying clock, the clock
 * must not be used outside of this instance.
 * 
 * @author mc_dev
 * 
 */
public class MonotonicSynchClock implements MonotonicClock {
    
    private final MonotonicClock clock;
    
    public MonotonicSynchClock(MonotonicClock clock) {
        this.clock = clock;
    }
    
    @Override
    public synchronized long clock() {
        return clock.clock();
    }
    
    @Override
    public synchronized void reset(long clock) {
        this.clock.reset(clock);
    }
    
}
