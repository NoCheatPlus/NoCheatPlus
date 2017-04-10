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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * IQueueRORA implementation using an external Lock or a ReentrantLock for
 * locking, with a LinkedList inside.
 * 
 * @author asofold
 *
 * @param <E>
 */
public class QueueRORAWithLock<E> implements IQueueRORA<E> {

    private final Lock lock;
    private LinkedList<E> elements = new LinkedList<E>();

    public QueueRORAWithLock() {
        this(new ReentrantLock());
    }

    public QueueRORAWithLock(Lock lock) {
        this.lock = lock;
    }

    @Override
    public int add(final E element) {
        lock.lock();
        elements.add(element);
        final int size = elements.size();
        lock.unlock();
        return size;
    }

    @Override
    public List<E> removeAll() {
        lock.lock();
        final List<E> result = elements;
        elements = new LinkedList<E>();
        lock.unlock();
        return result;
    }

    @Override
    public int reduce(final int maxSize) {
        int dropped = 0;
        lock.lock();
        final int size = elements.size();
        if (size  <= maxSize) {
            return dropped;
        }
        while (dropped < size - maxSize) {
            elements.removeFirst();
            dropped ++;
        }
        lock.unlock();
        return dropped;
    }

    @Override
    public void clear() {
        removeAll();
    }

    @Override
    public boolean isEmpty() {
        lock.lock();
        final boolean isEmpty = elements.isEmpty();
        lock.unlock();
        return isEmpty;
    }

    @Override
    public int size() {
        // TODO: Could maintain an int, simply.
        lock.lock();
        final int size = elements.size();
        lock.unlock();
        return size;
    }

}
