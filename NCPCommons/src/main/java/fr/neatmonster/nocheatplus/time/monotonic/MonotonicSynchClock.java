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
