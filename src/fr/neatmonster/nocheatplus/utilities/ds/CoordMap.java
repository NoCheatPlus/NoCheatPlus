package fr.neatmonster.nocheatplus.utilities.ds;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Map int coordinates to values (just for fun). Intended for Minecraft coordinates, probably not for too high values.<br>
 * This implementation is not thread safe, though changing values and get/contains should work if the map stays unchanged.
 * @author mc_dev
 *
 * @param <V>
 */
public class CoordMap<V> {
    
    private static final int p1 = 73856093;
    private static final int p2 = 19349663;
    private static final int p3 = 83492791;
    
    private static final int getHash(final int x, final int y, final int z) {
        return p1 * x ^ p2 * y ^ p3 * z;
    }
    
    private static final class Entry<V>{
        protected final int x;
        protected final int y;
        protected final int z;
        protected V value;
        protected final int hash;
        public Entry(final int x, final int y, final int z, final V value, final int hash){
            this.x = x;
            this.y = y;
            this.z = z;
            this.value = value;
            this.hash = hash;
        }
    }
    
    // Core data.
    private final float loadFactor;
    private List<Entry<V>>[] entries;
    /** Current size. */
    private int size = 0;
    
    public CoordMap(){
        this(10, 0.75f);
    }
    
    public CoordMap(final int initialCapacity){
        this(initialCapacity, 0.75f);
    }
    
    /**
     * 
     * @param initialCapacity Initial internal array size. <br>
     * TODO: change to expected number of elements (len = cap/load).
     * @param loadFactor
     */
    @SuppressWarnings("unchecked")
    public CoordMap(final int initialCapacity, float loadFactor){
        this.loadFactor = loadFactor;
        entries = new List[initialCapacity];
    }
    
    /**
     * Check if the map contains a value for the given coordinates.<br>
     * NOTE: Delegates to get, use get for fastest checks.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public final boolean contains(final int x, final int y, final int z){
        return get(x,y,z) != null;
    }
    
    /**
     * Get the value if there is a mapping for the given coordinates.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public final V get(final int x, final int y, final int z){
        final int hash = getHash(x, y, z);
        final int slot = Math.abs(hash) % entries.length;
        final List<Entry<V>> bucket = entries[slot];
        if (bucket == null) return null;;
        for (final Entry<V> entry : bucket){
            if (hash == entry.hash && x == entry.x && z == entry.z && y == entry.y) return entry.value;
        }
        return null;
    }
    
    /**
     * Add value with the coordinates + hash from the last contains call.
     * @param value
     * @return If a value was replaced.
     */
    public final boolean put(final int x, final int y, final int z, final V value){
        final int hash = getHash(x, y, z);
        final int absHash = Math.abs(hash);
        int slot = absHash % entries.length;
        List<Entry<V>> bucket = entries[slot];
        if (bucket != null){
            for (final Entry<V> entry : bucket){
                if (hash == entry.hash && x == entry.x && y == entry.z && z == entry.y){
                    entry.value = value;
                    return true;
                }
            }
        }
        else if (size + 1 > entries.length * loadFactor){
                resize(size + 1);
                slot = absHash % entries.length;
                bucket = entries[slot];
        }
        if (bucket == null) {
            // TODO: use array list ?
            bucket = new LinkedList<Entry<V>>();
            entries[slot] = bucket;
        }
        entries[slot] = bucket;
        bucket.add(new Entry<V>(x, y, z, value, hash));
        size ++;
        return false;
    }
    
    private final void resize(final int size) {
        // TODO: other capacity?
        final int newCapacity = (int) ((size + 4) / loadFactor);
        @SuppressWarnings("unchecked")
        final List<Entry<V>>[] newEntries = new List[newCapacity];
        for (int oldSlot = 0; oldSlot < entries.length; oldSlot++){
            final List<Entry<V>> oldBucket = entries[oldSlot];
            if (oldBucket != null){
                for (final Entry<V> entry : oldBucket){
                    final int newSlot = Math.abs(entry.hash) % newCapacity;
                    List<Entry<V>> newBucket = newEntries[oldSlot];
                    if (newBucket == null){
                        newBucket = new LinkedList<Entry<V>>();
                        newEntries[newSlot] = newBucket;
                    }
                    newBucket.add(entry);
                }
                oldBucket.clear();
            }
            entries[oldSlot] = null;
        }
        entries = newEntries;
    }

    public final int size(){
        return size;
    }
    
    public void clear(){
        size = 0;
        Arrays.fill(entries, null);
        // TODO: resize ?
    }
}
