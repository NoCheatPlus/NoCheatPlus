package fr.neatmonster.nocheatplus.utilities.ds.corw;

import java.util.LinkedList;
import java.util.List;


/**
 * A replace-on-read-all queue-thing, exchanging the internal list under lock by
 * a new empty one, for a small locking time, so it is not really a typical
 * copy-on-read. All methods use locking, the QueueRORA instance is used for
 * locking.
 * 
 * @author dev1mc
 *
 */
public class QueueRORA<E> {
    
    private LinkedList<E> elements = new LinkedList<E>();
    
    /**
     * Add to list (synchronized).
     * @param element
     * @return Size of queue after adding.
     */
    public int add(final E element) {
        final int size;
        synchronized (this) {
            elements.add(element);
            size = elements.size();
        }
        return size;
    }
    
    /**
     * 
     * @return An ordinary (linked) List containing all elements. 
     */
    public List<E> removeAll() {
        final List<E> result;
        synchronized (this) {
            result = elements;
            elements = new LinkedList<E>();
        }
        return result;
    }
    
    /**
     * Remove oldest entries until maxSize is reached.
     * @param maxSize
     * @return
     */
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
    
    public void clear() {
        removeAll();
    }
    
    /**
     * 
     * @return
     */
    public boolean isEmpty() {
        final boolean isEmpty;
        synchronized (this) {
            isEmpty = elements.isEmpty();
        }
        return isEmpty;
    }
    
    /**
     * 
     * @return
     */
    public int size() {
        final int size;
        synchronized (this) {
            size = elements.size();
        }
        return size;
    }
    
}
