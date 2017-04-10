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

import java.util.List;

/**
 * Thread-safe queue - a replace-on-read-all queue-thing, supposedly exchanging
 * the internally stored list by a new empty one under lock, for a very small
 * locking time, so it is not really a typical copy-on-read. All methods use
 * locking, implementation-specific.
 * 
 * @author asofold
 *
 */
public interface IQueueRORA<E> {

    /**
     * Add to list.
     * 
     * @param element
     * @return Size of queue after adding.
     */
    public int add(final E element);

    /**
     * 
     * @return An ordinary List containing all elements. This should be the
     *         previously internally stored list, to keep locking time minimal.
     */
    public List<E> removeAll();

    /**
     * Remove oldest entries until maxSize is reached.
     * 
     * @param maxSize
     * @return
     */
    public int reduce(final int maxSize);


    public void clear();


    public boolean isEmpty();


    public int size();

}
