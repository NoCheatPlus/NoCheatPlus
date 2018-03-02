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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock on write hash map. Less jumpy than cow, bucket oriented addition, bulk
 * remove. Does not implement the Map interface, due to it not being right for
 * this purpose.
 * <hr>
 * Field of use should be a thread-safe map, where get is not using locking and
 * where bulk (removal) operations can be performed with one time locking, while
 * entries usually stay for a longer time until expired.
 * <hr>
 * All changes are still done under lock. Iterators are meant to iterate
 * fail-safe, even on concurrent modification of the map. Calling remove() on an
 * iterator should relay to originalMap.remove(item). The internal size may not
 * ever shrink, at least not below targetSize, but it might grow with entries. <br>
 * Both null keys and null values are supported. 
 * 
 * @author asofold
 *
 */
public class HashMapLOW <K, V> {

    ///////////////////////
    // Static members
    ///////////////////////

    private static <K> int getHashCode(final K key) {
        return key == null ? 0 : key.hashCode();
    }

    private static int getBucketIndex(final int hashCode, final int buckets) {
        return Math.abs(hashCode) % buckets;
    }

    static class LHMEntry<K, V> implements Entry<K, V> {

        final int hashCode;
        final K key;
        V value;

        LHMEntry(int hashCode, K key, V value) {
            this.hashCode = hashCode;
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            final V oldValue = this.value; 
            this.value = value; 
            return oldValue;
        }

        @Override
        public int hashCode() {
            // By specification, not intended to be useful here.
            return hashCode ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            // By specification, not intended to be useful here.
            if (obj instanceof Entry<?, ?>) {
                final Entry<?, ?> entry = (Entry<?, ?>) obj;
                return obj == this
                        || (key == null ? entry.getKey() == null : key.equals(entry.getKey()))
                        && (value == null ? entry.getValue() == null : value.equals(entry.getValue()));
            }
            else {
                return false;
            }
        }

        /**
         * Delegate key comparison here, using a pre-calculated hash value.
         * 
         * @param otherHashCode
         * @param otherKey
         * @return
         */
        public boolean equalsKey(final int otherHashCode, final K otherKey) {
            if (otherHashCode != hashCode) {
                return false;
            }
            if (otherKey == key) {
                return true;
            }
            // (One of both still could be null.)
            return key != null && key.equals(otherKey);
        }

    }

    /**
     * Create with adding entries.
     * @author asofold
     *
     * @param <K>
     * @param <V>
     */
    static class LHMBucket<K, V> {

        // TODO: Link non-empty buckets.

        // TODO: final int index;

        int size = 0;

        /** Must be stored externally for iteration. */
        @SuppressWarnings("unchecked")
        LHMEntry<K, V>[] contents = (LHMEntry<K, V>[]) new LHMEntry[3]; // TODO: Configurable

        /**
         * Called under lock.
         * 
         * @param key
         * @param value
         * @param ifAbsent
         *            If true, an existing non-null (!) value will not be
         *            overridden.
         * @return
         */
        V put(final int hashCode, final K key, final V value, final boolean ifAbsent) {
            int emptyIndex;
            if (size == 0) {
                emptyIndex = 0;
                size ++;
            }
            else {
                emptyIndex = -1;
                LHMEntry<K, V> oldEntry = null;
                int entriesFound = 0;
                for (int i = 0; i < contents.length; i++) {
                    final LHMEntry<K, V> entry = contents[i];
                    if (entry != null) {
                        entriesFound ++;
                        if (entry.equalsKey(hashCode, key)) {
                            oldEntry = entry;
                            break;
                        }
                        else if (entriesFound == size && emptyIndex != -1) {
                            // TODO: Not sure this is just overhead for most cases.
                            break;
                        }
                    }
                    else if (emptyIndex == -1) {
                        emptyIndex = i;
                    }
                }
                if (oldEntry != null) {
                    final V oldValue = oldEntry.getValue();
                    if (oldValue == null || !ifAbsent) {
                        oldEntry.setValue(value);
                    }
                    return oldValue;
                }
            }
            // Create a new Entry.
            final LHMEntry<K, V> newEntry = new LHMEntry<K, V>(hashCode, key, value);
            if (emptyIndex == -1) {
                // Grow.
                grow(newEntry);
            }
            else {
                contents[emptyIndex] = newEntry;
            }
            size ++;
            return null;
        }

