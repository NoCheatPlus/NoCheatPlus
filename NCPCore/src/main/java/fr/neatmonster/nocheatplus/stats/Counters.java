package fr.neatmonster.nocheatplus.stats;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;

/**
 * Utility to count things, set up to get input from the primary server thread, as
 * well as from other threads. Consequently summaries for both are only
 * available from the primary thread.
 * 
 * @author dev1mc
 *
 */
public class Counters {
	
	/** Map strings for display/processing to "fast-access" ids. */
	private final Map<String, Integer> idMap = new LinkedHashMap<String, Integer>();
	
	/** Keys by id. */
	private String[] keys = new String[0];
	// Not sure if to use longs.
	/** Primary thread. */
	private int[] ptCounts = new int[0];
	/** Synchronized. */
	private int[] syCounts = new int[0];
	// TODO: Consider adding extra counts or ActionFrequency to track "n per minute".
	
	/**
	 * Register a key and return the id that is used for access. If the key is already registered, the registered id is returned.<br>
	 * Must only be called from the primary thread, or during (encapsulated) initialization.
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
		final int newId = ptCounts.length;
		idMap.put(key,  newId);
		keys = Arrays.copyOf(keys, newId + 1);
		keys[newId] = key;
		ptCounts = Arrays.copyOf(ptCounts, newId + 1);
		synchronized (syCounts) {
			syCounts = Arrays.copyOf(syCounts, newId + 1);
		}
		return newId;
	}
	
	/**
	 * Convenience method for quick testing / uncertain contexts, checks
	 * Bukkit.isPrimaryThread(), then delegates, thus is slower.
	 * 
	 * @param id
	 * @param count
	 */
	public void add(int id, int count) {
		if (Bukkit.isPrimaryThread()) {
			addPrimaryThread(id, count);
		} else {
			addSynchronized(id, count);
		}
	}
	
	/**
	 * Only call from the primary thread.
	 * @param id
	 * @param count
	 */
	public void addPrimaryThread(int id, int count) {
		ptCounts[id] += count;
	}
	
	/**
	 * Call from any thread.
	 * @param id
	 * @param count
	 */
	public void addSynchronized(int id, int count) {
		synchronized (syCounts) {
			syCounts[id] += count;
		}
	}
	
	/**
	 * Reset all counters to 0.<br>
	 * Must only be called from the primary thread.
	 */
	public void resetAll() {
		for (int i = 0; i < ptCounts.length; i ++) {
			ptCounts[i] = 0;
		}
		synchronized (syCounts) {
			for (int i = 0; i < syCounts.length; i ++) {
				syCounts[i] = 0;
			}
		}
	}
	
	public Map<String, Integer> getPrimaryThreadCounts() {
		final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
		final int length = keys.length;
		for (int i = 0; i < length; i++) {
			counts.put(keys[i], ptCounts[i]);
		}
		return counts;
	}
	
	public Map<String, Integer> getSynchronizedCounts() {
		final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
		final int[] syCounts;
		synchronized (this.syCounts) {
			syCounts = Arrays.copyOf(this.syCounts, this.syCounts.length);
		}
		for (int i = 0; i < syCounts.length; i++) {
			counts.put(keys[i], syCounts[i]);
		}
		return counts;
	}
	
	/**
	 * Get a map for keys to counts, preserving the registration order of keys
	 * for iteration (LinkedHashMap).<br>
	 * Only call from the primary thread.
	 * 
	 * @return
	 */
	public Map<String, Integer> getMergedCounts() {
		final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
		final int[] syCounts;
		synchronized (this.syCounts) {
			syCounts = Arrays.copyOf(this.syCounts, this.syCounts.length);
		}
		for (int i = 0; i < syCounts.length; i++) {
			counts.put(keys[i], syCounts[i] + ptCounts[i]);
		}
		return counts;
	}
	
	/**
	 * Return a String (one line), which summarizes the contents: key merged-count.<br>
	 * Only call in the primary thread.
	 * @return
	 */
	public String getMergedCountsString() {
		return getMergedCountsString(false);
	}
	
	/**
	 * Return a String (one line), which summarizes the contents: key merged-count (pt count / sy count).<br>
	 * Only call in the primary thread.
	 * @param details If to show difference of primary thread / synchronized.
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
