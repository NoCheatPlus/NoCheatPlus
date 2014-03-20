package fr.neatmonster.nocheatplus.checks.moving;

import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * This class is meant to record locations for players moving, in order to allow to be more   
 * lenient for the case of latency.
 * @author mc_dev
 *
 */
public class LocationTrace {
	
	public static final class TraceEntry {
		
		/** We keep it open, if ticks or ms are used. */
		public long time;
		/** Coordinates. */
		public double x, y, z;
		
		public void set(long time, double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.time = time;
		}
	}
	
	/**
	 * Iterate from oldest to latest. Not a fully featured Iterator.
	 * @author mc_dev
	 *
	 */
	public static final class TraceIterator {
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
		
		public final boolean hasNext() {
			// Just check if currentIndex is within range.
			return currentIndex >= 0 && currentIndex <= index && currentIndex > index - size || currentIndex > index && currentIndex >= index - size + entries.length;
		}
		
	}
	
	/** A Ring. */
	private final TraceEntry[] entries;
	/** Last element index. */
	private int index = -1;
	/** Number of valid entries. */
	private int size = 0;
	private final double mergeDistSq;
	
	// (No world name stored: Should be reset on world changes.)
	
	public LocationTrace(int bufferSize, double mergeDist) {
		// TODO: Specify entry-merging conditions.
		if (bufferSize < 1) {
			throw new IllegalArgumentException("Expect bufferSize > 0, got instead: " + bufferSize);
		}
		entries = new TraceEntry[bufferSize];
		for (int i = 0; i < bufferSize; i++) {
			entries[i] = new TraceEntry();
		}
		this.mergeDistSq = mergeDist * mergeDist;
	}
	
	public final void addEntry(final long time, final double x, final double y, final double z) {
		// TODO: Consider setting the squared distance to last entry
		if (size > 0) {
			final TraceEntry oldEntry = entries[index];
			// TODO: Consider duration of staying there ?
			if (x == oldEntry.x && y == oldEntry.y && z == oldEntry.z) {
				oldEntry.time = time;
				return;
			}
			else if (TrigUtil.distanceSquared(x, y, z, oldEntry.x, oldEntry.y, oldEntry.z) <= mergeDistSq) {
				// TODO: Could use Manhattan, after all.
				/**
				 * TODO: <br>
				 * The last entry has to be up to date, but it can lead to a "stray entry" far off the second one on time.<br>
				 * Introducing a mergeTime could also help against keeping too many outdated entries.<br>
				 * On merging conditions, checking dist/time vs. the second latest element could be feasible, supposedly with double distance. <br>
				 */
				oldEntry.set(time, x, y, z);
				return;
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
		newEntry.set(time, x, y, z);
	}
	
	/** Reset content pointers - call with world changes. */
	public void reset() {
		index = 0;
		size = 0;
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
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
	 * @param tick Absolute tick value for oldest accepted tick.
	 * @param ms Absolute ms value for oldest accepted ms;
	 * @return
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
