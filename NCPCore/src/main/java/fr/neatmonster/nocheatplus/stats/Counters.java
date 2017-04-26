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
package fr.neatmonster.nocheatplus.stats;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.utilities.ds.corw.LinkedHashMapCOW;

/**
 * Utility to count things, set up to get input from the primary server thread,
 * as well as from other threads. Fetching or resetting counts should be done
 * from within the primary thread, because locking is kept to a minimum.
 * 
 * @author dev1mc
 *
 */
public class Counters {

    /** Just hold a count, so copying of arrays allows keeping consistency. */
    private static final class CountEntry {
        public int count = 0;
    }

    /** Map strings for display/processing to "fast-access" ids. */
    private final Map<String, Integer> idMap = new LinkedHashMapCOW<String, Integer>();

    /** Keys by id. */
    private String[] keys = new String[0];
    // Not sure if to use longs.
    /** Primary thread. */
    private CountEntry[] ptCounts = new CountEntry[0];
    /** Access from outside of the primary thread. */
    private CountEntry[] syCounts = new CountEntry[0];
    /** Locks for access from outside of the primary thread. */
    private ReentrantLock[] syLocks = new ReentrantLock[0];
    /** Global lock for registration of new locks or ids. */
    private final ReentrantLock globalLock = new ReentrantLock();
    // TODO: Consider adding extra counts or ActionFrequency to track "n per minute".

    /**
     * Register a key and return the id that is used for access. If the key is
     * already registered, the registered id is returned.<br>
     * Should only be called from the primary thread, or during (encapsulated)
     * initialization.
     * 
     * @param key
     * @return The id to be used for adding to counts.
     */
    public int registerKey(String key) {
        if (key == null) {
            throw new NullPointerException("Key must not be null.");
        }
        Integer registeredId = idMap.get(key);
        if (registeredId != null) {
            return registeredId.intValue();
        }
        globalLock.lock();

        // Need to re-check the key under lock.
        final int newId;
        if (idMap.containsKey(key)) {
            newId = idMap.get(key).intValue();
        } else {
            // Add a new entry.
            newId = this.keys.length;
            // Explicitly initialize counts under lock.
            ptCounts = Arrays.copyOf(ptCounts, newId + 1);
            ptCounts[newId] = new CountEntry();
            syCounts = Arrays.copyOf(syCounts, newId + 1);
            syCounts[newId] = new CountEntry();
            syLocks = Arrays.copyOf(syLocks, newId + 1);
            syLocks[newId] = null;
            // Add keys last, to prevent use of this key until done.
            final String[] newKeys = Arrays.copyOf(this.keys, newId + 1);
            newKeys[newId] = key;
            this.keys = newKeys;
            idMap.put(key,  newId); // Very last (!).
        }

        globalLock.unlock();
        return newId;
    }

    /**
     * Convenience method for quick testing / uncertain contexts, checks
     * Bukkit.isPrimaryThread(), then delegates, thus is slower.
     * 
     * @param id
     * @param count
     *            Count to add.
     */
    public void add(int id, int count) {
        if (Bukkit.isPrimaryThread()) {
            addPrimaryThread(id, count);
        } else {
            addSynchronized(id, count);
        }
    }

    /**
     * Convenience: pass state via a boolean.
     * 
     * @param id
     * @param count
     * @param isPrimaryThread
     *            Must be correct.
     */
    public void add(int id, int count, boolean isPrimaryThread) {
        if (isPrimaryThread) {
            ptCounts[id].count ++;
        }
        else {
            addSynchronized(id, count);
        }
    }

    /**
     * Only call from the primary thread.
     * 
     * @param id
     * @param count
     *            Count to add.
     */
    public void addPrimaryThread(int id, int count) {
        ptCounts[id].count ++;
    }

    /**
     * Call from any thread.
     * 
     * @param id
     * @param count
     *            Count to add.
     */
    public void addSynchronized(final int id, final int count) {
        ReentrantLock lock = syLocks[id];
        if (lock == null) {
            addLock(id);
            lock = syLocks[id];
        }
        lock.lock();

        syCounts[id].count += count;

        lock.unlock();
    }

    /**
     * Add a lock to syLocks under the global lock;
     * 
     * @param id
     */
    private void addLock(final int id) {
        globalLock.lock();

        if (syLocks[id] == null) {
            syLocks[id] = new ReentrantLock();
        }

        globalLock.unlock();
    }

    /**
     * Reset all counters to 0.<br>
     * Calling this and getting the counts or string compilation should be
     * arranged in a consistent way, e.g. only calling those from the primary
     * thread, because the latter methods don't acquire any locks while
     * iterating.
     */
    public void resetAll() {
        globalLock.lock();

        for (int i = 0; i < ptCounts.length; i ++) {
            ptCounts[i] = new CountEntry();
        }
        for (int i = 0; i < syCounts.length; i ++) {
            syCounts[i] = new CountEntry();
        }

        globalLock.unlock();
    }

    /**
     * Does not acquire any locks.
     * 
     * @return
     */
    public Map<String, Integer> getPrimaryThreadCounts() {
        final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        final int length = keys.length;
        for (int i = 0; i < length; i++) {
            counts.put(keys[i], ptCounts[i].count);
        }
        return counts;
    }

    /**
     * Does not acquire any locks.
     * 
     * @return
     */
    public Map<String, Integer> getSynchronizedCounts() {
        final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        final int length = keys.length;
        for (int i = 0; i < length; i++) {
            counts.put(keys[i], syCounts[i].count);
        }
        return counts;
    }

    /**
     * Get a map for keys to counts, preserving the registration order of keys
     * for iteration (LinkedHashMap).<br>
     * Does not acquire any locks.
     * 
     * @return
     */
    public Map<String, Integer> getMergedCounts() {
        final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        final int length = keys.length;
        for (int i = 0; i < length; i++) {
            counts.put(keys[i], syCounts[i].count + ptCounts[i].count);
        }
        return counts;
    }

    /**
     * Return a String (one line), which summarizes the contents: key
     * merged-count.<br>
     * Only call in the primary thread.
     * 
     * @return
     */
    public String getMergedCountsString() {
        return getMergedCountsString(false);
    }

    /**
     * Return a String (one line), which summarizes the contents: key
     * merged-count (pt count / sy count).<br>
     * Does not acquire any locks.
     * 
     * @param details
     *            If to show difference of primary thread vs. other threads.
     * @return
     */
    public String getMergedCountsString(final boolean details) {
        final StringBuilder builder = new StringBuilder(1024);
        final Map<String, Integer> syCounts = getSynchronizedCounts();
        final Map<String, Integer> ptCounts = getPrimaryThreadCounts();
        builder.append('|');
        for (final Entry<String, Integer> entry : ptCounts.entrySet()) {
            final String key = entry.getKey();
            builder.append(' ');
            builder.append(key);
            builder.append(' ');
            final int pt = entry.getValue();
            final int sy = syCounts.get(key);
            final int sum = pt + sy;
            builder.append(Integer.toString(sum));
            if (details && sum > 0) {
                builder.append(" (");
                builder.append(Integer.toString(pt));
                builder.append('/');
                builder.append(Integer.toString(sy));
                builder.append(')');
            }
            builder.append(" |");
        }
        return builder.toString();
    }

}
