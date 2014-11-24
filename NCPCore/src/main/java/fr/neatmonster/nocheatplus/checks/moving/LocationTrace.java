package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Iterator;

import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * This class is meant to record locations for players moving, in order to allow to be more   
 * lenient for the case of latency for player-player interaction such as with fighting.
 * <br>
 * NOTES on intended use:<br>
 * <li>Is meant to always carry some location.</li>
 * <li>Records only the end-positions of a move.</li>
 * <li>Prefer calling add(...) with the current location, before iterating. Alternative: guard with isEmpty().</li>
 * <li>Updating on teleport events is not intended - if the distance is too big, Minecraft should prevent interaction anyway.</li>
 * @author mc_dev
 *
 */
public class LocationTrace {

    public static final class TraceEntry {

        /** We keep it open, if ticks or ms are used. */
        public long time;
        /** Coordinates. */
        public double x, y, z;
        public double lastDistSq;

        public void set(long time, double x, double y, double z, double lastDistSq) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
            this.lastDistSq = lastDistSq;
        }
    }

    /**
     * Iterate from oldest to latest. Not a fully featured Iterator.
     * @author mc_dev
     *
     */
    public static final class TraceIterator implements Iterator<TraceEntry>{
        private final TraceEntry[] entries;
        /** Index as in LocationTrace */
        private final int index;
        private final int size;
        private int currentIndex;
        private final boolean ascend;

        protected TraceIterator(TraceEntry[] entries, int index, int size, int currentIndex, boolean ascend) {
            if (currentIndex >= entries.length || currentIndex < 0 || 
                    currentIndex <= index - size || currentIndex > index && currentIndex <= index - size + entries.length) {
                // This should also prevent iterators for size == 0, for the moment (!).
                throw new IllegalArgumentException("startIndex out of bounds.");
            }
            this.entries = entries;
            this.index = index;
            this.size = size;
            this.currentIndex = currentIndex;
            this.ascend = ascend;
        }

        @Override
        public final TraceEntry next() {
            if (!hasNext()) {
                throw new IndexOutOfBoundsException("No more entries to iterate.");
            }
            final TraceEntry entry = entries[currentIndex];
            if (ascend) {
                currentIndex ++;
                if (currentIndex >= entries.length) {
                    currentIndex = 0;
                }
                int ref = index - size + 1;
                if (ref < 0) {
                    ref  += entries.length;
                }
                if (currentIndex == ref) {
                    // Invalidate the iterator.
                    currentIndex = -1;
                }
            } else {
                currentIndex --;
                if (currentIndex < 0) {
                    currentIndex = entries.length - 1;
                }
                if (currentIndex == index) {
                    // Invalidate the iterator.
                    currentIndex = - 1;
                }
            }
            return entry;
        }

        @Override
        public final boolean hasNext() {
            // Just check if currentIndex is within range.
            return currentIndex >= 0 && currentIndex <= index && currentIndex > index - size || currentIndex > index && currentIndex >= index - size + entries.length;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /** A Ring. */
    private final TraceEntry[] entries;
    /** Last element index. */
    private int index = -1;
    /** Number of valid entries. */
    private int size = 0;
    private final double mergeDist;
    private final double mergeDistSq;

    // (No world name stored: Should be reset on world changes.)

    public LocationTrace(int bufferSize, double mergeDist) {
        // TODO: Might consider a cut-off distance/age (performance saving for iteration). 
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Expect bufferSize > 0, got instead: " + bufferSize);
        }
        entries = new TraceEntry[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            entries[i] = new TraceEntry();
        }
        this.mergeDist = mergeDist;
        this.mergeDistSq = mergeDist * mergeDist;
    }

    public final void addEntry(final long time, final double x, final double y, final double z) {
        double lastDistSq = 0.0;
        if (size > 0) {
            final TraceEntry latestEntry = entries[index];
            // TODO: Consider duration of staying there ?
            if (x == latestEntry.x && y == latestEntry.y && z == latestEntry.z) {
                latestEntry.time = time;
                return;
            }
            lastDistSq = TrigUtil.distanceSquared(x, y, z, latestEntry.x, latestEntry.y, latestEntry.z);
            // TODO: Think about minMergeSize (1 = never merge the first two, size = first fill the ring).
            if (size > 1 && lastDistSq <= mergeDistSq) {
                // TODO: Could use Manhattan, after all.
                // Only merge if last distance was not greater than mergeDist, to prevent too-far-off entries.
                if (latestEntry.lastDistSq <= mergeDistSq) {
                    // Update lastDistSq, due to shifting the elements position.
                    final TraceEntry secondLatest = index - 1 < 0 ? entries[index - 1 + entries.length] : entries[index - 1];
                    lastDistSq = TrigUtil.distanceSquared(x, y, z, secondLatest.x, secondLatest.y, secondLatest.z);
                    latestEntry.set(time, x, y, z, lastDistSq);
                    return;
                }
            }
        }
        // Advance index.
        index++;
        if (index == entries.length) {
            index = 0;
        }
        if (size < entries.length) {
            size ++;
        }
        final TraceEntry newEntry = entries[index];
        newEntry.set(time, x, y, z, lastDistSq);
    }

    /** Reset content pointers - call with world changes. */
    public void reset() {
        index = 0;
        size = 0;
    }

    /**
     * Get the actual number of valid elements. After some time of moving this should be entries.length.
     * @return
     */
    public int size() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Get size of ring buffer (maximal possible number of elements).
     * @return
     */
    public int getMaxSize() {
        return entries.length;
    }

    public double getMergeDist() {
        return mergeDist;
    }

    /**
     * Iterate from latest to oldest.
     * @return
     */
    public TraceIterator latestIterator() {
        return new TraceIterator(entries, index, size, index, false);
    }

    /**
     * Iterate from oldest to latest.
     * @return
     */
    public TraceIterator oldestIterator() {
        final int currentIndex = index - size + 1;
        return new TraceIterator(entries, index, size, currentIndex < 0 ? currentIndex + entries.length : currentIndex, true);
    }


    /**
     * Iterate from entry with max. age to latest, always includes latest.
     * @param time Absolute time value for oldest accepted entries.
     * @return TraceIterator containing entries that have not been created before the given time, iterating ascending with time.
     */
    public TraceIterator maxAgeIterator(long time) {
        int currentIndex = index;
        int tempIndex = currentIndex;
        int steps = 1;
        while (steps < size) {
            tempIndex --;
            if (tempIndex < 0) {
                tempIndex += size;
            }
            final TraceEntry entry = entries[tempIndex];
            if (entry.time >= time) {
                // Continue.
                currentIndex = tempIndex;
            } else {
                break;
            }
            steps ++;
        }
        return new TraceIterator(entries, index, size, currentIndex, true);
    }

}
