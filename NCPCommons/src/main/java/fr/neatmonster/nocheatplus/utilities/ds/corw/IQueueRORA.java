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
