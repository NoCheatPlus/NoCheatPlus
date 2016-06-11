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
package fr.neatmonster.nocheatplus.utilities.ds.map;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Intended for Minecraft coordinates, probably not for too high values.<br>
 * This implementation is not thread safe, though changing values and
 * get/contains should work if the map stays unchanged.
 * 
 * <br>
 * Abstract base implementation for a hash map version.
 * 
 * @author asofold
 *
 */
public abstract class AbstractCoordHashMap<V, E extends fr.neatmonster.nocheatplus.utilities.ds.map.AbstractCoordHashMap.HashEntry<V>> implements CoordMap<V> {

    public static class HashEntry<V> implements Entry<V>{
        protected final int x;
        protected final int y;
        protected final int z;
        protected V value;
        protected final int hash;

        public HashEntry(final int x, final int y, final int z, final V value, final int hash) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.value = value;
            this.hash = hash;
        }
        @Override
        public final int getX(){
            return x;
        }
        @Override
        public final int getY(){
            return y;
        }
        @Override
        public final int getZ(){
            return z;
        }
        @Override
        public final V getValue(){
            return value;
        }
    }

    // Core data.
    private final float loadFactor;
    protected List<E>[] entries;
    /** Current size. */
    protected int size = 0;

    public AbstractCoordHashMap() {
        this(10, 0.75f);
    }

    public AbstractCoordHashMap(final int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * 
     * @param initialCapacity
     *            Initial internal array size. <br>
     *            TODO: change to expected number of elements (len = cap/load).
     * @param loadFactor
     */
    @SuppressWarnings("unchecked")
    public AbstractCoordHashMap(final int initialCapacity, float loadFactor) {
        this.loadFactor = loadFactor;
        entries = new List[initialCapacity];
    }

    @Override
    public boolean contains(final int x, final int y, final int z) {
        return get(x, y, z) != null;
    }

    @Override
    public V get(final int x, final int y, final int z) {
        final E entry = getEntry(x, y ,z);
        return entry == null ? null : entry.value;
    }

    /**
     * Just get an entry.
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected E getEntry(final int x, final int y, final int z) {
        final int hash = CoordHash.hashCode3DPrimes(x, y, z);
        final int slot = Math.abs(hash) % entries.length;
        final List<E> bucket = entries[slot];
        if (bucket == null) {
            return null;
        }
        for (final E entry : bucket) {
            if (hash == entry.hash && x == entry.x && z == entry.z && y == entry.y) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public V put(final int x, final int y, final int z, final V value) {
        final int hash = CoordHash.hashCode3DPrimes(x, y, z);
        final int absHash = Math.abs(hash);
        int slot = absHash % entries.length;
        List<E> bucket = entries[slot];
        if (bucket != null) {
            for (final E entry : bucket) {
                if (hash == entry.hash && x == entry.x && z == entry.z && y == entry.y) {
                    final V previousValue = entry.value;
                    entry.value = value;
                    return previousValue;
                }
            }
        } else if (size + 1 > entries.length * loadFactor) {
            resize(size + 1);
            slot = absHash % entries.length;
            bucket = entries[slot];
        }
        if (bucket == null) {
            // TODO: use array list ?
            bucket = new LinkedList<E>();
            entries[slot] = bucket;
        }
        bucket.add(newEntry(x, y, z, value, hash));
        size++;
        return null;
    }

    @Override
    public V remove(final int x, final int y, final int z) {
        final int hash = CoordHash.hashCode3DPrimes(x, y, z);
        final int absHash = Math.abs(hash);
        int slot = absHash % entries.length;
        final List<E> bucket = entries[slot];
        if (bucket == null) {
            return null;
        }
        else {
            final Iterator<E> it = bucket.iterator(); 
            while (it.hasNext()) {
                final E entry = it.next();
                if (entry.hash == hash && x == entry.x && z == entry.z && y == entry.y) {
                    it.remove();
                    size--;
                    if (bucket.isEmpty()) {
                        entries[slot] = null;
                    }
                    removeEntry(entry);
                    return entry.value;
                }
            }
            return null;
        }
    }

    private void resize(final int size) {
        // TODO: other capacity / allow to set strategy [also for reducing for long time use]
        final int newCapacity =  Math.min(Math.max((int) ((size + 4) / loadFactor), entries.length + entries.length / 4), 4);
        @SuppressWarnings("unchecked")
        final List<E>[] newEntries = new List[newCapacity];
        int used = -1; //  Fill old buckets to front of old array.
        for (int oldSlot = 0; oldSlot < entries.length; oldSlot++) {
            final List<E> oldBucket = entries[oldSlot];
            if (oldBucket == null) {
                continue;
            }
            for (final E entry : oldBucket) {
                final int newSlot = Math.abs(entry.hash) % newCapacity;
                List<E> newBucket = newEntries[newSlot];
                if (newBucket == null) {
                    if (used < 0) {
                        newBucket = new LinkedList<E>();
                    }
                    else{
                        newBucket = entries[used];
                        entries[used] = null;
                        used--;
                    }
                    newEntries[newSlot] = newBucket;
                }
                newBucket.add(entry);
            }
            oldBucket.clear();
            entries[oldSlot] = null;
            entries[++used] = oldBucket;
        }
        entries = newEntries;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        if (size > 0) {
            size = 0;
            Arrays.fill(entries, null);
        }
        // TODO: resize ?
    }

    /**
     * Get a new entry. This method can have side effects (linked structures
     * etc.), it exists solely for the purpose of adding new entries within
     * put(...).
     * 
     * @param x
     * @param y
     * @param z
     * @param value
     * @param hash
     * @return
     */
    protected abstract E newEntry(int x, int y, int z, V value, int hash);

    /**
     * Called after removing an entry from the internal storage.
     * 
     * @param entry
     */
    protected void removeEntry(E entry) {
        // Override if needed.
    }

}
