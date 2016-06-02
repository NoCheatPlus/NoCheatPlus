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
package fr.neatmonster.nocheatplus.utilities.ds.corw;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * More cows, more fun: Copy on write for a LinkedHashMap (optimized for fast reading from any thread).
 * <hr>
 * Shortcomings:<br>
 * <li>This does not allow access-ordered maps, use Collections.synchronizedMap(LinkedHashMap...) for that case.</li>
 * <li>Behavior of equals and hashCode could be problematic, because they have not been overridden, but in fact should be processed on copies of this map.</li>
 * @author dev1mc
 *
 */
public class LinkedHashMapCOW<K, V> implements Map<K, V> {

    // TODO: Consider a) add removeEldest... b) add option: copyMap(K) -> only copy if needed.  

    private LinkedHashMap<K, V> map;

    private final int initialCapacity;
    private final float loadFactor;

    /**
     * Uses: 16, 0.75f, false (default settings, insertion ordered).
     */
    public LinkedHashMapCOW() {
        this(16, 0.75f);
    }

    /**
     * Uses extra: 0.75f, false (default settings, insertion ordered).
     * @param initialCapacity
     */
    public LinkedHashMapCOW(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * Uses extra: false (default settings, insertion ordered).
     * @param initialCapacity
     * @param loadFactor
     */
    public LinkedHashMapCOW(int initialCapacity, float loadFactor) {
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.map = new LinkedHashMap<K, V>(initialCapacity, loadFactor, false);
    }

    /**
     * Uses: 16, 0.75f, false (default settings, insertion ordered).
     * @param map
     */
    public LinkedHashMapCOW(Map<K, V> map) {
        this();
        this.map.putAll(map);
    }

    /**
     * Not synchronized: return a copy of the internal map.
     * @return
     */
    private LinkedHashMap<K, V> copyMap() {
        final LinkedHashMap<K, V> newMap = new LinkedHashMap<K, V>(initialCapacity, loadFactor, false);
        newMap.putAll(this.map);
        return newMap;
    }

    @Override
    public void clear() {
        synchronized (this) {
            this.map.clear();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    /**
     * Unmodifiable version of the EntrySet. Entry.setValue might be possible, but dangerous :p
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    @Override
    public V get(Object key) {
        // NOTE: If accessOrder can be true, there needs to be synchronization here, defeating any purpose, better use Collections.synchronizedMap(LinkedHashMap...) for that case.
        return map.get(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Unmodifiable version of the KeySet.
     */
    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public V put(final K key, final V value) {
        final V out;
        synchronized (this) {
            final LinkedHashMap<K, V> newMap = copyMap();
            out = newMap.put(key, value);
            this.map = newMap;
        }
        return out;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        synchronized (this) {
            final LinkedHashMap<K, V> newMap = copyMap();
            newMap.putAll(m);
            this.map = newMap;
        }
    }

    @Override
    public V remove(final Object key) {
        final V out;
        synchronized (this) {
            final LinkedHashMap<K, V> newMap = copyMap();
            out = newMap.remove(key);
            this.map = newMap;
        }
        return out;
    }

    /**
     * Remove all given keys.<br>
     * Not the most efficient implementation, copying the map and then removing
     * keys, but still better than iterating remove(key).
     * 
     * @param keys
     */
    public void removeAll(final Collection<K> keys) {
        synchronized (this) {
            final LinkedHashMap<K, V> newMap = copyMap();
            for (final K key : keys) {
                newMap.remove(key);
            }
            this.map = newMap;
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    /**
     * Unmodifiable version of the values (Collection).
     */
    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(map.values());
    }

}
