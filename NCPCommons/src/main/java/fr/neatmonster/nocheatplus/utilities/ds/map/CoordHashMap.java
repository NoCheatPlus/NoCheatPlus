package fr.neatmonster.nocheatplus.utilities.ds.map;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Intended for Minecraft coordinates, probably not for too high values.<br>
 * This implementation is not thread safe, though changing values and
 * get/contains should work if the map stays unchanged.
 * <hr>
 * Simple hash map implementation of CoordMap<V>.
 * 
 * @author asofold
 *
 * @param <V>
 */
public class CoordHashMap<V> extends AbstractCoordHashMap<V, fr.neatmonster.nocheatplus.utilities.ds.map.AbstractCoordHashMap.HashEntry<V>> {

    // TODO: Move parts of abstract map here.

    /**
     * 
     * Simple version of an iterator just using the internal Buckets.
     * 
     * Does NOT throw anything, not the least thread safe.
     * 
     * @author mc_dev
     * 
     * @param <V>
     */
    public static class HashIterator<V> implements Iterator<Entry<V>> {

        // TODO: Switch to store an iterator?

        private final CoordHashMap<V> map;
        private final List<HashEntry<V>>[] entries;

        /** Current search position. */
        private int slot = 0;
        /** Current search position. */
        private int index = 0;

        /** Last found position. */
        private int slotLast = -1;
        /** Last found position. */
        private int indexLast = -1;

        protected HashIterator(final CoordHashMap<V> map) {
            this.map = map;
            entries = map.entries;
        }

        @Override
        public final boolean hasNext() {
            // Also set index and slot to next found element.
            while (slot < entries.length) {
                final List<HashEntry<V>> bucket = entries[slot];
                if (bucket == null){
                    slot ++;
                    index = 0;
                }
                else {
                    if (index < bucket.size()) {
                        return true;
                    }
                    else {
                        // Not found, reset.
                        slot ++;
                        index = 0;
                    }
                }
            }
            return false;
        }

        @Override
        public final Entry<V> next() {
            while (slot < entries.length) {
                final List<HashEntry<V>> bucket = entries[slot];
                if (bucket == null){
                    slot ++;
                    index = 0;
                }
                else {
                    final int size = bucket.size();
                    if (index < size) {
                        final Entry<V> res = bucket.get(index);
                        slotLast = slot;
                        indexLast = index;
                        index++;
                        if (index == size) {
                            index = 0;
                            slot++;
                        }
                        return res;
                    }
                    else{
                        // TODO: inconsistent, could be empty though.
                        slot++;
                        index = 0;
                    }
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public final void remove() {
            if (slotLast == -1){
                // Next had not been called at all,
                // or remove has been called several times.
                return;
            }
            final List<HashEntry<V>> bucket = entries[slotLast];
            bucket.remove(indexLast);
            if (bucket.isEmpty()) {
                entries[slotLast] = null;
            }
            else if (slotLast == slot) {
                index --;
            }
            map.size--;
            slotLast = indexLast = -1;
        }
    }

    public CoordHashMap() {
        super();
    }

    public CoordHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public Iterator<Entry<V>> iterator(){
        return new HashIterator<V>(this);
    }

    @Override
    protected fr.neatmonster.nocheatplus.utilities.ds.map.AbstractCoordHashMap.HashEntry<V> newEntry(int x, int y, int z, V value, int hash) {
        return new HashEntry<V>(x, y, z, value, hash);
    }

}
