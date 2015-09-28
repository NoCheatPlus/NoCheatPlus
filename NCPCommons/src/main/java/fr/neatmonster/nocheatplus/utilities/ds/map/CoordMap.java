package fr.neatmonster.nocheatplus.utilities.ds.map;

import java.util.Iterator;

/**
 * Map integer coordinates to values (just for fun).
 * 
 * @author asofold
 * 
 * @param <V>
 *            Type of the values to contain for a triple of coordinates.
 */
public interface CoordMap<V> {

    /**
     * Entry for iteration.
     * 
     * @author asofold
     *
     * @param <V>
     */
    public static interface Entry<V> {
        public int getX();
        public int getY();
        public int getZ();
        public V getValue();
    }

    /**
     * Check if the map contains a value for the given coordinates.<br>
     * NOTE: Delegates to get, use get for fastest checks.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean contains(final int x, final int y, final int z);

    /**
     * Get the value if there is a mapping for the given coordinates.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public V get(final int x, final int y, final int z);

    /**
     * Add value with the coordinates + hash from the last contains call.
     * 
     * @param value
     * @return If a value was replaced.
     */
    public boolean put(final int x, final int y, final int z, final V value);

    /**
     * Remove an entry.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public V remove(final int x, final int y, final int z);

    /**
     * Get the number of stored elements.
     * @return
     */
    public int size();

    /**
     * Remove all entries from the map.
     */
    public void clear();

    /**
     * Iterator over all elements (default order to be specified).
     * <hr>
     * There is no guarantee that any checks for concurrent modification are
     * performed.
     * 
     * @return
     */
    public Iterator<Entry<V>> iterator();

}
