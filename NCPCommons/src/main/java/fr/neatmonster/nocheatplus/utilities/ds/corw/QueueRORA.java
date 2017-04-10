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

import java.util.LinkedList;
import java.util.List;


/**
 * IQueueRORA implementation using the synchronized keyword for locking, with a
 * LinkedList inside.
 * 
 * @author asofold
 *
 * @param <E>
 */
public class QueueRORA<E> implements IQueueRORA<E> {

    private LinkedList<E> elements = new LinkedList<E>();

    @Override
    public int add(final E element) {
        final int size;
        synchronized (this) {
            elements.add(element);
            size = elements.size();
        }
        return size;
    }

    @Override
    public List<E> removeAll() {
        final List<E> result;
        synchronized (this) {
            result = elements;
            elements = new LinkedList<E>();
        }
        return result;
    }

    @Override
    public int reduce(final int maxSize) {
        int dropped = 0;
        synchronized (this) {
            final int size = elements.size();
            if (size  <= maxSize) {
                return dropped;
            }
            while (dropped < size - maxSize) {
                elements.removeFirst();
                dropped ++;
            }
        }
        return dropped;
    }

    @Override
    public void clear() {
        removeAll();
    }

    @Override
    public boolean isEmpty() {
        final boolean isEmpty;
        synchronized (this) {
            isEmpty = elements.isEmpty();
        }
        return isEmpty;
    }

    @Override
    public int size() {
        final int size;
        synchronized (this) {
            size = elements.size();
        }
        return size;
    }

}
