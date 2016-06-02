/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