        /**
         * Called under lock.
         * @param entry The entry to add (reason for growth).
         */
        private void grow(final LHMEntry<K, V> entry) {
            final int oldLength = contents.length;
            @SuppressWarnings("unchecked")
            LHMEntry<K, V>[] newContents = (LHMEntry<K, V>[]) new LHMEntry[contents.length + Math.max(2, contents.length / 3)];
            System.arraycopy(contents, 0, newContents, 0, contents.length);
            newContents[oldLength] = entry;
            contents = newContents;
        }

        /**
         * Blind adding of the entry to a free place. Called under lock.
         * 
         * @param entry
         */
        void addEntry(LHMEntry<K, V> entry) {
            size ++;
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] == null) {
                    contents[i] = entry;
                    return;
                }
            }
            // Need to grow.
            grow(entry);
        }

        /**
         * Called under lock.
         * @param hashCode
         * @param key
         * @return
         */
        V remove(final int hashCode, final K key) {
            if (size == 0) {
                return null;
            }
            else {
                for (int i = 0; i < contents.length; i++) {
                    final LHMEntry<K, V> entry = contents[i];
                    if (entry != null && entry.equalsKey(hashCode, key)) {
                        contents[i] = null;
                        size --;
                        return entry.getValue();
                    }
                }
                return null;
            }
        }

        /**
         * Not necessarily called under lock.
         * @param hashCode
         * @param key
         * @return
         */
        V get(final int hashCode, final K key) {
            final LHMEntry<K, V>[] contents = this.contents; // Mind iteration.
            if (size == 0) {
                return null;
            }
            else {
                for (int i = 0; i < contents.length; i++) {
                    final LHMEntry<K, V> entry = contents[i];
                    if (entry != null && entry.equalsKey(hashCode, key)) {
                        return entry.getValue();
                    }
                }
                return null;
            }
        }

        /**
         * Not necessarily called under lock.
         * @param hashCode
         * @param key
         * @return
         */
        boolean containsKey(final int hashCode, final K key) {
            final LHMEntry<K, V>[] contents = this.contents; // Mind iteration.
            if (size == 0) {
                return false;
            }
            else {
                for (int i = 0; i < contents.length; i++) {
                    final LHMEntry<K, V> entry = contents[i];
                    if (entry != null && entry.equalsKey(hashCode, key)) {
                        return true;
                    }
                }
                return false;
            }
        }

        /**
         * Called under lock.
         */
        void clear() {
            Arrays.fill(contents, null); // (Entries might be reused on iteration.)
            size = 0;
        }

    }

    static class LHMIterator<K, V> implements Iterator<Entry<K, V>> {

        private final HashMapLOW<K, V> map;
        private LHMBucket<K, V>[] buckets;
        /** Next index to check. */
        private int bucketsIndex = 0; 
        private LHMEntry<K, V>[] currentBucket = null;
        /** Next index to check. */
        private int currentBucketIndex = 0;
        private LHMEntry<K, V> currentEntry = null;
        private K lastReturnedKey = null;

        LHMIterator(HashMapLOW<K, V> map, LHMBucket<K, V>[] buckets) {
            this.map = map;
            this.buckets = buckets;
            // (Lazily advance.)
        }

        /**
         * Advance internal state (generic/root). Set currentEntry or reset
         * buckets to null.
         */
        private void advance() {
            currentEntry = null;
            if (buckets == null || currentBucket != null && advanceBucket()) {
                return;
            }

            for (int i = bucketsIndex; i < buckets.length; i++) {
                final LHMBucket<K, V> bucket = buckets[i];
                if (bucket != null) {
                    this.currentBucket = bucket.contents; // Index should already be 0.
                    if (advanceBucket()) {
                        this.bucketsIndex = i + 1; // Next one.
                        return;
                    }
                }
            }
            // No remaining entries, finished.
            buckets = null;
        }

        /**
         * Advance within currentBucket. Reset if nothing found.
         * @return true if something was found.
         */
        private boolean advanceBucket() {
            // First attempt to advance within first bucket.
            for (int i = currentBucketIndex; i < currentBucket.length; i++) {
                final LHMEntry<K, V> entry = currentBucket[i];
                if (entry != null) {
                    currentBucketIndex = i + 1;
                    currentEntry = entry;
                    return true;
                }
            }
            // Nothing found, reset.
            currentBucket = null;
            currentBucketIndex = 0;
            return false;
        }

        @Override
        public boolean hasNext() {
            if (currentEntry != null) {
                return true;
            }
            else if (buckets == null) {
                return false;
            }
            else {
                advance();
                return currentEntry != null;
            }
        }

        @Override
        public Entry<K, V> next() {
            // Lazily advance.
            if (currentEntry == null) {
                advance();
                if (currentEntry == null) {
                    buckets = null;
                    throw new NoSuchElementException();
                }
            }
            final Entry<K, V> entry = currentEntry;
            lastReturnedKey = entry.getKey();
            currentEntry = null;
            return entry;
        }

        @Override
        public void remove() {
            if (lastReturnedKey == null) {
                throw new IllegalStateException();
            }
            map.remove(lastReturnedKey); // TODO: CAN NOT WORK, NEED INVALIDATE ENTRY OTHERWISE
            lastReturnedKey = null;
        }

    }

    static class LHMIterable<K, V> implements Iterable<Entry<K,V>> {

        private final Iterator<Entry<K, V>> iterator;

        LHMIterable(Iterator<Entry<K, V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return iterator;
        }

    }


    ///////////////////////
    // Instance members
    ///////////////////////

    private final Lock lock;

    /** Intended/expected size. */
    private final int targetSize;
    /** Lazily filled with objects (null iff empty). */
    private LHMBucket<K, V>[] buckets;
    private int size = 0;

    private float loadFactor = 0.75f;

    // TODO: Configurable: loadFactor
    // TODO: Configurable: initial size and resize multiplier for Buckets.
    // TODO: Configurable: allow shrink.

    /**
     * Initialize with a ReentrantLock.
     * @param targetSize
     *            Expected (average) number of elements in the map.
     */
    public HashMapLOW(int targetSize) {
        this(new ReentrantLock(), targetSize);
    }

    /**
     * Initialize with a certain lock.
     * @param lock
     * @param targetSize
     */
    public HashMapLOW(Lock lock, int targetSize) {
        this.lock = lock;
        this.targetSize = targetSize;
        buckets = newBuckets(targetSize);
    }

    /**
     * New buckets array for the given number of items.
     * 
     * @param size
     * @return A new array to hold the given number of elements (size), using
     *         internal settings.
     */
    @SuppressWarnings("unchecked")
    private LHMBucket<K, V>[] newBuckets(int size) {
        return (LHMBucket<K, V>[]) new LHMBucket[Math.max((int) ((1f / loadFactor) * (float) size), targetSize)];
    }

    /**
     * Resize according to the number of elements. Called under lock.
     */
    private void resize() {
        final LHMBucket<K, V>[] newBuckets = newBuckets(size); // Hold current number of elements.
        final int newLength = newBuckets.length;
        // Entries are reused, but not buckets (buckets would break iteration).
        for (int index = 0; index < buckets.length; index++) {
            final LHMBucket<K, V> bucket = buckets[index];
            if (bucket != null && bucket.size > 0) {
                for (int j = 0; j < bucket.contents.length; j++) {
                    final LHMEntry<K, V> entry = bucket.contents[j];
                    if (entry != null) {
                        final int newIndex = getBucketIndex(entry.hashCode, newLength);
                        LHMBucket<K, V> newBucket = newBuckets[newIndex];
                        if (newBucket == null) {
                            newBucket = new LHMBucket<K, V>();
                            newBuckets[newIndex] = newBucket;
                        }
                        newBucket.addEntry(entry);
                    }
                }
            }
        }
        buckets = newBuckets;
    }

    /**
     * Not under Lock.
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Not under lock.
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clear the map, detaching from iteration by unlinking storage containers.
     */
    public void clear() {
        lock.lock();
        buckets = newBuckets(targetSize);
        size = 0;
        lock.unlock();
    }

    /**
     * Immediate put, under lock.
     * 
     * @param key
     * @param value
     * @return
     */
    public V put(final K key, final V value) {
        return put(key, value, false);
    }

    /**
     * Immediate put, only if there is no value or a null value set for the key,
     * under lock.
     * 
     * @param key
     * @param value
     * @return
     */
    public V putIfAbsent(final K key, final V value) {
        return put(key, value, true);
    }

    /**
     * 
     * @param key
     * @param value
     * @param ifAbsent
     *            If true, an existing non-null (!) value will not be
     *            overridden.
     * @return
     */
    private final V put(final K key, final V value, final boolean ifAbsent) {
        final int hashCode = getHashCode(key);
        lock.lock();
        final int index = getBucketIndex(hashCode, buckets.length);
        LHMBucket<K, V> bucket = buckets[index];
        if (bucket == null) {
            bucket = new LHMBucket<K, V>();
            buckets[index] = bucket;
        }
        V oldValue = bucket.put(hashCode, key, value, ifAbsent);
        if (oldValue == null) {
            size ++;
            if (size > (int) (loadFactor * (float) buckets.length)) {
                resize();
            }
        }
        lock.unlock();
        return oldValue;
    }

    /**
     * Immediate remove, under lock.
     * 
     * @param key
     * @return
     */
    public V remove(final K key) {
        final int hashCode = getHashCode(key);
        lock.lock();
        final V value = removeUnderLock(hashCode, key);
        // TODO: Shrink, if necessary.
        lock.unlock();
        return value;
    }

    /**
     * Remove a value for a given key. Called under lock. Not intended to
     * shrink, due to being called on bulk removal.
     * 
     * @param hashCode
     * @param key
     * @return
     */
    private V removeUnderLock(final int hashCode, final K key) {
        final int index = getBucketIndex(hashCode, buckets.length);
        final LHMBucket<K, V> bucket = buckets[index];
        if (bucket == null || bucket.size == 0) {
            return null;
        }
        else {
            final V value = bucket.remove(hashCode, key);
            if (value != null) {
                size --;
            }
            return value;
        }

    }

    /**
     * Remove all given keys, using minimal locking.
     * 
     * @param keys
     */
    public void remove(final Collection<K> keys) {
        lock.lock();
        for (final K key : keys) {
            final int hashCode = getHashCode(key);
            removeUnderLock(hashCode, key);
        }
        lock.unlock();
    }

    /**
     * Retrieve a value. Does not use locking.
     * 
     * @param key
     *            The stored value for the given key. Returns null if no value
     *            is stored.
     */
    public V get(final K key) {
        final int hashCode = getHashCode(key);
        final LHMBucket<K, V>[] buckets = this.buckets;
        final LHMBucket<K, V> bucket = buckets[getBucketIndex(hashCode, buckets.length)];
        if (bucket == null || bucket.size == 0) {
            return null;
        }
        else {
            return bucket.get(hashCode, key);
        }
    }

    /**
     * Retrieve a value for a given key, or null if not existent. This method
     * uses locking.
     * 
     * @param key
     */
    public V getLocked(final K key) {
        lock.lock();
        final V value = get(key);
        lock.unlock();
        return value;
    }

    /**
     * Test if a mapping for this key exists. Accurate if key is null. Does not
     * use locking.
     * 
     * @param key
     * @return
     */
    public boolean containsKey(final K key) {
        final int hashCode = getHashCode(key);
        final LHMBucket<K, V>[] buckets = this.buckets;
        final LHMBucket<K, V> bucket = buckets[getBucketIndex(hashCode, buckets.length)];
        if (bucket == null || bucket.size == 0) {
            return false;
        }
        else {
            return bucket.containsKey(hashCode, key);
        }
    }

    /**
     * Test if a mapping for this key exists. Accurate if key is null. This does
     * use locking.
     * 
     * @param key
     * @return
     */
    public boolean containsKeyLocked(final K key) {
        lock.lock();
        final boolean res = containsKey(key);
        lock.unlock();
        return res;
    }

    /**
     * Get an iterator reflecting this 'stage of resetting'. During iteration,
     * entries may get removed or added, values changed. Concurrent modification
     * will not let the iteration fail.
     * <hr>
     * This operation does not use locking.
     * 
     * @return
     */
    public Iterator<Entry<K, V>> iterator() {
        return size == 0 ? new LHMIterator<K, V>(null, null) : new LHMIterator<K, V>(this, buckets);
    }

    /**
     * Get an Iterable containing the same iterator, as is returned by
     * iterator(). See: {@link #iterator()}
     * 
     * @return
     */
    public Iterable<Entry<K, V>> iterable() {
        return new LHMIterable<K, V>(iterator());
    }

    /**
     * Get all keys as a LinkedHashSet fit for iteration. The returned set is a
     * new instance, so changes don't affect the original HashMapLOW instance.
     * 
     * @return
     */
    public Collection<K> getKeys() {
        final Set<K> out = new LinkedHashSet<K>();
        final Iterator<Entry<K, V>> it = iterator();
        while (it.hasNext()) {
            out.add(it.next().getKey());
        }
        return out;
    }

}
