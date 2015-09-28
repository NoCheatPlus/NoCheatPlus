package fr.neatmonster.nocheatplus.utilities.ds.map;

import java.util.Iterator;

/**
 * Intended for Minecraft coordinates, probably not for too high values.<br>
 * This implementation is not thread safe, though changing values and
 * get/contains should work if the map stays unchanged.
 * <hr>
 * Linked hash map implementation of CoordMap<V>, allowing for insertion/access
 * order. Default order is the order of insertion.
 * 
 * @author asofold
 *
 * @param <V>
 */
public class LinkedCoordHashMap<V> extends AbstractCoordHashMap<V, fr.neatmonster.nocheatplus.utilities.ds.map.LinkedCoordHashMap.LinkedHashEntry<V>> {

    // TODO: Implement linked structure + iterator (+reversed iteration).

    public static class LinkedHashEntry<V> extends fr.neatmonster.nocheatplus.utilities.ds.map.AbstractCoordHashMap.HashEntry<V> {

        public LinkedHashEntry(int x, int y, int z, V value, int hash) {
            // TODO: linked ...
            super(x, y, z, value, hash);
            throw new IllegalStateException("Not yet implemented."); // TODO: Implement.
        }

    }

    public boolean isAccessOrder() {
        throw new IllegalStateException("Not yet implemented."); // TODO: Implement.
    }

    public boolean isInsertionOrder() {
        throw new IllegalStateException("Not yet implemented."); // TODO: Implement.
    }

    @Override
    public Iterator<Entry<V>> iterator() {
        throw new IllegalStateException("Not yet implemented."); // TODO: Implement.
    }

    /**
     * Control order of iteration. Actual order depends on the accessOrder flag.
     * @param reversed
     * @return
     */
    public Iterator<Entry<V>> iterator(boolean reversed) {
        throw new IllegalStateException("Not yet implemented."); // TODO: Implement
    }

    @Override
    protected LinkedHashEntry<V> newEntry(int x, int y, int z, V value, int hash) {
        throw new IllegalStateException("Not yet implemented."); // TODO: Implement
    }

    

}
