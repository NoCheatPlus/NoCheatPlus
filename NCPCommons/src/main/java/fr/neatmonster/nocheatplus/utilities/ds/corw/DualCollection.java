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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Keep two collections, one for access by the primary thread, one for
 * asynchronous access, for iteration within the primary thread. This is
 * intended to provide a way to keep locking away from the primary thread as
 * much as possible, for cases where it's certain that both the primary thread
 * as well as other threads will add elements, at least on occasion. Internal
 * collections are kept null, with a little hysteresis built in, to avoid
 * constant repetition of object creation. Multiple collections and differing
 * collection types can be processed under the same lock, to reduce the
 * probability of the primary thread running into contention.
 * <hr>
 * The methods don't check if you're within the correct thread, consequently
 * they are named according to how they're supposed to be used. Thus use of this
 * class is not confined to the Bukkit primary thread, despite having been
 * created with that context in mind.<br>
 * The internal collections stay null, until elements are added. Setting the
 * fields to null, will happen lazily within mergePrimaryThread and
 * isEmptyPrimaryThread, to avoid re-initializing every iteration. So the
 * suggested sequence is: adding elements - mergePrimaryThread - only if not
 * isEmptyPrimaryThread() then iteratorPrimaryThread, because the iterator will
 * force creation of a collection, if the field is set to null. After iteration
 * use clearPrimaryThread. Instead of mergePrimaryThread() and then checking
 * isEmptyPrimaryThread, isEmtpyAfterMergePrimaryThread can be used to do both
 * in one method call.
 * 
 * @author asofold
 * 
 * 
 * @param <T>
 *            The type of stored elements.
 * @param <C>
 *            The Collection type.
 *
 */
public abstract class DualCollection<T, C extends Collection<T>> {

    private final Lock lock;

    // Might consider setting sets to null, until used.
    private Collection<T> primaryThreadCollection = null;
    private Collection<T> asynchronousCollection = null;

    /** Once emptyCount reaches nullCount, empty collections are nulled. */
    private short nullCount = 6;
    /**
     * Number of times, clear has been called with the collection being empty
     * already.
     */
    private short emptyCount = 0;

    /**
     * Initialize with a new ReentrantLock.
     */
    public DualCollection() {
        this(new ReentrantLock());
    }

    /**
     * Initialized with the given lock.
     * @param lock
     *            The lock to use for locking.
     */
    public DualCollection(Lock lock) {
        this.lock = lock;
    }

    //////////
    // Setup
    //////////

    /**
     * Roughly control how fast lists reset. This is not exact, as both primary
     * thread and asynchronous collections being empty but not null, could both
     * cause the emptyCount to increase. Thus with one being null for a couple
     * of iterations, the count only is increased with the other being
     * empty.<br>
     * The emptyCount is increased within the primary thread only, on
     * mergePrimaryThread for the asynchronous collection being empty, on
     * isEmptyPrimaryThread for the primary thread collection being empty. The
     * emptyCount is reset to 0 only within isEmtpyPrimaryThread, on setting the
     * primaryThreadCollection to null.
     * 
     * @param nullCount
     */
    public void setNullCount(short nullCount) {
        this.nullCount = nullCount;
    }

    /////////////
    // Abstract
    /////////////

    /**
     * Retrieve a new collection for internal storage. <br>
     * Must be thread-safe, as collections may be created lazily, and on
     * occasion get nulled.
     */
    protected abstract C newCollection();

    ////////////////
    // Thread-safe
    ////////////////

    /**
     * Add an element to the asynchronous collection. <br>
     * Thread-safe.
     * 
     * @param element
     * @return
     */
    public boolean addAsynchronous(T element) {
        lock.lock();
        if (asynchronousCollection == null) {
            asynchronousCollection = newCollection();
        }
        final boolean res = asynchronousCollection.add(element);
        lock.unlock();
        return res;
    }

    /**
     * Add multiple elements to the asynchronous collection. <br>
     * Thread-safe.
     * 
     * @param elements
     * @return
     */
    public boolean addAllAsynchronous(Collection<T> elements) {
        if (elements.isEmpty()) {
            return false;
        }
        else {
            lock.lock();
            if (asynchronousCollection == null) {
                asynchronousCollection = newCollection();
            }
            final boolean res = asynchronousCollection.addAll(elements);
            lock.unlock();
            return res;
        }
    }

    /**
     * Test if the asynchronous collection contains an element, will use
     * locking, unless the field is set to null. <br>
     * Thread-safe.
     * 
     * @param element
     * @return
     */
    public boolean containsAsynchronous(T element) {
        if (asynchronousCollection == null) {
            // Opportunistic.
            return false;
        }
        else {
            ;
            lock.lock();
            // (Could be set to null within the primary thread.)
            final boolean res = asynchronousCollection == null ? false : asynchronousCollection.contains(element);
            lock.unlock();
            return res;
        }
    }

