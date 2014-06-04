package fr.neatmonster.nocheatplus.time.monotonic;

/**
 * Basic implementation of increasing a counter for a not necessarily monotonic
 * underlying clock, fetched with fetchClock(). Not thread-safe.
 * 
 */
public abstract class MonotonicAbstractClock implements MonotonicClock {
    
    private long clock;
    
    private long lastFetch;
    
    public MonotonicAbstractClock() {
    	clock = fetchClock();
    	lastFetch = clock;
    }
    
    public MonotonicAbstractClock(long clock) {
    	reset(clock);
    }
    
    protected abstract long fetchClock();
    
    @Override
    public long clock() {
    	// TODO: Add feature to detect running too fast and correction as well.
        final long fetch = fetchClock();
        final long diff = fetch - this.lastFetch;
        if (diff > 0) {
            this.clock += diff;
        }
        this.lastFetch = fetch;
        return this.clock;
    }
    
    @Override
    public void reset(long clock) {
        this.clock = clock;
        this.lastFetch = fetchClock();
    }
    
}
