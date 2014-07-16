package fr.neatmonster.nocheatplus.utilities.ds;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * More cows, more fun: Copy on write for a LinkedHashMap (optimized for fast reading from any thread).
 * <hr>
 * This does not allow access-ordered maps, use Collections.synchronizedMap(LinkedHashMap...) for that case.
 * @author dev1mc
 *
 */
public class LinkedHashMapCOW<K, V> implements Map<K, V> {
	
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
	public V put(K key, V value) {
		final V out;
		synchronized (this) {
			final LinkedHashMap<K, V> newMap = copyMap();
			out = newMap.put(key, value);
			this.map = newMap;
		}
		return out;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		synchronized (this) {
			final LinkedHashMap<K, V> newMap = copyMap();
			newMap.putAll(m);
			this.map = newMap;
		}
	}

	@Override
	public V remove(Object key) {
		final V out;
		synchronized (this) {
			final LinkedHashMap<K, V> newMap = copyMap();
			out = newMap.remove(key);
			this.map = newMap;
		}
		return out;
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