    /////////////////////////
    // Primary thread only.
    /////////////////////////

    /**
     * Add all elements from the asynchronous collection to the primary thread
     * collection under lock - this may be omitted, if the asynchronous
     * collection is nulled, to avoid locking. This will clear the asynchronous
     * collection. This will increase the emtpyCount, and would null the
     * asynchronousCollection on the emptyCount reaching the nullCount. <br>
     * Primary thread only.
     */
    public void mergePrimaryThread() {
        if (asynchronousCollection != null) { // Opportunistic.
            lock.lock();
            // (Can only be set to null within the primary thread.)
            internalMergePrimaryThreadNoLock();
            lock.unlock();
        }
    }

    /**
     * Demands asynchronousCollection to be not null, and to be called under
     * lock. <br>
     * Primary thread only.
     */
    private void internalMergePrimaryThreadNoLock() {
        if (asynchronousCollection.isEmpty()) {
            if (++ emptyCount >= nullCount) {
                asynchronousCollection = null;
            }
        }
        else {
            if (primaryThreadCollection == null) {
                primaryThreadCollection = newCollection();
                emptyCount = 0;
            }
            primaryThreadCollection.addAll(asynchronousCollection);
            asynchronousCollection.clear();
        }
    }

    /**
     * Same as mergePrimaryThread, just not locking. <br>
     * <b>Only use with external control over the lock and in locked
     * state.</b><br>
     * Primary thread only.
     */
    public void mergePrimaryThreadNoLock() {
        if (asynchronousCollection != null) {
            internalMergePrimaryThreadNoLock();
        }
    }

    /**
     * Add an element to the primary thread collection. <br>
     * Primary thread only.
     * 
     * @param element
     * @return
     */
    public boolean addPrimaryThread(T element) {
        if (primaryThreadCollection == null) {
            primaryThreadCollection = newCollection();
            emptyCount = 0;
        }
        return primaryThreadCollection.add(element);
    }

    /**
     * Add multiple elements to the primary thread collection. <br>
     * Primary thread only.
     * 
     * @param elements
     * @return
     */
    public boolean addAllPrimaryThread(Collection<T> elements) {
        if (primaryThreadCollection == null) {
            primaryThreadCollection = newCollection();
            emptyCount = 0;
        }
        return primaryThreadCollection.addAll(elements);
    }

    /**
     * Test if the primary thread collection is empty. This will increase the
     * emtpyCount, if the collection is empty, and it would null the
     * primaryThreadCollection and reset the emptyCount, if the emptyCount
     * reaches the nullCount.<br>
     * Primary thread only.
     * 
     * @return
     */
    public boolean isEmptyPrimaryThread() {
        if (primaryThreadCollection == null) {
            return true;
        }
        else if (primaryThreadCollection.isEmpty()) {
            if (++ emptyCount >= nullCount) {
                primaryThreadCollection = null;
                emptyCount = 0;
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Convenience method to call mergePrimaryThread() first, then return
     * isEmptyPrimaryThread().<br>
     * Primary thread only.
     * 
     * @return If the primary thread collection is empty, after adding all
     *         elements from the asynchronous collection.
     */
    public boolean isEmtpyAfterMergePrimaryThread() {
        mergePrimaryThread();
        return isEmptyPrimaryThread();
    }

    /**
     * Same as isEmtpyAfterMergePrimaryThread, just not locking. <br>
     * <b>Only use with external control over the lock and in locked
     * state.</b><br>
     * Primary thread only.
     */
    public boolean isEmtpyAfterMergePrimaryThreadNoLock() {
        mergePrimaryThreadNoLock();
        return isEmptyPrimaryThread();
    }

    /**
     * Test if the primary thread collection contains the given element. <br>
     * Primary thread only.
     * 
     * @return
     */
    public boolean containsPrimaryThread(final T element) {
        return primaryThreadCollection == null ? false : primaryThreadCollection.contains(element);
    }

    /**
     * Iterator for the primary thread collection. <br>
     * Primary thread only.
     * 
     * @return
     */
    public Iterator<T> iteratorPrimaryThread() {
        // TODO: Consider to store an empty and/or unmodifiable iterator.
        if (primaryThreadCollection == null) {
            primaryThreadCollection = newCollection();
        }
        return primaryThreadCollection.iterator();
    }

    /**
     * Return a merged collection and clear internal ones - primary thread only.
     * 
     * @return Returns null, if no elements are contained.
     */
    public Collection<T> getMergePrimaryThreadAndClear() {
        mergePrimaryThread();
        final Collection<T> res = this.primaryThreadCollection;
        this.primaryThreadCollection = null;
        return (res == null || res.isEmpty()) ? null : res;
    }

    /**
     * Clear the primary thread collection. <br>
     * Primary thread only.
     * 
     * @return
     */
    public void clearPrimaryThread() {
        if (primaryThreadCollection != null) {
            primaryThreadCollection.clear();
        }
    }

}
