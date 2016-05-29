package fr.neatmonster.nocheatplus.checks.moving.location.tracking;

import java.util.Iterator;

import fr.neatmonster.nocheatplus.components.location.IGetPosition;
import fr.neatmonster.nocheatplus.components.pool.AbstractPool;

/**
 * This class is meant to record locations for players moving, in order to allow
 * to be more lenient for the case of latency, e.g. with player-player
 * interaction such as with fighting. <br>
 * NOTES on intended use:<br>
 * <li>Is meant to always carry some location, however right after
 * initialization it may throw NullPointerException if you use it without
 * updating to a location.</li>
 * <li>Records only the end-positions of a move. Typically also updated, when a
 * player is attacked.</li>
 * <li>Prefer calling add(...) with the current location, before iterating.
 * Alternative: guard with isEmpty().</li>
 * <li>Updating on teleport events is not intended - if the distance is too big,
 * Minecraft should prevent interaction anyway.</li>
 * <li>There is no merging done on small distances. Neither is the time updated
 * for the latest entry, if the position stays the same. Always consider the
 * time difference to now for the first entry, and the time difference to the
 * previous entry for other entries.</li>
 * 
 * @author asofold
 *
 */
public class LocationTrace {

    public static interface ITraceEntry extends IGetPosition {

        public long getTime();

    }

    public static class TraceEntry implements ITraceEntry {
        
        // TODO: Consider using a simple base implementation for IGetSetPosiotion.

        /** We keep it open, if ticks or ms are used. */
        private long time;
        /** Coordinates. */
        private double x, y, z;

        /** Older/next. */
        private TraceEntry next;
        /** Newer/previous. */
        private TraceEntry previous;

        /**
         * Previous and next are not touched here (!).
         * 
         * @param time
         * @param x
         * @param y
         * @param z
         */
        public void set(long time, double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public double getZ() {
            return z;
        }

        @Override
        public long getTime() {
            return time;
        }

    }

    public static final class TraceEntryPool extends AbstractPool<TraceEntry> {

        public TraceEntryPool(int maxPoolSize) {
            super(maxPoolSize);
        }

        @Override
        protected TraceEntry newInstance() {
            return new TraceEntry();
        }

    }

    /**
     * Iterate over a slice of entries with given start and direction. Not a
     * fully featured Iterator.
     * 
     * @author asofold
     *
     */
    public static final class TraceIterator implements Iterator<ITraceEntry> {
        /** Element the iteration starts with. */
        final TraceEntry first;
        /** The next element to return for calling next(). */
        private TraceEntry next;

        /**
         * If set to true, calling next means to advance in direction of
         * entry.next.
         */
        final boolean nextIsNext;

        protected TraceIterator(final TraceEntry first, boolean nextIsNext) {
            this.first = first;
            this.nextIsNext = nextIsNext;
            next = first;
            if (!hasNext()) {
                // TODO: Consider IllegalStateException?
                throw new IllegalArgumentException("Empty iterators are not allowed.");
            }
        }

        @Override
        public final ITraceEntry next() {
            if (!hasNext()) {
                throw new IndexOutOfBoundsException("No more entries to iterate.");
            }
            final TraceEntry next = this.next;
            this.next = nextIsNext ? next.next : next.previous;
            return next;
        }

