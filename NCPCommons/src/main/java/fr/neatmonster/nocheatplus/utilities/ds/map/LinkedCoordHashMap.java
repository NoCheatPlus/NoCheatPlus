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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Intended for Minecraft coordinates, probably not for too high values.<br>
 * This implementation is not thread safe, though changing values and
 * get/contains should work if the map stays unchanged.
 * <hr>
 * Linked hash map implementation of CoordMap<V>, allowing for insertion/access
 * order. By default entries are added to the end. This implementation does not
 * imitate the LinkedHashMap behavior [may be adapted to be similar, if
 * desired], instead methods are provided for manipulating the order at will.
 * 
 * @author asofold
 *
 * @param <V>
 */
public class LinkedCoordHashMap<V> extends AbstractCoordHashMap<V, fr.neatmonster.nocheatplus.utilities.ds.map.LinkedCoordHashMap.LinkedHashEntry<V>> implements CoordMap<V> {

    // TODO: Add default order for get/put?
    // TODO: Tests.

    /**
     * Where to move an entry.
     * @author asofold
     *
     */
    public static enum MoveOrder {
        FRONT,
        NOT,
        END
    }

    public static class LinkedHashEntry<V> extends fr.neatmonster.nocheatplus.utilities.ds.map.AbstractCoordHashMap.HashEntry<V> {

        protected LinkedHashEntry<V> previous = null;
        protected LinkedHashEntry<V> next = null;

        public LinkedHashEntry(int x, int y, int z, V value, int hash) {
            super(x, y, z, value, hash);
        }

    }

    public static class LinkedHashIterator<V> implements Iterator<Entry<V>> {

        private final LinkedCoordHashMap<V> map;

        private LinkedHashEntry<V> current = null;
        private LinkedHashEntry<V> next;

        private final boolean reverse;

        protected LinkedHashIterator(LinkedCoordHashMap<V> map, boolean reverse) {
            this.map = map;
            this.reverse = reverse;
            next = reverse ? map.lastEntry : map.firstEntry;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public LinkedHashEntry<V> next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            current = next;
            next = reverse ? next.previous : next.next;
            return current;
        }

        @Override
        public void remove() {
            if (current != null) {
                // TODO: more efficient version ?
                map.remove(current.x, current.y, current.z);
                current = null;
            }
        }

    }

    protected LinkedHashEntry<V> firstEntry = null;
    protected LinkedHashEntry<V> lastEntry = null;

    public LinkedCoordHashMap() {
        super();
    }

    public LinkedCoordHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedCoordHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Move an entry to the start of the linked structure.
     * 
     * @param x
     * @param y
     * @param z
     * @return The value that was stored for the given coordinates. If no entry
     *         is present, null is returned.
     */
    public V moveToFront(final int x, final int y, final int z) {
        final LinkedHashEntry<V> entry = getEntry(x, y, z);
        if (entry == null) {
            return null;
        }
        removeEntry(entry);
        setFirst(entry);
        return entry.value;
    }

    /**
     * Move an entry to the end of the linked structure.
     * 
     * @param x
     * @param y
     * @param z
     * @return The value that was stored for the given coordinates. If no entry
     *         is present, null is returned.
     */
    public V moveToEnd(final int x, final int y, final int z) {
        final LinkedHashEntry<V> entry = getEntry(x, y, z);
        if (entry == null) {
            return null;
        }
        removeEntry(entry);
        setLast(entry);
        return entry.value;
    }

    /**
     * Convenience method to specify moving based on MoveOrder. Note that
     * MoveOrder.NOT leads to super.get being called, in order to be bale to
     * return a present value.
     * 
     * @param x
     * @param y
     * @param z
     * @param order
     * @return The value that was stored for the given coordinates. If no entry
     *         is present, null is returned.
     */
    public V move(final int x, final int y, final int z, final MoveOrder order) {
        switch (order) {
            case END:
                return moveToEnd(x, y, z);
            case FRONT:
                return moveToFront(x, y, z);
            default: 
                break;
        }
        // Ensure no changes.
        return super.get(x, y, z);
    }

    /**
     * Convenience method to control where the resulting entry is put to (front
     * or end).
     * 
     * @param x
     * @param y
     * @param z
     * @param value
     * @param moveToEnd
     * @return
     */
    public V put(final int x, final int y, final int z, final V value, final MoveOrder order) {
        // TODO: Optimized.
        final V previousValue = super.put(x, y, z, value);
        if (order == MoveOrder.FRONT) {
            moveToFront(x, y, z);
        }
        else if (previousValue != null && order == MoveOrder.END) {
            // Ensure the intended order.
            moveToEnd(x, y, z);
        }
        return previousValue;
    }

    public V get(final int x, final int y, final int z, final MoveOrder order) {
        // TODO: Optimized.
        final V value = super.get(x, y ,z);
        if (value != null && order != MoveOrder.NOT) {
            move(x, y, z, order);
        }
        return value;
    }

    @Override
    public LinkedHashIterator<V> iterator() {
        return new LinkedHashIterator<V>(this, false);
    }

    /**
     * Control order of iteration.
     * 
     * @param reversed
     * @return
     */
    public LinkedHashIterator<V> iterator(boolean reversed) {
        return new LinkedHashIterator<V>(this, reversed);
    }

    @Override
    protected LinkedHashEntry<V> newEntry(int x, int y, int z, V value, int hash) {
        LinkedHashEntry<V> entry = new LinkedHashEntry<V>(x, y, z, value, hash);
        // Always put in last.
        setLast(entry);
        return entry;
    }

    /**
     * Insert entry as the first element. Assumes the entry not to be linked.
     * 
     * @param entry
     */
    private void setFirst(LinkedHashEntry<V> entry) {
        if (this.firstEntry == null) {
            this.firstEntry = this.lastEntry = entry;
        }
        else {
            entry.next = this.firstEntry;
            this.firstEntry.previous = entry;
            this.firstEntry = entry;
        }
    }

    /**
     * Insert entry as the last element. Assumes the entry not to be linked.
     * 
     * @param entry
     */
    private void setLast(LinkedHashEntry<V> entry) {
        if (this.firstEntry == null) {
            this.firstEntry = this.lastEntry = entry;
        }
        else {
            entry.previous = this.lastEntry;
            this.lastEntry.next = entry;
            this.lastEntry = entry;
        }
    }

    @Override
    protected void removeEntry(LinkedHashEntry<V> entry) {
        // Just unlink.
        if (entry == this.firstEntry) {
            this.firstEntry = entry.next;
            if (this.firstEntry != null) {
                this.firstEntry.previous = null;
            }
        }
        else {
            entry.previous.next = entry.next;
        }
        if (entry == this.lastEntry) {
            this.lastEntry = entry.previous;
            if (this.lastEntry != null) {
                this.lastEntry.next = null;
            }
        }
        else {
            entry.next.previous = entry.previous;
        }
        entry.previous = entry.next = null;
    }

    @Override
    public void clear() {
        super.clear();
        firstEntry = lastEntry = null;
    }

}