        @Override
        public final boolean hasNext() {
            // Just check if currentIndex is within range.
            return next != null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private final TraceEntryPool pool;
    /** Maximum age for an entry. */
    private long maxAge;
    /** Maximum number of entries to keep. */
    private int maxSize;
    /** Number of valid entries. */
    private int size = 0;
    /** First (latest) entry. */
    private TraceEntry firstEntry = null;
    /** Last (oldest) entry. */
    private TraceEntry lastEntry = null;

    // (No world name stored: Should be reset on world changes.)

    /**
     * 
     * @param maxAge
     *            Stored as long here for efficiency of comparison.
     * @param maxSize
     * @param pool
     */
    public LocationTrace(final long maxAge, final int maxSize, final TraceEntryPool pool) {
        this.pool = pool;
        this.maxAge = maxAge;
        this.maxSize = maxSize;
    }

    public final void addEntry(final long time, final double x, final double y, final double z) {
        if (size > 0) {
            if (x == firstEntry.x && y == firstEntry.y && z == firstEntry.z) {
                // (No update of time. firstEntry ... now always counts.)
                return;
            }
        }
        // Add a new entry.
        final TraceEntry newEntry = pool.getInstance();
        newEntry.set(time, x, y, z);
        setFirst(newEntry);
        // Remove the last entry, if maxSize is exceeded.
        if (size > maxSize) {
            returnToPool(lastEntry);
        }
        // Remove too old entries.
        // TODO: Call externally rather or only call every so and so time?
        checkMaxAge(time);
    }

    /**
     * Set entry as first element. Updates size.
     * 
     * @param entry
     */
    private void setFirst(final TraceEntry entry) {
        entry.previous = null;
        if (this.firstEntry != null) {
            entry.next = this.firstEntry;
            this.firstEntry.previous = entry;
            // lastEntry remains unchanged.
        }
        else {
            // The first entry to be here.
            entry.next = null;
            this.lastEntry = entry;
        }
        this.firstEntry = entry;
        size ++;
    }

    /**
     * Return entries older than maxAge to the pool. Always keeps the latest
     * entry.
     * 
     * @param time
     *            The time now.
     */
    public void checkMaxAge(final long time) {
        // TODO: Visibility to private? [can set to null or reset on logout]
        // Ensure we have entries to expire at all.
        if (time - lastEntry.time < maxAge || size == 1) {
            return;
        }
        // Find the earliest entry to expire and return to pool.
        TraceEntry entry = lastEntry;
        while (time - entry.previous.time > maxAge) {
            entry = entry.previous;
        }
        returnToPool(entry);
    }

    /** Reset/invalidate all elements. */
    public void reset() {
        returnToPool(firstEntry);
        firstEntry = lastEntry = null;
        size = 0; // Redundant.
    }

    /**
     * Unlink and return this entry and all older entries to the pool. This does
     * adjust size.
     * 
     * @param entry
     */
    private void returnToPool(TraceEntry entry) {
        if (entry == null) {
            return;
        }
        // Unlink from a newer entry.
        if (entry.previous != null) {
            entry.previous.next = null;
        }
        // Reset lastEntry.
        this.lastEntry = entry.previous; // Assume it's always correct.
        // Reset first entry, if this is was the first.
        if (entry == this.firstEntry) {
            this.firstEntry = null;
        }
        // Unlink and return this and all following entries.
        TraceEntry nextEntry;
        while (entry != null) {
            nextEntry = entry.next;
            entry.next = entry.previous = null;
            pool.returnInstance(entry);
            entry = nextEntry;
            size --;
        }
    }

    /**
     * Get the number of stored elements.
     * 
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Test if no elements are stored.
     * 
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Get the maximum number of stored entries.
     * @return
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Get the maximum age of stored entries.
     * @return
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * Iterate from latest to oldest.
     * @return
     */
    public TraceIterator latestIterator() {
        return new TraceIterator(firstEntry, true);
    }

    /**
     * Iterate from oldest to latest.
     * @return
     */
    public TraceIterator oldestIterator() {
        return new TraceIterator(lastEntry, false);
    }

    /**
     * Iterate from entry with max. age to latest, always includes latest.
     * @param time Absolute time value for oldest accepted entries.
     * @return TraceIterator containing entries that have not been created before the given time, iterating ascending with time.
     */
    public TraceIterator maxAgeIterator(final long time) {
        TraceEntry start = firstEntry;
        while (start != null) {
            if (start.next != null && start.next.time >= time) {
                start = start.next;
            }
            else {
                break;
            }
        }
        return new TraceIterator(start, false);
    }

    /**
     * Adjust settings and expire entries if needed.
     * 
     * @param traceMaxAge
     * @param traceMaxSize
     * @param time The time now.
     */
    public void adjustSettings(final long maxAge, final int maxSize, final long time) {
        // Time run backwards check (full reset).
        if (size > 0 && time < firstEntry.time) {
            reset();
        }
        // maxAge.
        if (this.maxAge != maxAge ) {
            this.maxAge = maxAge;
            checkMaxAge(time);
        }
        // maxSize.
        if (this.maxSize != maxSize) {
            this.maxSize = maxSize;
            TraceEntry entry = this.lastEntry;
            int size = this.size;
            while (size > maxSize) {
                entry = entry.previous;
                size --;
            }
            returnToPool(entry);
        }
    }

}
